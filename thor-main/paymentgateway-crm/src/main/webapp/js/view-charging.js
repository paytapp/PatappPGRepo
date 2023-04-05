$(document).ready(function(e){

    function downloadFile(_that){
        var _getId = _that.id;
        var _merchant = $("#merchants").val();
        $("#payIdCsv").val(_merchant);
        if(_getId == "csv-download"){
            $("[data-id=downlaodFile]").attr("action", "ExcelSheetDownloadAction");
        }
        if(_getId == "pdf-download"){
            $("[data-id=downlaodFile]").attr("action", "PdfFileDownloadAction");
        }
        $("[data-id=downlaodFile]").submit();
    }

    document.querySelector("#csv-download").onclick = function(e){
        downloadFile(this);
    }

    document.querySelector("#pdf-download").onclick = function(e){
        downloadFile(this);
    }

     // excel file download action
     $("#csv-download").on("click", function(e){
         var _merchant = $("#merchants").val();
         $("#payIdCsv").val(_merchant);
     });

     // pdf download action
     $("#pdf-download").on("click", function(e){
         var _merchant = $("#merchants").val();
         $.ajax({
             type: "post",
             url: "downloadPdfAction",
             success:function(data){

             },
             error: function(data){

             },
         })
     });

    // submit button action 
    $("#submitBtn").on("click", function(e){
        var _merchant = $("#merchants").val();
        var _acquirer = $("#acquirer").val();
        if(_merchant != ""){
            $("body").removeClass("loader--inactive");
            $.ajax({
                type: "post",
                url: "viewChargingDetailsAction",
                data: {"payId": _merchant, "acquirerType": _acquirer},
                success: function(data){
                    console.log(data);
                    $("#tableData").html("");
                    $(".noData").addClass("d-none");
                    $(".download-btn").removeClass("d-none");
                    var table = "";
                    // console.log(data.chargingDetailsData["acquirer"]);
                    if(Object.keys(data.chargingDetailsData).length !== 0){
                    for(key in data.chargingDetailsData){
                        
                        table += "<span class='inner-heading d-b lpay_h3 mt-20 text-heading'>"+key+"</span>";
                        console.log(key);
                        // console.log(data.chargingDetailsData[key][0]);
                        for(var i = 0; i < data.chargingDetailsData[key].length; i++){
                            //console.log(data.chargingDetailsData[key][i]);
                            for(key2 in data.chargingDetailsData[key][i]){
                                table += "<div class='lpay_table_wrapper mt-20'><table class='lpay_custom_table'>";
                                table += "<span class='inner-heading'>"+key2+"</span>";
                                table += "<tr class='lpay_table_head'><th>Merchant</th><th>Currency</th><th>Mop</th><th>Transaction</th><th>PG TDR</th><th>PG FC</th><th>Bank TDR</th><th>Bank FC</th><th>Reseller TDR</th><th>Reseller FC</th><th>Merchant TDR</th><th>Merchant FC</th><th>Merchant GST</th><th>Min Txn</th><th>Max Txn</th><th>Max Charge Merchant</th><th>Max Charge Acquirer</th></tr>";
                                // console.log(data.chargingDetailsData[key][i][key2].length);
                                
                                for(var j = 0; j < data.chargingDetailsData[key][i][key2].length; j++){
                                    table += "<tr>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["businessName"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["currency"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["mopType"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["transactionType"]+"</td>";
                                    /* table += "<td>"+data.chargingDetailsData[key][i][key2][j]["slabId"]+"</td>"; */
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["pgTDR"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["pgFixCharge"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["bankTDR"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["bankFixCharge"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["resellerTDR"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["resellerFixCharge"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["merchantTDR"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["merchantFixCharge"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["merchantServiceTax"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["minTxnAmount"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["maxTxnAmount"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["maxChargeMerchant"]+"</td>";
                                    table += "<td>"+data.chargingDetailsData[key][i][key2][j]["maxChargeAcquirer"]+"</td>";
                                    /* table += "<td>"+data.chargingDetailsData[key][i][key2][j]["acquiringMode"]+"</td>"; */
                                    table += "</tr>";
                                }
                            table += "</table></div>";
                            }

                        }
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