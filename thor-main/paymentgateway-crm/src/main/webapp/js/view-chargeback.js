
    const dateValidate = _ => {
        var date = new Date();
        // var currentMonth = date.getMonth() + 1;
        // var currentDate = date.getDate() + "-" + currentMonth + "-" + date.getFullYear();
        var targetDateStr = document.getElementById('targetDate').value;

        var targetDateArr = targetDateStr.split('-'),
            $ddTarget = targetDateArr[0],
            $mmTarget = targetDateArr[1],
            $yearTimeTarget = targetDateArr[2].split(" "),
            $yyTarget = $yearTimeTarget[0],
            $timeTarget = $yearTimeTarget[1].split(":"),
            $hourTarget = $timeTarget[0],
            $minuteTarget = $timeTarget[1],
            $secondTarget = $timeTarget[2];

        // var currentDateArr = currentDate.split('-');

        var targetDate = new Date($yyTarget, Number($mmTarget) -1, $ddTarget, $hourTarget, $minuteTarget, $secondTarget); //Year, Month, Date

        // currentDate = new Date(date.getFullYear(), date.getMonth() - 1, currentDateArr[0]); //Year, Month, Date

        // console.log(currentDate);

        var oneDay = 24*60*60*1000; // hours*minutes*seconds*milliseconds
        var diffDays =  (targetDate.getTime() - date.getTime() ) / oneDay;

        console.log(diffDays);

        if(diffDays >= 0) {
            document.getElementById("status-btn-box").style.display = "block";
        }
    }

    dateValidate();

    $(document).ready(function() {
        $(".date-field").each(function() {
            var _date = $(this).text();
            
            _date = _date.trim();
            _date = _date.replace(" PM.000", "");
            _date = _date.replaceAll("/", "-");

            $(this).text(_date);

            if(!$(this).hasClass("target-date")) {
                var tempDate = $(this).text();
                tempDate = tempDate.split("-");

                var _month = Number(tempDate[0]) < 10 ? "0" + tempDate[0] : tempDate[0],
                    _day = Number(tempDate[1]) < 10 ? "0" + tempDate[1] : tempDate[1];
                
                tempDate = tempDate[2].split(" ");
                var _year = tempDate[0];

                //     _time = tempDate[1].split(":"),
                //     _hours = Number(_time[0]) < 10 ? "0" + _time[0] : _time[0],
                //     _minutes = Number(_time[1]) < 10 ? "0" + _time[1] : _time[1],
                //     _seconds = Number(_time[2]) < 10 ? "0" + _time[2] : _time[2];

                // _time = _hours + ":" + _minutes + ":" + _seconds;

                tempDate = "20" + _year + "-" + _month + "-" + _day + " " + tempDate[1];

                $(this).text(tempDate);
            }
        });

        // CHANGE FORMAT FOR TARGET DATE
        var _targetDate = $(".target-date").text();
        _targetDate = _targetDate.split("-");
        _targetDate = _targetDate[2] + "-" + _targetDate[1] + "-" + _targetDate[0] + " 23:59:59";
        $(".target-date").text(_targetDate);

        $(".download-file").on("click", function(e) {
            e.preventDefault();
            var documentId = $(this).attr("data-document");         
            var fileName = $(this).attr("data-filename");
            var btnName = $(this).attr("data-btnname");
            var action = $(this).attr("data-action");
            $("#downloadFile input[name='documentId']").val(documentId);
            $("#downloadFile input[name='imageName']").val(fileName);
            $("#downloadFile input[name='downloadBtnName']").val(btnName);
            $("#downloadFile").attr("action", action);
            $("#downloadFile").submit();
        });

        function renderTable() {
            var chargebackListArr = ["updateDateString", "chargebackStatus"];
            $.ajax({
                type: "POST",
                url: "reloadChargebackDetails",
                timeout: 0,
                data: {
                    caseId : document.getElementById("caseId").value,
                },
                success: function(data) {
                    var chargebackList = data.chargebacklist,
                        detailBox = "",
                        _dateStr;
                    chargebackList.forEach(function(item) {
                        detailBox += '<tr>';

                        for(var i = 0; i < chargebackListArr.length; i++) {
                            if(i == 0) {
                                detailBox += '<td>'+ item[chargebackListArr[0]] +'</td>';
                            } else if(i == 1) {
                                var str = item[chargebackListArr[1]].split(" "),
                                    status = str[0],
                                    statusBy = str[2],
                                    userType = $("#chargebackUserType").val();
                                
                                if(userType == "MERCHANT" || userType == "SUBUSER") {
                                    if(status == "Accepted" && (statusBy == "merchant" || statusBy == "subuser" || statusBy == "SubMerchant" || statusBy == "SuperMerchant")) {
                                        detailBox += '<td>'+ status +'</td><td>'+ status +'</td><td></td>';
                                        $("#reloadChargebackDetails").addClass("hidden--active");
                                    } else if(status == "Rejected" && (statusBy == "merchant" || statusBy == "subuser" || statusBy == "SubMerchant" || statusBy == "SuperMerchant")) {
                                        detailBox += '<td>'+ status +'</td><td></td><td></td>';
                                        $("#reloadChargebackDetails").addClass("hidden--active");
                                    } else if(status == "Accepted" && (statusBy == "admin" || statusBy == "subadmin")) {
                                        detailBox += '<td></td><td>'+ status +'</td><td></td>';
                                        $("#reloadChargebackDetails").addClass("hidden--active");
                                    } else if(status == "Rejected" && (statusBy == "admin" || statusBy == "subadmin")) {
                                        detailBox += '<td></td><td>'+ status +'</td><td></td>';
                                        $("#reloadChargebackDetails").addClass("hidden--active");
                                    } else if(status == "Refunded") {
                                        detailBox += '<td>'+ status +'</td><td>'+ status +'</td><td></td>';
                                        $("#reloadChargebackDetails").addClass("hidden--active");
                                    } else if(status == "Closed") {
                                        detailBox += '<td>'+ status +'</td><td>'+ status +'</td><td></td>';
                                        $("#reloadChargebackDetails").addClass("hidden--active");
                                    } else if(status == "New") {
                                        detailBox += '<td>'+ status +'</td><td>'+ status +'</td><td></td>';
                                        // $("#reloadChargebackDetails").addClass("d-none");
                                    }
                                }

                                if(userType == "ADMIN" || userType == "SUBADMIN") {
                                    if(status == "Accepted" && (statusBy == "merchant" || statusBy == "subuser" || statusBy == "SubMerchant" || statusBy == "SuperMerchant")) {
                                        detailBox += '<td>'+ status +'</td><td>'+ status +'</td><td><div class="action-btns refund-action-btn"><a href="#" class="btnRefund">Refund</a></div></td>';
                                    } else if(status == "Rejected" && (statusBy == "merchant" || statusBy == "subuser" || statusBy == "SubMerchant" || statusBy == "SuperMerchant" )) {
                                        detailBox += '<td>'+ status +'</td><td></td><td><div class="action-btns accept-reject-btns"><a href="#" name="btnAccept" onclick="updateStatus(this,event)">Accept</a><a href="#" name="btnReject" onclick="updateStatus(this,event)">Reject</a></div></td>';
                                    } else if(status == "Accepted" && (statusBy == "admin" || statusBy == "subadmin")) {
                                        detailBox += '<td></td><td>'+ status +'</td><td><div class="action-btns refund-action-btn"><a href="#" class="btnRefund">Refund</a></div></td>';
                                    } else if(status == "Rejected" && (statusBy == "admin" || statusBy == "subadmin")) {
                                        detailBox += '<td></td><td>'+ status +'</td><td></td>';
                                        $("#reloadChargebackDetails").addClass("hidden--active");
                                    } else if(status == "Refunded") {
                                        detailBox += '<td>'+ status +'</td><td>'+ status +'</td><td></td>';
                                        $("#reloadChargebackDetails").addClass("hidden--active");
                                    } else if(status == "Closed") {
                                        detailBox += '<td>'+ status +'</td><td>'+ status +'</td><td></td>';
                                        $("#reloadChargebackDetails").addClass("hidden--active");
                                    } else if(status == "New") {
                                        detailBox += '<td>'+ status +'</td><td>'+ status +'</td><td></td>';
                                        // $("#reloadChargebackDetails").addClass("d-none");
                                    }
                                }
                            }
                        }
                        
                        
                        detailBox += '</tr>';
                    });

                    console.log(detailBox);

                    $("#reloadChargebackDetails tbody").html("");
                    $(detailBox).appendTo("#reloadChargebackDetails tbody");

                    $("#reloadChargebackDetails > tbody > tr").each(function() {
                        if($(this).find("td").eq(1).text() == "New" || $(this).find("td").eq(2).text() == "New") {
                            $(this).remove();
                        }

                        if($(this).find("td").eq(2).text() == "Accepted") {
                            $(".accept-reject-btns").addClass("d-none");
                        }

                        if($(this).find("td").eq(3).text() == "Closed") {
                            $(".refund-action-btn").addClass("d-none");
                        }
                    });

                    if($(".refund-action-btn").hasClass("active")) {
                        $(".accept-reject-btns").removeClass("active");
                    }
                },
                error: function(data) {
                }
            });
        }
        
        renderTable();

        $("body").on("click", ".btnRefund", function(e) {
            e.preventDefault();
            $("#manualRefundProcessChargeback").submit();
        });

        if ($(document.getElementById("caseStatus")).length){
            $(caseStatus).find("option").eq(0).remove();
        }

        $("#commentId").on("keyup", function(e) {
            var _val = $(this).val();
            
            if(_val == "") {
                $("#error-commentId").removeClass("invisible");
            } else {
                $("#error-commentId").addClass("invisible");
            }
        });

        $("form#files").submit(function(e) {
            e.preventDefault();

            var _comment = document.getElementById("commentId").value;

            if(_comment == "") {
                document.getElementById("error-commentId").classList.remove("invisible");
                return false;
            } else {
                var isFileUploaded = document.querySelector(".lpay-upload").getAttribute("data-status");
                if(isFileUploaded == "error-status") {
                    return false;
                } else {
                    $("body").removeClass("loader--inactive");

                    var token = document.getElementsByName("token")[0].value;
                    document.getElementById("tokenId").value = token;
                    document.getElementById("commentPost").value = _comment;
                    
                    var formData = new FormData($(this)[0]);
                    
                    $.ajax({
                        url : 'chargebackCommentCreaterAction',
                        type : 'post',
                        timeout: 0,                        
                        data: formData,
                        async: false,   
                        contentType: false,  
                        processData: false,
                        success : function(data) {
                            var responseDiv = document.getElementById("response");
                                responseDiv.innerHTML = data.response;
                                responseDiv.style.display = "block";
                            var responseData = data.response;
                            
                            if(responseData == null) {                          
                                alert("Details not updated.");  
                                window.location.reload();
                            } else {
                                // var commentFetchTextField = document.getElementById("allComments"); 
                                //     responseDiv.className = "success success-text";             
                                alert("Comment added Successfully.");
                                window.location.reload();
                            }
                        },
                        error : function(data) {
                            // var responseDiv = document.getElementById("response");
                            // responseDiv.innerHTML = "Details not updated.!!"
                            // responseDiv.style.display = "block";
                            // responseDiv.className = "error error-new-text";             

                            alert("Details not updated!");
                            window.location.reload();
                        },
                        processData: false,  // Important!
                        contentType: false,
                        cache: false,
                    });
                }
                
                return true;
            }
        });

        // validateFileUpload({that: this, maxLimit: 5})

        var _result = "";
        
        $(".lpay_upload_input").on("change", function(e) {
            $("body").removeClass("loader--inactive");

            var _that = $(this),
                _val = _that.val(),
                _label = _that.closest(".lpay-upload");

            if(_val != "") {
                setTimeout(function() {
                    $(".default-upload").addClass("d-none");

                    var result = validateFileUpload({
                        that: _that,
                        maxLimit: 5
                    });

                    if(result) {
                        _label.attr("data-status", "success-status");
                        $("#filename-success").text(_result);
                        $("body").addClass("loader--inactive");
                    } else {                        
                        _label.attr("data-status", "error-status");
                        $("#filename-error").text(_result);
                        $("body").addClass("loader--inactive");
                    }
                }, 500);
            } else {
                setTimeout(() => {
                    $(".default-upload").removeClass("d-none");
                    _label.attr("data-status", "");
                    
                    $("body").addClass("loader--inactive");
                }, 500);
            }
        });

        var validateFileUpload = function(obj) {
            var _that = obj.that;
            var _files = _that[0].files;
    
            if(_that.value !== "") {
                for(var i = 0; i < _files.length; i++) {
                    var fileName = _files[i].name;
                    var _size = _files[i].size / 1024;
                    var _maxSize = 1024 * obj.maxLimit;
                    var fileExtension = fileName.split('.').pop().toLowerCase();
        
                    if(_size > _maxSize) {
                        _result = "File cannot be greater than "+ _maxSize / 1024 +" mb";
                        return false;
                    } else if(fileExtension == "" || fileExtension == "csv" || fileExtension == "pdf") {
                        if(_files.length > 1) {
                            _result = _files.length + " files selected.";
                        } else {
                            _result = fileName;
                        }

                        if(i == _files.length - 1) {
                            return true;
                        }
                    } else {
                        _result = "Wrong file format.";
                        return false;
                    }            
                }
            } else {
                return "File not selected.";
            }
        }
    });