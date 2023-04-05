$(document).ready(function() {
    // upload file
    $(".lpay_upload_input").on("change", function(e){
        var _val = $(this).val();
        var _fileSize = $(this)[0].files[0].size;
        var _tmpName = _val.replace("C:\\fakepath\\", "");
        if(_val != ""){
            $("body").removeClass("loader--inactive");
            $(".default-upload").addClass("d-none");
            $("#placeholder_img").css({"display":"none"});
            if(_fileSize < 2000000){
                $(this).closest("label").attr("data-status", "success-status");
                $("#fileName").text(_tmpName);
                $("#studentInventrySubmit").attr("disabled", false);
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500);
            }else{
                $(this).closest("label").attr("data-status", "error-status");
                $("#studentInventrySubmit").attr("disabled", true);
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500);
            }
        }
    });

    $("#studentInventrySubmit").on("click", function(e){
        $("#uploadInvForm").submit();
    })

    var _getResponse = $("#response").text();
    if(_getResponse != ""){
        alert(_getResponse);
    }else{
        console.log("bye")
    }

    $('#example').DataTable( {
        dom: 'B',
        buttons: [
            {extend: 'csv', className: "lpay_button lpay_button-md lpay_button-primary", text: "Download CSV Format"}
        ]
    });
});
