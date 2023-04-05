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

    // ADD SELECT OPTION
    var _addSelectOption = function(reffObj) {
        var opt = document.createElement("option");
        opt.appendChild(document.createTextNode(reffObj.text));
        opt.value = reffObj.value;
        document.getElementById(reffObj.element).appendChild(opt);
    }

    // update payment Region
    var updateSelection = function(obj) {
        _addSelectOption({
            element: obj.element,
            value: "",
            text: "Select Payment Type"
        });
        for(var key in obj.data) {
            _addSelectOption({
                element: obj.element,
                value: obj.data[key],
                text: key
            });
        }
        $("#" + obj.element).selectpicker("refresh");
    }

    // show payment type behalf of payment region
    $("#paymentRegion").on("change", function(e){
        var _val = $(this).val();
        var _pmntType = "filter_paymentType";
        $("#filter_paymentType").html("");
        if(_val == "Domestic"){
            updateSelection({
                element: _pmntType,
                data: domesticObj
            });
        } else if(_val == "International"){
            updateSelection({
                element: _pmntType,
                data: internationalObj
            });
        }
        if(_val != ""){
            $("[data-id=filter_paymentType]").removeClass("d-none");
        }else{
            $("[data-id=filter_paymentType]").addClass("d-none");
        }
    });

    $("#paymentOptions-info select").on("change", function(e){
        var _checked = false;
        var _dataObj = {};
        // var _getAllInput = document.querySelectorAll(".filter_sub").length;
        $("#paymentOptions-info select").each(function(e){
            var _val = $(this).val();
            var _key = $(this).attr("name");
            if(_val == "" || _val == null){;
                _checked = false;
                return false;
            }else{
                _dataObj[_key] = _val;
                _checked = true;
            }
        });
        if(_checked == true){
            $("body").removeClass("loader--inactive");
            $('#myTable').DataTable( {
                destroy: true,
                dom : 'BTftlpi',
                buttons: ['csvHtml5', 'pdf'
                ],
                "ajax": {
                    "type": "post",
                    "url": 'viewSufDetailsAction',
                    "data": _dataObj
                },
                "lengthMenu": [[10, 25, 50, -1], [10, 25, 50, "All"]],
                "columns": [
                    { "data": 'merchantName' },
                    { "data": 'txnType' },
                    { "data": 'mopType' },
                    { "data": 'paymentType' },
                    { "data": 'paymentRegion' },
                    { "data": 'fixedCharge' },
                    { "data": 'percentageAmount' },
                ]
            } );
            setTimeout(function(){
                $("body").addClass("loader--inactive");
            }, 500);
        }
    });

    // datatable call default
    $("#myTable").DataTable({
        dom : 'BTftlpi',
        buttons: ['csvHtml5', 'pdf'
        ],
        "lengthMenu": [[10, 25, 50, -1], [10, 25, 50, "All"]],
    });

});