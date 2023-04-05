$(document).ready(function() {

    $("#merchantPayId").on("change", function(e){
        var _this = $(this).val();
        var _file = $("#upload-input").val();
        $("[data-id=payId]").val(_this);
        if(_this != "" && _file != ""){
            $("#dispatchSubmit").attr("disabled", false);
        }else{
            $("#dispatchSubmit").attr("disabled", true);
        }
    });
	$("#dispatchSubmit").on("click", function(e){
        $("#saveDispatchSlip").submit();
    });

    // upload file
    $(".lpay_upload_input").on("change", function(e){
        var _val = $(this).val();
        var _fileSize = $(this)[0].files[0].size;
        var _tmpName = _val.replace("C:\\fakepath\\", "");
        var _fileExtn = _tmpName.substring(_tmpName.lastIndexOf("."));
        console.log(_fileExtn);
        if(_val != ""){
            $("body").removeClass("loader--inactive");
            $(".default-upload").addClass("d-none");
            $("#placeholder_img").css({"display":"none"});
            if(_fileExtn == ".csv"){
                if(_fileSize < 2000000){
                    $(this).closest("label").attr("data-status", "success-status");
                    $("#fileName").text(_tmpName);
                    $("#realFileName").val(_tmpName);
                    
                    if($("#merchantPayId").val() != ""){
                        $("#dispatchSubmit").attr("disabled", false);
                    }
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    }, 500);
                }else{
                    $(this).closest("label").attr("data-status", "error-status");
                    $("#dispatchSubmit").attr("disabled", true);
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    }, 500);
                }
            }else{
                $(this).closest("label").attr("data-status", "error-status-file");
                    $("#dispatchSubmit").attr("disabled", true);
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                }, 500);
            }
        }
    });

    $("#dispatchSubmit").on("click", function(e){
        $("#uploadDispatch").submit();
    });

    $('#example').DataTable( {
        dom: 'B',
        buttons: [
            {extend: 'csv', className: "lpay_button lpay_button-md lpay_button-primary", text: "Download CSV Format"}
        ]
    });
});
