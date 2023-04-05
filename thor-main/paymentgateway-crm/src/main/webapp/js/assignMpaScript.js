$(document).ready(function(e){

    var _token = $("[name='token']").val();

    // detail action
    function deleteAction(e){
        
        var _parent = $(".active-tr");
        var _categories = _parent.find(".categoryTd").text();
        $("body").removeClass("loader--inactive");
        $.ajax({
            type: "post",
            url: "deleteCheckerMakerAction",
            data: {"industryCategory": _categories, "token": _token},
            success: function(data){
                var table = $("#table_id").DataTable();
                table.ajax.reload();
                $("body").addClass("loader--inactive");
            },
            error: function(data){
            }
        });
    }
    

    $("body").on("click", ".btn-delete", function(e){
        e.preventDefault();
        var _parent = $(this).closest("tr");
        $("#table_id tr").removeClass("active-tr");
        _parent.addClass("active-tr");
        $("#fancybox").fancybox({
            'overlayShow': true
        }).trigger('click');
    });

    $("body").on("click", "#confirm-btn", function(e){
        deleteAction();
        $.fancybox.close();
    });
    $("body").on("click", "#cancel-btn", function(e){
        $.fancybox.close();
    })

    // function to send data to backend

    function saveAction(url){
        var _categories = $("#categories").val();
        var _maker = $("#maker").val();
        var _checker = $("#checker").val();
        if(_categories != "-1" && _maker != "-1" && _checker != "-1"){
            if(_maker != _checker){
                $("body").removeClass("loader--inactive");
                $.ajax({
                    type: "post",
                    url: url,
                    data: { "industryCategory": _categories, "checkerPayId": _checker, "makerPayId": _maker, "token": _token },
                    success: function(data){
                        console.log(data);
                        var table = $("#table_id").DataTable();
                        table.ajax.reload();
                        location.reload(true);
                        $(".error-msg").addClass("d-none");
                        $("option[value='-1']").attr("selected", "selected");
                        $(".assign-mpa-table").attr("disabled", false);
                        $("body").addClass("loader--inactive");
                    },
                    error: function(data){
                        $("option[value='-1']").attr("selected", "selected");
                        $(".error-msg").removeClass("d-none");
                        $("body").addClass("loader--inactive");
                    }
                })
            }else{
                alert("Maker and Checker Should Not be Same");
            }
        }
    }

    $("body").on("click", "#submit-btn", function(){
        saveAction("saveCheckerMakerAction");
    });

    $("body").on("click", "#submit-edit", function(){
        saveAction("editCheckerMaker");
    });

    $(".merchant__form_control").on("change", function(e){
        $(".error-msg").addClass("d-none");
    })
    

    function makerChecker(_data){
        $("body").removeClass("loader--inactive");
        var token = $("[name='token']").val();
        $.ajax({
            type: "post",
            url: "fetchMakerCheckerListAction",
            data: { "industryCategory": _data,"token": token },
            success: function(data){
                // var getData = data.industryTypes;
                makerData(data, "checkerMakeList", "Checker", "#checker");
                makerData(data, "checkerMakeList", "Maker", "#maker");
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 1000);
            },
            error: function(data){
            }
        })
    }

    function industryCategory(){
        $("body").removeClass("loader--inactive");
        var token = $("[name='token']").val();
        $.ajax({
            type: "post",
            url: "fetchMakerCheckerListAction",
            data: {"token": token },
            success: function(data){
                industryType(data, "#categories");
                $("body").addClass("loader--inactive");
            },
            error: function(data){

            }
        })
    }

    

    industryCategory();
    // industry type/

    function industryType(data, id){
        var getData = data.industryTypes;
        for(key in getData){
            $(id).append("<option value='"+ getData[key]+"'>"+ getData[key]+"</option>")
        }
        $(id).selectpicker();
    }

    $("#checker").selectpicker();
    $("#maker").selectpicker();

    function makerData(data, checkerMaker,checker, id){
        $(id).html("");
        $("#maker").append("<option value='-1'>Select Maker</option>");
        $("#checker").append("<option value='-1'>Select Checker</option>");
        for(var i = 0; i < data[checkerMaker].length; i++){
            $(id).append("<option data-value='"+ data[checkerMaker][i]["name"] +"' value='"+data[checkerMaker][i]["payId"]+"'>"+ data[checkerMaker][i]["name"] +"</option>");
        }
        $(id).selectpicker('refresh');
        // $(id).selectpicker();
    }

    /// makerChecker();

    $("#categories").on("change", function(e){
        var _this = $(this).val();
        if(_this != ""){
            makerChecker(_this);
        }
    })
    
    $('#table_id').DataTable({
        language: {
            search: "",
            searchPlaceholder: "Search..."
        },
        dom : 'BTftlpi',
        buttons: ['csv', 'print', 'pdf'],
        "ajax": {
            "url" : "fetchAllCheckerMakerAction",
            "type" : "POST",
            "data" : {
                "token": _token
            }
        },
        "columns": [
        {"data" : "industryCategory",className: "categoryTd"},
        {
            "data": null,
            className: "makerId",
            "render" : function(data, type, full) {
                return '<span id="makerInfo" data-input="'+data["makerPayId"]+'">'+data["makerName"]+'</span>'
            }
    },
        {
            "data": null,
            className: "checkerId",
            "render" : function(data, type, full) {
                return '<span id="checkerInfo" data-input="'+data["checkerPayId"]+'">'+data["checkerName"]+'</span>'
            }
        },
        {"data": null, 
        "mRender": function (o) { return '<div class="actionBtns visual-mode"><a href="#" class="btn-edit bg-color-primary-light-2 color-primary hover-color-primary border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-pencil-alt"></i></a><a href="#"  class="btn-delete bg-color-red-lightest color-red hover-color-red border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-trash"></i></a></div>'; }
        },
        
        ],
    });

    // edit button script
    $("body").on("click", ".btn-edit", function(e){
        $("#submit-btn").attr("id", "submit-edit");
        $("option").removeAttr("selected");

        var _parent = $(this).closest("tr");
        $(".assign-mpa-table").attr("disabled", true);
        $("#cancel-btn").removeClass("d-none");
        var _category = _parent.find(".categoryTd").text();
        makerChecker(_category);
        var _maker = _parent.find(".makerId").find("span").attr("data-input");
        var _checker = _parent.find(".checkerId").find("span").attr("data-input");
        // $("option[value='"+_category+"']").attr("selected", "selected");
        setTimeout(function(){
            $("#categories").selectpicker('refresh');
            $("#maker").selectpicker('refresh');
            $("#checker").selectpicker('refresh');
            $("#categories").selectpicker('val', _category);
            $("#maker").selectpicker('val', _maker);
            $("#checker").selectpicker('val', _checker);
            $("body").addClass("loader--inactive");
        }, 1000)
        $("#categories").attr("disabled", true);
    })
    //cancel button
    $("body").on("click", ".cancel-btn", function(e){
        e.preventDefault();
        $("#categories").selectpicker('refresh');
            $("#maker").selectpicker('refresh');
            $("#checker").selectpicker('refresh');
        $(".selectpicker").selectpicker('val', "-1");
        $("#submit-edit").attr("id", "submit-btn");
        $("#cancel-btn").addClass("d-none");
        $(".assign-mpa-table").attr("disabled", false);
        $("option[value='-1']").attr("selected", "selected");
        $("#categories").attr("disabled", false);
        $("#categories").selectpicker('refresh');
            $("#maker").selectpicker('refresh');
            $("#checker").selectpicker('refresh');
    })


    

})