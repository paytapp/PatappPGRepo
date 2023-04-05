function renderTable() {
    document.querySelector("body").classList.remove("loader--inactive");
    $('#txnResultDataTable').dataTable({
        "ajax" : {

            "url" : "subMerchantSearchAction",
            "type" : "POST",
            "data" : function(d) {
                return generatePostData(d);
            }
        },
            
        "fnDrawCallback" : function() {				
            $("#submit").removeAttr("disabled");
            hideColumn();
            setTimeout(function(e){
                document.querySelector("body").classList.add("loader--inactive");
            }, 500)
        },
        "paginationType" : "full_numbers",
        "lengthMenu" : [ [ 10, 25, 50 ], [ 10, 25, 50 ] ],
        "order" : [ [ 2, "desc" ] ],
        "columns" : [
            {
                "data" : "superMerchantName",
                "className" : "superMerchantName"
            },
            {
                "data" : "businessName",
                "className" : "businessName"
            },
            {
                "data" : "payId",
                "className" : "status",
                "class":"payId"									
            },
            {
                "data" : "emailId",
                "className" : "status",
                "class":"emailId"									
            },
            {
                "data" : "mobile"
            },
            {
                "data" : "updatedBy",
                "className" : "status",
                "class":"updatedBy"									
            },
            {
                "data" : "updationDate",
                "className" : "status",
                "class":"updationDate"									
            },
            {
                "data" : "status"
            },							
            {
                "data" : null,
                "orderable" : false,
                "mRender" : function(row) {
                    return '<button class="lpay_button lapy_button-md lpay_button-secondary btnChargeBack"  onClick = "editSubMerch(this)">Edit</button>';
                }
            },
        ]
    });
}

function reloadTable(){
    document.querySelector("body").classList.remove("loader--inactive");
    $("#submit").attr("disabled", true);
    var tableObj = $('#txnResultDataTable');
    var table = tableObj.DataTable();
    table.ajax.reload();
    	
}

document.querySelector("#submit").onclick = reloadTable;

$(document).ready(function(e){
    renderTable();
})

function hideColumn(){
    
    var _table = $("#txnResultDataTable").DataTable();
    var _resellerMerchant = $("#resellerMerchantSignupFlag").val();

    if(_resellerMerchant == "true"){
        _table.columns(8).visible(false);
    }
    // console.log(_getActive);
}

function generatePostData(d) {
    var token = document.getElementsByName("token")[0].value;
    var superMerchant = document.getElementById("merchant").value;
    var subMerchantEmail = document.getElementById("subMerchantEmail").value;
    var mobile = document.getElementById("mobile").value;
    var status = document.getElementById("status").value;

    if (superMerchant == '') {
        superMerchant = 'ALL'
    }
    if (subMerchantEmail == '') {
        subMerchantEmail = 'ALL'
    }
    if (mobile == '') {
        mobile = 'ALL'
    }
    if (status == '') {
        status = 'ALL'
    }		

    var obj = {
        superMerchant : superMerchant,
        subMerchantEmail : subMerchantEmail,
        mobile : mobile,
        status : status,
        draw : d.draw,
        length : d.length,
        start : d.start,
        token : token,
        "struts.token.name" : "token",
    };

    return obj;
}

function editSubMerch(val) {
    $("body").removeClass("loader--inactive");
    var payId = $(val).closest("tr").find(".payId").text();
    var _businessName = $(val).closest("tr").find(".businessName").text();
    var _superMerchantName = $(val).closest("tr").find(".superMerchantName").text();
    document.getElementById('payId').value = payId;
    document.querySelector("#businessNameSubMerchant").value = _businessName;
    document.querySelector("#superMerchantName").value = _superMerchantName;
    document.getElementById('subMerchEditFrm').submit();	
}	