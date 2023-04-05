
$(document).ready(function() {
    // DOMESTIC PAYMENT TYPES
    var domesticObj = {
        "Credit Card" :   "CC",
        "Debit Card" : "DC",
        "Net Banking" : "NB",
        "Wallet" : "WL",
        "EMI CC" : "EMCC",
        "EMI DC" : "EMDC",
        "UPI" : "UP",
        "COD": "CD"
    };

    // INTERNATIONAL PAYMENT TYPE
    var internationalObj = {
        "Credit Card" :   "CC",
        "Debit Card" : "DC"
    };

    var fetchData = function(data) {

        let tableWrapper = $("#paymentOptions-info");
        
        tableWrapper.find("table").text("");
        let detailBox = "";

        if(data.length > 0) {
            // tableWrapper.find("table").removeClass("d-none");
            // errorMsgWrapper.addClass("d-none");
            
            $("#wwgrp_payId").find(".selectpicker").selectpicker("deselectAll");
            $("#wwgrp_payId").find(".selectpicker").selectpicker("refresh");

            $("#txnType").selectpicker("deselectAll");
            $("#txnType").selectpicker("refresh");
            
            // create th 
            detailBox += "<thead class='lpay_table_head'><tr><th>Discount Applicable</th><th>Dicount</th><th>Discount Type</th><th>Payment Type</th><th>Issuer Bank</th><th>Mop Type</th><th>Payment Region</th><th>Card Holder Type</th><th>Slab</th><th>EMI Duration</th><th>Fixed Charge</th><th>Percentage Amount</th><th>Action</th></tr></thead>";

            for(let i = 0; i < data.length; i++) {
                detailBox += '<tr>'; // ROW START

                // MERCHANT NAME
                detailBox += '<td class=""><span>'+ data[i]["discountApplicableOn"] +'</span><input type="hidden" name="discountApplicableOn" value="'+ data[i]["discountApplicableOn"] +'"></td>';                

                 // MERCHANT
                 detailBox += '<td class=""><span>'+ data[i]["discount"] +'</span><input type="hidden" name="discount" value="'+ data[i]["discount"] +'"></td>';

                 // DISCOUNT TYPE
                 detailBox += '<td class=""><span>'+ data[i]["discountType"] +'</span><input type="hidden" name="discountType" value="'+ data[i]["discountType"] +'"></td>';

                // TXN TYPE
                detailBox += '<td class=""><span>'+ data[i]["paymentType"] +'</span><input type="hidden" name="paymentType" value="'+ data[i]["paymentType"] +'"></td>';
                
                // MOP TYPE
                detailBox += '<td class=""><span>'+ data[i]["issuerBank"] +'</span><input type="hidden" name="issuerBank" value="'+ data[i]["issuerBank"] +'"></td>';

                // PAYMENT TYPE
                detailBox += '<td class=""><span>'+ data[i]["mopType"] +'</span><input type="hidden" name="mopType" value="'+ data[i]["mopType"] +'"></td>';

                // PAYMENT REGION
                detailBox += '<td class=""><span>'+ data[i]["paymentRegion"] +'</span><input type="hidden" name="paymentRegion" value="'+ data[i]["paymentRegion"] +'"></td>';

                 // CARD HOLDER TYPE
                 detailBox += '<td class=""><span>'+ data[i]["cardHolderType"] +'</span><input type="hidden" name="cardHolderType" value="'+ data[i]["cardHolderType"] +'"></td>';

                  // SLAB
                detailBox += '<td class=""><span>'+ data[i]["slab"] +'</span><input type="hidden" name="amountSlab" value="'+ data[i]["slab"] +'"></td>';

                // SLAB
                detailBox += '<td class=""><span>'+ data[i]["emiDuration"] +'</span><input type="hidden" name="emiDuration" value="'+ data[i]["emiDuration"] +'"></td>';

                // FIXED CHARGES
                detailBox += '<td class=""><span>'+ data[i]["fixedCharges"] +'</span><input type="hidden" name="fixedCharges" value="'+ data[i]["fixedCharges"] +'"><input type="text" class="input-edit max-width-100 form-control py-5 px-5 height-auto font-size-14 percent-amount" data-id="fixedCharges" onkeypress="onlyDigit(event)" value="'+ data[i]["fixedCharges"] +'"></td>';

                // PERCENTAGE
                detailBox += '<td class=""><span>'+ data[i]["percentageCharges"] +'</span><input type="hidden" name="percentageCharges" value="'+ data[i]["percentageCharges"] +'"><input type="text" data-id="percentageCharges" class="input-edit form-control py-5 px-5 height-auto font-size-14 max-width-100 percent-amount" onkeypress="onlyDigit(event)" value="'+ data[i]["percentageCharges"] +'"></td>';

                // ACTION COLUMN
                detailBox += '<td><div class="actionBtns visual-mode"><a  href="javascript:;" class="btn-edit bg-color-primary-light-2 color-primary hover-color-primary border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-pencil-alt"></i></a><a href="javascript;"  class="btn-delete bg-color-red-lightest color-red hover-color-red border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-trash"></i></a></div><div class="actionBtns edit-mode"><a href="#" class="btn-save bg-color-green-lightest color-green hover-color-green border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-check"></i></a><a href="#" class="btn-cancel bg-color-red-lightest color-red hover-color-red border-radius-6 font-size-15 d-flex align-items-center justify-content-center"><i class="fa fa-times"></i></a></div></td>';
                detailBox += '</tr>';
            }
        } else {
            detailBox += '<tr><td colspan="7" class="color-red text-center font-size-14">No data avaiable</td></tr>';
        }

        $(detailBox).appendTo(tableWrapper.find(".discount-table"));
        loadTableData();
    }

    var showAllPaymentOptions = function(payId, txnType, paymentType, paymentRegion, url) {
        $("body").removeClass("loader--inactive");
        var _checkbox = $("#payment-options input[type='checkbox']");
        _checkbox.removeAttr("checked");

        let token  = document.getElementsByName("token")[0].value;

        $.ajax({
            type: "post",
            url: url,
            data: {
                payId: payId,
                txnType: txnType,
                paymentType: paymentType,
                paymentRegion: paymentRegion,
                "token": token,
                "struts.token.name": "token"
            },
            success: function(data){
                // showAllPaymentOptions(payId, "sufDetailAction");
                fetchData(data.aaData);
                
                setTimeout(function() {
                    $("body").addClass("loader--inactive");                    
                }, 1000);
            },
            error: function(data) {
                alert("Try again, Something went wrong!");
            }
        });

    }

    

    // CREATE / REMOVE PAYMENT OPTION
    $("#btn-sufDetails").on("click", function(e) {
        e.preventDefault();
        var dataObj = {};
        var _checked = false;
        var _slabCheck = $("#amountSlab").val();
        var _perCharges = $("#percentageCharges").val();
        var _fixCharges = $("#fixedCharges").val();
        if(_slabCheck != ""){
            if(_perCharges != "" || _fixCharges != ""){
                if(_perCharges == ""){
                    $("#percentageCharges").val("0.00");
                }
                if(_fixCharges == ""){
                    $("#fixedCharges").val("0.00");
                }
            }
        }

        $(".active .discount-input[name]").each(function(e){
            var _key = $(this).attr("id");
            var _val = $(this).val();
            var _type = typeof _val;
            if(_val != "" && _val != null){
                if(_type == "object"){
                    if(_val != null){
                        dataObj[_key] = $("#"+_key).val().join();
                    }
                }else{
                    dataObj[_key] = _val;
                }
                _checked = true;
                return true;
            }else{
                var _getText = $("#"+_key).attr("data-text");
                alert("Please Enter "+_getText+"");
                _checked = false;
                return false
            }
        });
        console.log(dataObj);
        if(_checked == true){
        $("body").removeClass("loader--inactive");
            $.ajax({
                type: "POST",
                url: "discountDetailSubmitAction",
                data: dataObj,
                success: function(data){
                    console.log(data);
                    if(data.activeDiscountDetails == "success"){
                        window.scroll(0, 0);
                        $(".discount-filter-div .col-md-3").addClass("d-none");
                        $(".discount-applicable").removeClass("d-none").addClass("active");
                        $("#discountApplicableOn").selectpicker('refresh');
                        $(".submit-wrapper").addClass("d-none");
                        $(".discount-filter-div .lpay_success").text(data.response);
                        $(".discount-filter-div .lpay_success").removeClass("d-none");
                        // $(".selectpicker").selectpicker('destroy');
                        $(".discount-filter-div .selectpicker").val("");
                        $(".form-control").val("");
                        $(".selectpicker").selectpicker('refresh');
                        fetchData(data.aaData);
                        setTimeout(function(e){
                            $(".discount-filter-div .lpay_success").addClass("d-none");
                        }, 3000);
                        setTimeout(function(e){
                            $("body").addClass("loader--inactive");
                        }, 500);
                    }
                },
                error: function(data){
                    alert("Something went wrong");
                $("body").removeClass("loader--inactive");
                    // console.log(data);
                }
            });
        }
    });

    // SELECT ALL OPTIONS
    $("#selectAll").on("change", function(e) {
        var status = $(this).is(":checked");
        var _checkbox = $("#payment-options input[type='checkbox']");
        if(status == true) {
            _checkbox.attr('checked', 'checked');
        } else {
            _checkbox.removeAttr('checked');
        }
    });

    var table = "#discountDataTable";

    function loadTableData(){
        $(".pagination-wrapper").html("");
        // var _getRow = $("#maxRows").val();
        $("#current-show").text($("#maxRows").val());
        
        var _trNum = 0;
        // var _maxRows = parseInt($("#maxRows").val());
        var _maxRows = 20;
        var _totalRows = $("#discountDataTable tbody tr").length;
        $("#totalEntries").text(_totalRows);
        $(table+" tr:gt(0)").each(function(e){
            _trNum++;
            if(_trNum > _maxRows){
                $(this).hide();
            }
            if(_trNum <= _maxRows){
                $(this).show();
            }
        });
        if(_totalRows > _maxRows){
            var _pageNum = Math.ceil(_totalRows/_maxRows);
            console.log(_pageNum);
            for(var i = 1; i <= _pageNum;){
                $(".pagination-wrapper").append("<span data-page="+i+">"+i++ +"</span>");
            }   
        }
        $(".pagination-wrapper span:first-child").addClass("page-active");
        $(".pagination-wrapper span").on("click", function(e){
            var _pageNumber = $(this).attr("data-page");
            $("#current-show").text($("#maxRows").val());
            var _trIndex = 0;
            $(".pagination-wrapper span").removeClass("page-active");
            $(this).addClass("page-active");
            $(table+" tr:gt(0)").each(function(e){
                _trIndex++;
                if(_trIndex > (_maxRows*_pageNumber) || _trIndex <= ((_maxRows*_pageNumber)-_maxRows)){
                    $(this).hide();
                }else{
                    $(this).show();
                }
            })
        })
    }

    setTimeout(function(e){
        loadTableData();
    }, 1000);

    

    // CANCEL EDITABLE ROW
    $("body").on("click", ".btn-cancel", function(e) {
        e.preventDefault();
        $(this).closest("tr").removeClass("editable--active");
    });


    // SAVE ROW
    $("body").on("click", ".btn-save", function(e) {
        e.preventDefault();
        $("body").removeClass("loader--inactive");
        $(".editable--active input[name=fixedCharges]").val($("[data-id=fixedCharges]").val());
        $(".editable--active input[name=percentageCharges]").val($("[data-id=percentageCharges]").val());
        var dataObj = {};
        $(".editable--active input[type=hidden]").each(function(e){
            var _key = $(this).attr("name");
            var _val = $(this).val();
            dataObj[_key] = _val;
        })
        console.log(dataObj);
        $.ajax({
            type: "POST",
            url: "editDiscountDetailAction",
            data: dataObj,
            success: function(data){
                console.log(data);
                window.scroll(0, 0);
                $(".discount-filter-div .success-text").text(data.response);
                $(".discount-filter-div .success-text").removeClass("d-none");
                setTimeout(function(e){
                    $(".discount-filter-div .success-text").addClass("d-none");
                }, 3000);
                fetchData(data.aaData);
            },
            error: function(data){
                alert("Something went wrong");
            }
        });
        $(this).closest("tr").removeClass("editable--active");
        setTimeout(function(){
            $("body").addClass("loader--inactive");
        }, 500);
    });

    // DELETE ROW
    $("body").on("click", ".btn-delete", function(e) {
        e.preventDefault();
        $(this).closest("tr").addClass("delete--active");
        $("#fancybox").fancybox({
            'overlayShow': true
        }).trigger('click');
    });

    function deleteRow(){
        $("body").removeClass("loader--inactive");
            var dataObj = {};
            $(".delete--active input[type=hidden]").each(function(e){
                var _key = $(this).attr("name");
                var _val = $(this).val();
                dataObj[_key] = _val;
            })
            console.log(dataObj);
            $.ajax({
                type: "POST",
                url: "deleteDiscountDetailAction",
                data: dataObj,
                success: function(data){
                    window.scroll(0,0);
                    fetchData(data.aaData);
                    $(".discount-filter-div .success-text").text(data.response);
                    $(".discount-filter-div .success-text").removeClass("d-none");
                    setTimeout(function(e){
                        $(".discount-filter-div .success-text").addClass("d-none");
                    }, 3000);
                    setTimeout(function(e){
                        $("body").addClass("loader--inactive");
                    }, 500);
                },
                error: function(data){
                    alert("Something went wrong");
                }
            });
        
    }

    $("body").on("click", "#confirm-btn", function(e){
        
        deleteRow();
        $.fancybox.close();
          
    });

    $("body").on("click", "#cancel-btn", function(e){
        $.fancybox.close();
    })
    
    // function editDeleteFunc(url){
    //     var allList = $("#hidden-content").find("li");
    //     var paymentType = [];
        
    //     var dataObj = {
    //         payId : $("#hidden-content").find("input[name='edit-pay-id']").val(),
    //         merchantName : $("#hidden-content").find("input[name='edit-merchant']").val(),
    //         txnType : $("#hidden-content").find("input[name='edit-txntype']").val(),
    //         paymentType: paymentType,
    //     };
    //     allList.each(function(e) {
    //         var _checkbox = $(this).find("input[type='checkbox']");
    //         var isChecked = _checkbox.is(":checked");

    //         if(isChecked) {
    //             var _key = _checkbox.val();
    //             var _val = $(this).find(".detail-input").val()+","+ $(this).find(".detail-input-per").val();
                
    //             dataObj[_key] = _val;
    //             paymentType.push(_key +"="+ _val);
    //         }
    //     });
    


    //     $.ajax({
    //         type: "post",
    //         url: url,
    //         data: dataObj,
    //         success: function(data){
    //         }
    //     })
    // }



    // insdustory category
    function inustoryCategory(data){
        var _selectWrapper = $("[data-id=industryCategory]");
        for (var i = 0; i < data.length; i++){  
            $(_selectWrapper).append("<option value="+data[i]+">"+data[i]+"</option>");
        }
        $(_selectWrapper).selectpicker("refresh");
    }

    // slab select
    function slab(data){
        var _selectWrapper = $("#amountSlab");
        _selectWrapper.append("<option value=ALL>ALL</option>");
        for(var i = 0; i < data.length; i++){
            $(_selectWrapper).append("<option value="+data[i]+">"+data[i]+"</option>");
        }
        $(_selectWrapper).selectpicker("refresh");

    }

    $("#saveEdit").click(function(e) {
        e.preventDefault();
        editDeleteFunc("sufDetailEditAction");
    });

    $("body").on("change", ".suf-edit-check", function(){
        if($(this).find("input[type='checkbox']").is(":checked")){
            $(this).nextAll("input").removeAttr("readonly");
        }else{
            $(this).nextAll("input").attr("readonly", true);
        }
    });

    $("body").on("click", ".btn-edit", function(e) {
        $(this).closest("tr").addClass("editable--active");
    });


    // radio button show data
    $("#discountApplicableOn").on("change", function(e){
        var _getDiscountApplicable = $(this).val();
        $("[data-active]").addClass("d-none");
        $("[data-active]").removeClass("active");
        $("[data-active=discountType]").removeClass("d-none");
        $("[data-active=discountType]").addClass("active");
        $(["[data-active] [name=discount]"]).attr("id", "");
        $("[data-active="+_getDiscountApplicable+"]").addClass("active");
        $("[data-active="+_getDiscountApplicable+"]").find("select").attr("id", "discount");
        $("[data-active="+_getDiscountApplicable+"]").removeClass("d-none");
        $("[data-active] select").val(" ");
        $("[data-active] select").selectpicker('refresh');
        $("[data-active] input").val("");
        $(".submit-wrapper").addClass("d-none");
    });

    $("#discountApplicableOnFilter").on("change", function(e){
        var _getDiscountApplicable = $(this).val();
        $("[data-filter]").addClass("d-none");
        $("[data-filter]").removeClass("active-filter");
        $("[data-filter=discountType]").removeClass("d-none");
        $("[data-filter=discountType]").addClass("active-filter");
        $("[data-filter="+_getDiscountApplicable+"]").addClass("active-filter");
        $("[data-filter="+_getDiscountApplicable+"]").removeClass("d-none");
    });

    // discount input filter

    $(".discount-input-filter").on("change", function(e){
        var dataObj = {};
        var nullCheck = false;
        $(".active-filter").each(function(e){
            var _key = $(this).find(".discount-input-filter").attr("name");
            var _val = $(this).find(".discount-input-filter").val();
            dataObj[_key] = _val;
            if(dataObj[_key] == null || dataObj[_key] == ""){
                nullCheck = false;
                return false;
            }else{
                nullCheck = true;
            }
        });
        if(nullCheck == true){
            $.ajax({
                type: "POST",
                url: "filteredDiscountDetailAction",
                data : dataObj,
                success: function(data){
                    fetchData(data.aaData);
                },
                error: function(data){
                    alert("Something went wrong.")
                }
            })
        }
    })

    // show payment region type
    $("#discountType").on("change", function(e){
        var _getDiscountType = $(this).val();
        if(_getDiscountType == ""){
            $("[data-active=paymentRegion]").addClass("d-none");
            $("[data-active=paymentRegion]").removeClass("active");
        }else{
            $("[data-active=paymentRegion]").addClass("active");
            $("[data-active=paymentRegion]").removeClass("d-none");
        }
    })

    // FILTER DATA
    $("body").on("change", ".filter_suf", function(e) {
        var payId = $("#filter-payId").val();
        var txnType = $("#filter_txnType").val();
        var paymentType = $("#filter_paymentType").val();
        var paymentRegion = $("#filter_paymentRegion").val();
       

        if(payId != "" && txnType != "" && paymentType != "" && paymentRegion != "") {
            showAllPaymentOptions(payId, txnType, paymentType, paymentRegion, "sufDetailAction");
        }
        
    });
    
    var fetchMopList = function(mopList) {
        var mopListStr = "";
        if(mopList !== undefined) {
            if(mopList.length > 0) {
                // mopListStr += "<option id=new value="+mopList+">All</option>";
                mopList.forEach(function(mopType) {
                    mopListStr += "<option value="+mopType+">"+mopType+"</option>";
                });
            } else {
                $("#mopType").html("");
                $("label[for='selectAll']").addClass("d-none");
            }
        } else {
            mopListStr = '<li class="col-xs-12 color-red">No data available</li>';
            $("label[for='selectAll']").addClass("d-none");
        }
        $("#mopType").html("");
        $(mopListStr).appendTo("#mopType");
        $("#mopType").selectpicker("refresh");
        $("[data-id=mopType]").addClass("active");
        $("[data-id=mopType]").removeClass("d-none");
        setTimeout(function() {
            $("body").addClass("loader--inactive");
        }, 1000);
    }

    $("body").on("click", "#mopType", function(e){
        $('#payment-options').selectpicker('selectAll');
    });

  
    $("#payId, #paymentType").on("change",function() {
      
        var paymentType = $("#paymentType").val();

        if(paymentType !== "") {
            $("body").removeClass("loader--inactive");
            let token  = document.getElementsByName("token")[0].value;
    
            $.ajax({
                type: "POST",
                url: "getSufDetailMopTypeAction",
                data: {
                    paymentType : paymentType,
                    "token": token,
                    "struts.token.name": "token"
                },
                success: function(data) {
                    console.log(data.mopList);
                   fetchMopList(data.mopList); 
                },
                error: function(data) {
                    alert("Something went wrong!");
                },
				input: function(data) {
                   // Do NOthing
                }
            });
        } else {
            $("#moptype-wrapper").addClass("d-none");
        }
    });

    // ADD SELECT OPTION
    var _addSelectOption = function(reffObj) {
        var opt = document.createElement("option");
        opt.appendChild(document.createTextNode(reffObj.text));
        opt.value = reffObj.value;
        document.getElementById(reffObj.element).appendChild(opt);
    }

    var updateSelection = function(obj) {
        
        _addSelectOption({
            element: obj.element,
            value: "",
            text: "Select Payment Type"
        });
        for(var key in obj.data) {
            console.log(obj);
            _addSelectOption({
                element: obj.element,
                value: obj.data[key],
                text: key
            });
        }
        $("#" + obj.element).selectpicker("refresh");
    }

    $("#paymentRegion, #filter_paymentRegion").on("change", function(e) {
        var _val = $(this).val();
        var getId = $(this).attr("id");
        var pmntType = null;

        if(getId == "paymentRegion") {
            pmntType = "paymentType";
        } else if(getId == "filter_paymentRegion") {
            pmntType = "filter_paymentType"
        }
        
        if(_val !== "") {
            var _paymentType = $("#" + pmntType);
            _paymentType.html("");

            if(pmntType == "paymentType") {
                $("#suf-paymentType").addClass("active");
                $("#suf-paymentType").removeClass("d-none");
            } else if(pmntType == "filter_paymentType") {
                $("#filter-paymentType").removeClass("d-none");
            }


            if(_val == "Domestic") {                
                updateSelection({
                    element: pmntType,
                    data: domesticObj
                });
            } else if(_val == "International") {
                updateSelection({
                    element: pmntType,
                    data: internationalObj
                });
            }

            if(pmntType == "paymentType") {
                if(_paymentType.val() == "") {
                    $("#moptype-wrapper").addClass("d-none");
                }
            }
        } else {
            $("#suf-paymentType, #moptype-wrapper, #filter-paymentType").addClass("d-none");
        }
    });


    // $("#filter-payId").append("<option value=ALL>ALL</option>");

    // $(".active .discount-input").each(function)

    // SHOW EMI TENURE ON CHANGE PAYMENT TYPE
    $("#paymentType").on("change", function(e){
        var _getPaymentType = $(this).val();
        $(".common-card").removeClass("d-none");
        $(".common-card").addClass("active");
        $(".submit-wrapper").removeClass("d-none");
        
        if(_getPaymentType == "EMCC" || _getPaymentType == "EMDC" || _getPaymentType == "CC" || _getPaymentType == "DC"){
            $("[data-active=cardHolderType]").removeClass("d-none").addClass("active");
        }else{
            $("[data-active=cardHolderType]").addClass("d-none").removeClass("active");
        }

        if(_getPaymentType == "EMCC" || _getPaymentType == "EMDC"){
            $("[data-active=EMI]").removeClass("d-none").addClass("active");
            $("[data-active=issuerType]").removeClass("d-none").addClass("active");

        }

        else if(_getPaymentType == "CD"){
            $("[data-active=issuerType]").addClass("d-none").removeClass("active");
            $("[data-active=EMI]").addClass("d-none").removeClass("active");
        }

        else{
            $("[data-active=EMI]").addClass("d-none").removeClass("active"); 
            $("[data-active=issuerType]").removeClass("d-none").addClass("active");
            $("[data-active=issuerType]").removeClass("d-none").addClass("active");

        }
    });

    var fetchAllSufDetail = function() {
        $("body").removeClass("loader--inactive");

        $.ajax({
            type: "GET",
            url: "fetchDiscountDetails",
            success: function(data) {
                console.log(data);
                fetchData(data.aaData);
                inustoryCategory(data.industryCategory);
                slab(data.slab);
                setTimeout(function() {
                    $("body").addClass("loader--inactive");
                }, 1000);
            },
            error: function() {
                alert("Try Again, Something went wrong!");
                setTimeout(function() {
                    $("body").addClass("loader--inactive");
                }, 1000);
                return false;
            }
        });
    }

    fetchAllSufDetail();

    // percent amount
    $("body").on("blur",".percent-amount", function(e) {
        let val = $(this).val();
        if(val !== "") {
            val = Number(val);
            $(this).val(val.toFixed(2));
        }
    });
});