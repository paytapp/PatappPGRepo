$(document).ready(function(e){
    // excel file download action
    $("#csv-download").on("click", function(e){
        var _merchant = $("#merchants").val();
        $("#payId").val(_merchant);
        $('#downloadFileForm').attr('action', 'downloadExcelAction');
        $('#downloadFileForm').submit();
    });

    // pdf download action
    $("#pdf-download").on("click", function(e){
        var _merchant = $("#merchants").val();
        $("#payId").val(_merchant);
        $('#downloadFileForm').attr('action', 'downloadPdfAction');
        $('#downloadFileForm').submit();
    });

    // submit button action 
    $("#submitBtn").on("click", function(e){
        var _merchant = $("#merchants").val();
        if(_merchant != ""){
            $("body").removeClass("loader--inactive");
            $.ajax({
                type: "post",
                url: "viewSmartRouterAction",
                data: {"payId": _merchant},
                success: function(data){
                    $(".empty-data").addClass("d-none");
                    $(".download-btn").removeClass("d-none");
                    var table = "";
                    console.log(data.routerRuleData["acquirer"]);
                    if(Object.keys(data.routerRuleData).length !== 0){
                    for(key in data.routerRuleData){
                        table += "<div class='lpay_table_wrapper'><table class='lpay_custom_table w-100'>";
                        table += "<span class='inner-heading text-primary mt-30'>"+key+"</span>";
                        table += "<tr class='lpay_table_head'><th>Acquirer</th><th>Status</th><th>Description</th><th>Mode</th><th>Payment Type</th><th>Mop Type</th><th>Allowed Fail Count</th><th>Always On</th><th>Load(%)</th><th>Priority</th><th>Retry Time</th><th>Min Txn</th><th>Max Txn</th><th>Acq Mode</th></tr>";
                        for(var i = 0; i < data.routerRuleData[key].length; i++){
                            table += "<tr>";
                            table += "<td>"+data.routerRuleData[key][i]["acquirer"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["currentlyActive"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["currentlyActive"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["mode"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["paymentType"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["mopType"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["allowedFailureCount"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["alwaysOn"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["loadPercentage"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["priority"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["retryMinutes"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["minAmount"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["maxAmount"]+"</td>";
                            table += "<td>"+data.routerRuleData[key][i]["onUsoffUsName"]+"</td>";
                            table += "</tr>";
                        }
                        table += "</table></div>";
                    }
                    $("#tableData").append(table);
                    $("#tableData").removeClass("d-none");
                    setInterval(function(e){
                        $("body").addClass("loader--inactive");
                    }, 500);
                    }else{
                        $("#tableData").html("");
                        // $("#tableData").addClass("d-none");
                        $(".download-btn").addClass("d-none");
                        $(".empty-data").removeClass("d-none");
                        setInterval(function(e){
                            $("body").addClass("loader--inactive");
                        }, 500);
                    }
                },
                error: function(data){
                    alert("Something went wrong!");
                }
            });
        }else{
            
        }
    });
});