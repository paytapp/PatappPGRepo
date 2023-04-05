function tabChange(_selector){
    var _getClickTab = _selector;
    $(".lpay-nav-item").removeClass("active");
    $("[data-id='"+_getClickTab+"']").closest("li").addClass("active");
    $(".lpay_tabs_content").addClass("d-none");
    $("[data-target="+_getClickTab+"]").removeClass("d-none");
}

var _userType = document.querySelector("#userType").value;
if(_userType == "RESELLER"){
    tabChange("merchantList");    
}else{
    tabChange("resellerList");    
}

$(document).ready(function(e){

    // ===================== merchant List Function starts ========================

    function merchantList(_that , _dataTableId, _userType, _data){

        document.querySelector(_dataTableId).closest(".lpay_tabs_content").setAttribute("data-active", "true");

        var _hasAttribute = document.querySelector(_dataTableId).closest(".lpay_tabs_content").getAttribute("data-active");
        
        if(_hasAttribute == "true"){

            $("body").removeClass("loader--inactive");

            function generatePostData(d){
                var token = document.getElementsByName("token")[0].value;
                var obj = {
                    userType : _userType
                };
                return obj;
            }
    
            $(_dataTableId).dataTable({
                dom : 'BTftlpi',
                buttons: ['csv', 'print', 'pdf'],
                language: {
                    search: "",
                    searchPlaceholder: "Search"
                },
                "ajax": {
                    "type": "post",
                    "url": "getUserStatusList",
                    "data" : function (d){
                            return generatePostData(d);
                        }
                    },
                    "fnDrawCallback" : function(settings, json) {
                        hideColumn(_userType, _dataTableId);
                        $("body").addClass("loader--inactive");
                    },
                    "destroy": true,
                    "order": [[ 3, 'desc' ]],
                    "sAjaxDataProp" : _data,
                    "aoColumns": [
                    {"mData" : "payId", "className": "payId"},
                    {"mData" : "businessName", "className": "businessName"},
                    {"mData" : "emailId"}, 
                    {"mData" : "userTypeOrName", "className": "userTypeOrName"},
                    {"mData" : "status", "className": "status"},
                    {"mData" : "modeType", "className" : "mode_type", "width" : "123px"},
                    {
                        "mData" : null,
                        "sClass" : "center",
                        "bSortable" : false,
                        "mRender" : function() {
                            return '<div class="edit-mode"><button class="lpay_button lpay_button-sm lpay_button-secondary pointer editMerchant" id="editPermission">Edit</button></div><div class="save-mode"><button class="lpay_button lpay_button-sm lpay_button-primary pointer saveMerchant" id="saveMerchant"><i class="fa fa-check" aria-hidden="true"></i></button><button class="lpay_button lpay_button-sm lpay_button-secondary pointer cancelMerchant" id="canelMerchant"><i class="fa fa-times" aria-hidden="true"></i></button></div>';
                    }
                }
                            
                ]
            });

            document.querySelector(_dataTableId).closest(".lpay_tabs_content").setAttribute("data-active", "false");
            
    
            setTimeout(function(e){
                $("body").addClass("loader--inactive");
            }, 1000)
        }

    }

    function parentMerchantList(_that , _dataTableId, _userType, _data){

        document.querySelector(_dataTableId).closest(".lpay_tabs_content").setAttribute("data-active", "true");

        var _hasAttribute = document.querySelector(_dataTableId).closest(".lpay_tabs_content").getAttribute("data-active");
        
        if(_hasAttribute == "true"){

            $("body").removeClass("loader--inactive");

            function generatePostData(d){
                var token = document.getElementsByName("token")[0].value;
                var obj = {
                    userType : _userType
                };
                return obj;
            }
    
            $(_dataTableId).dataTable({
                dom : 'BTftlpi',
                buttons: ['csv', 'print', 'pdf'],
                language: {
                    search: "",
                    searchPlaceholder: "Search"
                },
                "ajax": {
                    "type": "post",
                    "url": "getUserStatusList",
                    "data" : function (d){
                            return generatePostData(d);
                        }
                    },
                    "fnDrawCallback" : function(settings, json) {
                        hideColumn(_userType, _dataTableId);
                        $("body").addClass("loader--inactive");
                    },
                    "destroy": true,
                    "order": [[ 3, 'desc' ]],
                    "sAjaxDataProp" : _data,
                    "aoColumns": [
                    {"mData" : "payId", "className": "payId"},
                    {"mData" : "businessName", "className": "businessName"},
                    {"mData" : "emailId"}, 
                    {"mData" : "userTypeOrName", "className": "userTypeOrName"},
                    {"mData" : "status", "className": "status"},
                    {"mData" : "modeType", "className" : "mode_type", "width" : "123px"},
                    {
                        "mData" : null,
                        "sClass" : "center",
                        "bSortable" : false,
                        "mRender" : function() {
                            return '<div class="edit-mode"><button class="lpay_button lpay_button-sm lpay_button-secondary pointer editMerchant" id="editPermission">Edit</button></div><div class="save-mode"><button class="lpay_button lpay_button-sm lpay_button-primary pointer saveMerchant" id="saveMerchant"><i class="fa fa-check" aria-hidden="true"></i></button><button class="lpay_button lpay_button-sm lpay_button-secondary pointer cancelMerchant" id="canelMerchant"><i class="fa fa-times" aria-hidden="true"></i></button></div>';
                    }
                }
                            
                ]
            });

            document.querySelector(_dataTableId).closest(".lpay_tabs_content").setAttribute("data-active", "false");
            
    
            setTimeout(function(e){
                $("body").addClass("loader--inactive");
            }, 1000)
        }

    }

    function hideColumn(_value, _dataTableId){
        var _checkMerchant = _value;
        var _table = $(_dataTableId).DataTable();
        if(_checkMerchant == "MERCHANT" || _checkMerchant == "SUPER_MERCHANT"){
            _table.columns(3).visible(false);
            _table.columns(5).visible(true);
        }else{
            _table.columns(3).visible(true);
            _table.columns(5).visible(false);
        }
    }


    if(_userType == "RESELLER"){
        merchantList(null, "#merchantListDatatable", "MERCHANT", "listMerchant");
    }else{
        merchantList(null, "#resellerListDatatable", "RESELLER", "listReseller");
    }


    // sent action for Merchant list
    if(document.querySelector("#resellerList") != null){
        document.querySelector("#resellerList").addEventListener("click", function(e){
            merchantList(e, "#resellerListDatatable", "RESELLER", "listReseller");
        })
    }

    // sent action for Merchant list
    document.querySelector("#merchantList").addEventListener("click", function(e){
        merchantList(e, "#merchantListDatatable", "MERCHANT", "listMerchant");
    })

    // sent action for Sup Merchant list
    document.querySelector("#superMerchantList").addEventListener("click", function(e){
        merchantList(e, "#superMerchantListDatatable", "SUPER_MERCHANT", "listSuperMerchant");
    })

    // sent action for Sub Merchant list
    document.querySelector("#subMerchantList").addEventListener("click", function(e){
        merchantList(e, "#subMerchantListDatatable", "SUB_MERCHANT", "listSubMerchant");
    })
    // sent action for Sub Merchant list
    document.querySelector("#parentMerchantList").addEventListener("click", function(e){
        merchantList(e, "#parentMerchantListDatatable", "PARENTMERCHANT", "listParentMerchant");
    })

    // sent action for subuser list
    if(document.querySelector("#subUserList") != null){
        document.querySelector("#subUserList").addEventListener("click", function(e){
            merchantList(e, "#subUserListDatatable", "SUBUSER", "listSubUser");
        })
    }

    // datatable edit mode
    $("body").on("click", ".editMerchant", function(e){
        var _checkEditMode = document.querySelector(".edit-tr");
        if(_checkEditMode == null){
            var _par = $(this).closest("tr");
            var _status = _par.find(".status").text();
            var _activeTab = document.querySelector(".lpay-nav-item.active").querySelector("a").getAttribute("data-id");
            var _getId = document.querySelector("[data-target='"+_activeTab+"']").querySelector("table").id;
            _par.addClass("edit-tr");
            var _getHtml = $("#userStatus").html();
            _par.find(".status").css("width", "300px");
            _par.find(".status").append(_getHtml);
            _par.find(".status .bootstrap-select").remove();
            _par.find(".status #status").attr("id", "newStatus");
            _par.find(".status .bootstrap-select").remove();
            if(_activeTab == "merchantList" || _activeTab == "superMerchantList"){
                var table = new $.fn.dataTable.Api('#'+_getId);
                var _data = table.rows(_par).data();
                _par.find(".status #processingmode").attr("id", "newMode");
                $("#newMode").selectpicker();
                if(_data[0]['modeType'] != null){
                    $("#newMode").selectpicker('val', _data[0]['modeType']); 
                }
            }
            $("#newStatus").selectpicker('refresh');
            $("#newStatus").selectpicker('val', _status.trim());
        }else{
            alert("You have already opened edit in other tab")
        }
    })

    $("body").on("click", ".cancelMerchant", function(e){
        var _par = $(this).closest("tr");
        _par.removeClass("edit-tr");
        _par.find(".status").css("width", "92px");
        $("#newStatus").selectpicker('refresh');
        _par.find(".lpay_select_group").remove();
    })

    $("body").on("click", ".saveMerchant", function(e){
        var _par = $(this).closest("tr");
        var _userType = $(".lpay-nav-item.active").find(".lpay-nav-link").attr("data-type");
        var _table = $(".lpay-nav-item.active").find(".lpay-nav-link").attr("id");
        var _currentTableData = $(".lpay-nav-item.active").find(".lpay-nav-link").attr("data-active");
        var _payId = _par.find(".payId").text();
        var _status = _par.find(".status #newStatus").val();
        var _mode = _par.find(".status #newMode").val();
        $.ajax({
            type : "post",
            url : "updateUserStatus",
            data: {
                "userType" : _userType,
                "userStatus" : _status,
                "payId" : _payId,
                "modeType" : _mode
            },
            success: function(data){
                if(data.response == "SUCCESS"){
                    merchantList(null, "#"+_table+"Datatable", data.userType, _currentTableData);
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".responseMsg").text("Data has been saved successfully.");
                    $(".lpay_popup").fadeIn();
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text(data.response);
                    $(".lpay_popup").fadeIn();
                }
                _par.removeClass("edit-tr");
                _par.find(".lpay_select_group").remove();
            }
        })
    })

    
    $(".confirmButton").on("click", function(e){
        $(".lpay_popup").fadeOut();
    });

    $(".download-userlist").on("click", function(e) {
        e.preventDefault();

        $("body").removeClass("loader--inaction");

        var userType = $(this).attr("data-type");

        $("#list-userType").val(userType);
        $("#downloadUserList").submit();

        setTimeout(function() {
            $("body").addClass("loader--inaction");
        }, 1000);
    });
});