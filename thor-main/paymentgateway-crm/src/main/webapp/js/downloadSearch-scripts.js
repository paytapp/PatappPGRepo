$(document).ready(function(e){
    
    $('#example').DataTable( {
        dom: 'B',
        buttons: [
			{
				extend: 'csv',
				text: 'Download CSV Format',
				className: 'lpay_button lpay_button-md lpay_button-primary',
				exportOptions: {
					modifier: {
						search: 'none'
					}
				}
			}
		]
    });

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
                $("#bulkUpdateSubmit").attr("disabled", false);
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500);
            }else{
                $(this).closest("label").attr("data-status", "error-status");
                $("#bulkUpdateSubmit").attr("disabled", true);
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500);
            }
        }
    });

    document.querySelector("#bulkUpdateSubmit").onclick = function(e){
        setTimeout(function(e){
            document.querySelector(".default-upload").classList.remove("d-none");
            document.querySelector("#placeholder_img").style.display = "block";
            $(".lpay_upload_input").val("");
            $(".lpay-upload").removeAttr("data-status");
        }, 1500)
    }

})