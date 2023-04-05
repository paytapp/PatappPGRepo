function tabShow(_this){
    var _li = document.querySelectorAll(".lpay_tabs li");
    var _div = document.querySelectorAll(".lpay_tabs_content");
    _div.forEach(function(index, element, array){
        index.classList.add("d-none");
    })
    _li.forEach(function(index, element, array){
        index.classList.remove("active");
    })
    document.querySelector("[data-id='"+_this+"']").closest("li").classList.add("active");
    document.querySelector("[data-target='"+_this+"']").classList.remove("d-none");
    // complaintData();
}


$(document).ready(function(e){

    var _isSuperMerchant = document.querySelector("#isSuperMerchant").value;
    if(_isSuperMerchant == "true"){
        $(".common-column").removeClass("col-md-6");
        $(".common-column").addClass("col-md-4");
    }
    
    var dateToday = new Date(); 
    $(".datepick").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : true,
        minDate: dateToday,			
    });

    function complaintData(){
        document.querySelector("body").classList.remove("loader--inactive");
        var _userType = document.querySelector("#userType").value;
        var _complaintClass = "";
        if(_userType == "ADMIN"){
            _complaintClass = "complaintId"
        }
        $("#complaintList-table").dataTable({
            "ajax" : {
                "type" : "POST",
                "url" : "viewComplaintAction",
                "data" : function(d) {
                    return generatePostData(d);
                }
            },
            "fnDrawCallback" : function(settings, json) {
                setTimeout(function(e){
                    document.querySelector("body").classList.add("loader--inactive");
                }, 500);
            },
            "destroy": true,
            "columns" : [
                { "mData" : "complaintId", "className" : _complaintClass },
                { "mData" : "merchant" },
                { "mData" : "complaintType" },
                { "mData" : "createDate" },
                { "mData" : "createdBy" },
                { "mData" : "updatedBy" },
                { 
                    "mData" : null,
                    "mRender" : function(row){
                        if(row.status == "In-Process"){
                            return "<span class='processed-status status-btn'>"+row.status+"</span>";
                        }else if (row.status == "Open"){
                            return "<span class='initiate-status status-btn'>"+row.status+"</span>";
                        }else{
                            return "<span class='complete-status status-btn'>"+row.status+"</span>";
                        }
                    }
                },
            ]
        });
    }

    complaintData();

    document.querySelector("#viewComplaint").onclick = function(e){
        complaintData();
    }

    function generatePostData(d){

        var _obj = {};
        var _allInput = document.querySelectorAll("[data-target='complaintList'] [data-var]");

        _allInput.forEach(function(index, array, element){
            _obj[index.getAttribute("data-var")] = index.value;
        })


        return _obj;

    }

    $("body").on("click", ".complaintId", function(e){
        var _complaintId = $(this).text();

        $.ajax({
            type: "POST",
            url: "viewComplaintDetailsAction",
            data: {
                "complaintId" : _complaintId
            },
            success: function(data){

                document.querySelector("#complaint_div").innerHTML = "";
                var _div = "";
                var _json = JSON.parse(data.complaint.message);

                if(_json.length > 0){
                    for(var i = 0; i < _json.length; i++){
                        var _statusClass = "";
                        var _downloadButton = "";
                        if(_json[i]['file'] == true){
                            _downloadButton = "<span class='complainer_attached'>Download</span>";
                        }else{
                            _downloadButton = "<span></span>";
                        }

                        if(_json[i]['status'] == "Open"){
                            _statusClass = "initiate-status";
                        }else if(_json[i]['status'] == "In-Process"){
                            _statusClass = "processed-status";
                        }else if(_json[i]['status'] == "Resolved"){
                            _statusClass = "complete-status";
                        }

                        _div += "<div class='lpay-popup_complaint_list mb-20' data-status='"+_json[i]['status']+"'>";
                        _div += "<div class='complaint_info'><span class='complainer_name'>"+_json[i]['name']+"</span>"+_downloadButton+"</div>";
                        _div += "<div class='complaint_msg'>"+_json[i]['comment']+"</div><div class='status-update_div'><div class='common-status "+_statusClass+"'>Status: "+_json[i]['status']+"</div><div class='lastUpdate text-right'>"+_json[i]['date']+"</div></div>";
                        _div += "</div>";
                    
                    }
                }

                document.querySelector("#complaint_div").innerHTML = _div;
                $("#complaintStatus").val(data.complaint.status);
                $("#complaintStatus").selectpicker('refresh');
                $("#complaintId-update").val(data.complaintId);
                $(".lpay-popup_complaint").fadeIn();

            }
        })
    })

    $("body").on("click", ".complainer_attached", function(e){

        document.querySelector("#download-complaintId").value = document.querySelector("#complaintId-update").value;
        document.querySelector("#download-status").value = $(this).closest(".lpay-popup_complaint_list").attr("data-status");
        document.querySelector("#fileDownloadComplaint").submit();

    })

    $("#cancelBtn").on("click", function(e){
        $(".lpay-popup_complaint").fadeOut();
    })

    $("#update-status").on('click', function(e){
        var _status = document.querySelector("#complaintStatus").value;
        var _comment = document.querySelector("#comments-update").value;
        if(_status != "" && _comment != ""){
            document.querySelector("body").classList.remove("loader--inactive");
            var _myForm = document.querySelector("#complaintUpdateAction");
            var _formData = new FormData(_myForm);
            var _files = document.querySelector("#upload-input-new-update").files;
            var _fileName = [];
            if(_files.length > 0){
                for(var i = 0; i < _files.length; i++){
                    _fileName.push(_files[i]['name']);
                }
            }
            _formData.append('fileName', _fileName.toString());

            $.ajax({
                type: "POST",
                enctype: "multipart/form-data",
                url: "updateComplaintAction",
                data: _formData,
                processData: false,
                contentType: false,
                success: function(data){

                    $(".lpay-popup_complaint").fadeOut();
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".responseMsg").text(data.responseMsg);
                    $(".lpay_popup").fadeIn();
                    document.querySelector("body").classList.add("loader--inactive");

                }
            })
        }else{
            alert("Please select comment/status");
        }
        return false;
        
    })

    $(".confirmButton").on("click", function(e){
        reset();
        complaintData();
    })

    function reset(){
        var _inputs = document.querySelectorAll("[data-var]");
        $(".default-upload").removeClass("d-none");
        $(".default-upload img").css('display', 'block');
        if(_isSuperMerchant != "true"){
            $("#subMerchant").closest(".complaint-input").addClass("d-none");
            $(".common-column").removeClass("col-md-4");
            $(".common-column").addClass("col-md-6");
        }
        $(".default-upload").closest("label").removeAttr("data-status");
        $(".lpay_upload_input").val("");
        $(".lpay_upload_input_update").val("");
        _inputs.forEach(function(index, element, array){
            var _inputs = index.localName;
            if(index.id != "dateFrom" && index.id != "dateTo"){
                if(_inputs == "select"){
                    $("#"+index.id).val('default');
                    $("#"+index.id).selectpicker('refresh');
                }else{
                    index.value = "";
                }
            }
        })
        $(".lpay_popup").fadeOut();
    }
})




// get sub merchant
function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue){
    var _merchant = _this.target.value;
    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
    if(_merchant != "" && _merchant != "ALL"){
        document.querySelector("body").classList.remove("loader--inactive");
        var data = new FormData();
        data.append('payId', _merchant);
        var _xhr = new XMLHttpRequest();
        _xhr.open('POST', _url, true);
        _xhr.onload = function(){
            if(_xhr.status === 200){
                var obj = JSON.parse(this.responseText);
                var  _option = "";
                if(_object.isSuperMerchant == true){
                    if(obj.superMerchant == true){
                        document.querySelector("#"+_subMerchantAttr).innerHTML = "";
                        _option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value="+_selectValue+">"+_selectLabel+"</option>";
                        for(var i = 0; i < obj.subMerchantList.length; i++){
                            _option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
                        }
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
                        document.querySelector("#"+_subMerchantAttr+" option[value='"+_selectValue+"']").selected = true;
                        $("#"+_subMerchantAttr).selectpicker('refresh');
                        $("#"+_subMerchantAttr).selectpicker();
                    }else{
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
                        document.querySelector("#"+_subMerchantAttr).value = "";
                    }
                }
            }
        }
        _xhr.send(data);
        setTimeout(function(e){
            document.querySelector("body").classList.add("loader--inactive");
        }, 1000);
    }else{
        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
        document.querySelector("#"+_subMerchantAttr).value = "";

    }
}

document.querySelector("#merchant").addEventListener("change", function(_this){
    getSubMerchant(_this, "getSubMerchantList", {
        isSuperMerchant : true
    }, "ALL", "ALL");
    setTimeout(function(e){
        var _checkSubMerchant = document.querySelector("#subMerchant").closest(".complaint-input").classList.toString();
        // console.log(_checkSubMerchant.indexOf("d-none"));
        if(_checkSubMerchant.indexOf("d-none") == -1){
            $(".common-column").addClass("col-md-4");
            $(".common-column").removeClass("col-md-6");
        }else{
            $(".common-column").removeClass("col-md-4"); 
            $(".common-column").addClass("col-md-6"); 
        }
    }, 500)
});


function removeError(_this){
    _this.closest(".complaint-input").classList.remove("has-error-complaint");
}

function validateFieldAndResponse(_selector, _inputs){
    var _parent = document.querySelector(_selector);
    var _inputs = _parent.querySelectorAll(_inputs);
    var _obj = {};
    _inputs.forEach(function(index, element, array){
        var _checkVisible = index.closest(".d-none");
        var _val = index.value;
        if(_checkVisible != null && _val == ""){
        }else{
            if(index.value == ""){
                index.closest(".complaint-input").classList.add("has-error-complaint");
                index.closest(".complaint-input").querySelector(".error-field").innerHTML = "Should not be blank";
            }else{
                _obj[index.getAttribute("data-var")] = index.value;
            }
        }
    })
    _obj['error'] = document.querySelector(".has-error-complaint");
    
    return _obj;
}

function registerComplaintRequest(){
    var _myForm = document.querySelector("#complaintRaiseAction");
    var _obj = validateFieldAndResponse("#complaintRaiseAction", "[data-var]");
    if(_obj.error == null){
        document.querySelector("body").classList.remove("loader--inactive");
        var _formData = new FormData(_myForm);
        var _files = document.querySelector("#upload-input-new").files;
        var _fileName = [];
        if(_files.length > 0){
            for(var i = 0; i < _files.length; i++){
                _fileName.push(_files[i]['name']);
            }
        }
       _formData.append('fileName', _fileName.toString());
        $.ajax({
            type: "POST",
            enctype: "multipart/form-data",
            url: "complaintRaiseAction",
            data: _formData,
            processData: false,
            contentType: false,
            success: function(data){
                
                if(data.complaintId != '' && data.complaintId != null){
                    $(".lpay_popup-innerbox").attr("data-status", "success");
                    $(".responseMsg").html("Your complaint has been raised successfully !<br>complaint ID: <strong>"+data.complaintId+"</strong>");
                }else{
                    $(".lpay_popup-innerbox").attr("data-status", "error");
                    $(".responseMsg").text("Something went wrong");
                }
                document.querySelector("body").classList.add("loader--inactive");
                $(".lpay_popup").fadeIn();
                
            }
        })
    }
}

$(".lpay_upload_input, .lpay_upload_input_update").on("change", function (e) {
    var _val = $(this).val();
    console.log(e);

    var _imageArr = [];
    if(e.target.files.length > 0){
        for(var i = 0; i < e.target.files.length; i++){
            _imageArr.push(e.target.files[i].name);
        }
    }
    var _fileSize = $(this)[0].files[0].size;
    var _fileType = $(this)[0].files[0].type;
    $(this).closest("label").next(".hideFields").val(_imageArr);
    if (_val != "") {
        if(_fileType == "image/png" || _fileType == "image/jpeg" || _fileType == "image/jpg" || _fileType == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" || _fileType == "application/vnd.ms-excel"){
            $("body").removeClass("loader--inactive");
            $(this).closest("label").find(".default-upload").addClass("d-none");
            $(this).closest("label").find("#placeholder_img").css({ "display": "none" });
            if (_fileSize < 2000000) {
                $(this).closest("label").attr("data-status", "success-status");
                $(this).closest("label").find(".fileName").text(_imageArr);
                setTimeout(function (e) {
                    $("body").addClass("loader--inactive");
                }, 500);
            } else {
                $(this).closest("label").attr("data-status", "error-status");
                setTimeout(function (e) {
                    $("body").addClass("loader--inactive");
                }, 500);
            }
        }else{
            $(this).closest(".single-account-input").addClass("hasError");
            $(this).closest(".single-account-input").find(".error-field").text("Invalid file type");
        }
    }
});

