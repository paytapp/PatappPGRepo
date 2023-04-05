var globalPieChartList,
    globalActiveBtn,
    globalPieChartSale,
    globalPieChartDateFrom,
    globalPieChartDateTo,
    globalPieChartRefund,
    globalDateFromRefund,
    globalDateToRefund,
    globalPieChartRefund,
    globalDateFromHighest,
    globalDateToHighest,
    globalDateFromLowest,
    globalDateToLowest,
    globalHighestData,
    globalLowestData;


function ifPayout(_behaviour, _show, _showBehaviour){
    var _getAllClass = document.querySelectorAll(".dashboard-container");
    _getAllClass.forEach(function(index, array, element){
        index.classList[_behaviour]("d-none");
    })
    document.querySelector(_show).classList[_showBehaviour]("d-none");
}

function graph(){

    var _arr = [];
    // var months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
    var _month = "";
    var _day = "";
    for(var i = 0; i <= 6; i++){

        var dt = new Date();
        dt.setDate( dt.getDate() - 1 - i );
        var _newDate = new Date(dt);
        
        if(_newDate.getMonth() < 9 ){
            _month = "0"+Number(_newDate.getMonth() + 1);
        }else{
            _month = _newDate.getMonth() + 1;
        }

        if(_newDate.getDate() <= 9){
            _day = "0"+_newDate.getDate();
        }else{
            _day = _newDate.getDate();
        }

        _arr.push(_newDate.getFullYear() + "-" + _month + "-" + _day)
    }

    var _toDate = _arr[0];
    var _fromDate = _arr[6];

    var _limitLeft = [];
    var _limitConsumed = [];

    $.ajax({
        type: "POST",
        url: "graphLedgerAction",
        data: {
            "dateToGraph" : _toDate,
            "dateFromGraph" : _fromDate
        },
        success: function(data){


            if(data.aaGraphData.length > 0){

                for(key in data.aaGraphData){
                    _limitLeft.push(Number(data.aaGraphData[key]['openingBalance']));
                    _limitConsumed.push(Number(data.aaGraphData[key]['totalDebit']));
                }
    
                Highcharts.chart('container_ledger', {
                    chart: {
                        type: 'bar'
                    },
                    title: {
                        text: null
                    },
                    xAxis: {
                        categories: _arr,
                        title: {
                            text: null
                        }
                    },
                    yAxis: {
                        min: 0,
                        title: {
                            text: 'All figures in INR',
                            align: 'high'
                        },
                        labels: {
                            overflow: 'justify'
                        }
                    },
                    tooltip: {
                        valueSuffix: ' INR'
                    },
                    plotOptions: {
                        bar: {
                            dataLabels: {
                                enabled: true
                            }
                        }
                    },
                    legend: {
                        backgroundColor:
                            Highcharts.defaultOptions.legend.backgroundColor || '#FFFFFF',
                        shadow: true
                    },
                    credits: {
                        enabled: false
                    },
                    series: [
                        {
                          name: 'Limit Consumed',
                          data: _limitConsumed,
                          color: "#26a0da"
                        }, {
                          name: 'Limit Left',
                          data: _limitLeft,
                          color: "#041530"
                    }],
                    responsive: {
                        rules: [{
                            condition: {
                                maxWidth: 500
                            },
                            chartOptions: {
                                legend: {
                                    layout: 'horizontal',
                                    align: 'center',
                                    verticalAlign: 'bottom'
                                },
                                yAxis: {
                                    labels: {
                                        align: 'left',
                                        x: 0,
                                        y: -5
                                    },
                                    title: {
                                        text: null
                                    }
                                }
                                
                            }
                        }]
                    }
                });            
            }else{
                document.querySelector(".empty-graph").style.display = "flex";
            }

        }
    })

}

// graph();

function checkBalance(_amount){
    var _checkLogin = document.querySelector("#superMerchantLogin");
    var _obj = {};
    document.querySelector(_amount).closest("div").classList.add("loader-amount");
    var _allInput = document.querySelectorAll(".check-balance-div [data-var]");
    _allInput.forEach(function(index, array, element){
        var _attr = index.attributes['data-var'].nodeValue;
        _obj[_attr] = index.value;
    })
    if(_checkLogin != null){
        _obj.subMerchantPayId = _obj.payId;
        delete _obj.payId; 
    }
    $.ajax({
        type: "POST",
        url: "currentBalanceLedger",
        data: _obj,
        success: function(data){
            setTimeout(function(e){
                document.querySelector(_amount).innerHTML = data.respMap.checkAmount;
                document.querySelector(_amount).closest("div").classList.remove("loader-amount");
            }, 1000);
        }
    })
}

document.querySelector("#checkBalance").onclick = function(e){
    checkBalance("#ledgerAmount");
};

var _token = document.getElementsByName("token")[0].value;

    function setPayoutData(){
        document.querySelector("body").classList.remove("loader--inactive");
        var _allInput = document.querySelectorAll("[data-payout]");
        var _obj = {};
        _allInput.forEach(function(index, element, array){
            _obj[index.getAttribute("data-payout")] = index.value;
        });
        if($(".payoutFilterButton button.btnActive").length == 1){
            _obj['inputDays'] = $(".payoutFilterButton button.btnActive").text();
        }else{
            _obj['inputDays'] = "custom";
        }
        _obj['token'] = _token;
        _obj["struts.token.name"] = "token";
        _obj['subMerchantId'] = _obj['subMerchant'];
        delete _obj["subMerchant"];
        $.ajax({
            type: "POST",
            url: "payOutDataAction",
            data: _obj,
            success: function(data){
                var _json = data.payOut;
                for(key in _json){
                    var _checkNull = document.querySelector("[data-pay='"+key+"']");
                    if(_checkNull != null){
                        document.querySelector("[data-pay='"+key+"']").innerHTML = _json[key];
                    }
                }
                document.querySelector("body").classList.add("loader--inactive");
            }
        })
    }

    
    $(".payout-data").on("click", function(e){
        var _this = $(this).attr("data-download");
        $("#payoutDownloadForm").html("");
        var _data = "";
        var _allInput = document.querySelectorAll("[data-payout]");
        _allInput.forEach(function(index, element, array){
            _data += "<input type='hidden' value='"+index.value+"' name='"+index.getAttribute('data-payout')+"' />";
        })
        _data += "<input type='hidden' name='transactionType' value='"+_this+"' />";
        _data += "<input type='hidden' name='merchants' value='"+$("#payoutMerchant").val()+"' />";
        if($(".payoutFilterButton button.btnActive").length == 1){
            _data += "<input type='hidden' name='inputDays' value='"+$(".payoutFilterButton button.btnActive").text()+"'/>";
        }else{
            _data += "<input type='hidden' name='inputDays' value='custom'/>";
        }
        // $("#transactionType").val(_this);
        $("#payoutDownloadForm").append(_data);

        $("#payoutDownloadForm").submit();
    })
    
    $("#btn-payoutData").on("click", function(e){
        $(".payoutFilterButton button").removeClass("btnActive");
        setPayoutData();
    })
    
    $(".payoutFilterButton button").on("click", function(e){
        $(".payoutFilterButton button").removeClass("btnActive");
        $(this).addClass("btnActive");
        setPayoutData();
    })


function handleChange() {
    $("body").removeClass("loader--inactive");

    var _saleFlag = document.querySelector("#saleOrRfundFlag").value;

    if(_saleFlag == "payout") {
        ifPayout("add", ".dashboard-new-container", "remove");
        graph();
        checkBalance("#ledgerAmount");
        setPayoutData();
    } else {
        ifPayout("remove", ".dashboard-new-container", "add");

        if(_saleFlag == "true") {
            $("#status-text").text("Sale");
        } else {
            $("#status-text").text("Refund");
        }
    }

    var ele = document.querySelector(".newteds .btnActive"),
        activeBtn = "";

    if(ele !== null) {
        activeBtn = String(ele.getAttribute("name"));
    } else {
        activeBtn = "custom";
    }

    if(activeBtn == 'day') {
        $('#transactionTime').text('Hourly');
    } else if(activeBtn == 'week') {
        $('#transactionTime').text('Weekly');
    } else if(activeBtn == 'month' || activeBtn == 'lastMonth') {
        $('#transactionTime').text('Monthly');
    } else if(activeBtn == "year") {
        $('#transactionTime').text('Yearly');
    } else {
        $('#transactionTime').text('Filtered');
    }

    var highChartSelect = $("#highchart-select").val();

    if(highChartSelect == "histogram") {
        $("#highchart-select").selectpicker("val", "lineChart");
    }
    
    lineChart(activeBtn);
    statistics(activeBtn);

    getPieChartSaleData(activeBtn);
    // getPieChartRefundData();

    var usertype = $("#USER_TYPE").val();
    if(usertype == "ADMIN" || usertype == "SUBADMIN" || usertype == "RESELLER" || $("#superMerchantLogin").val() == "true") {
        getHighestMerchant(activeBtn);
        getLowestMerchant(activeBtn);
    }
    setTimeout(function(e){
        $("body").addClass("loader--inactive");
    }, 1000);
}

var _weekDaysArr = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday','Friday','Saturday'],
hoursTxt = ['00:00', '01:00', '02:00', '03:00','04:00','05:00','06:00','07:00','08:00','09:00','10:00','11:00','12:00', '13:00', '14:00', '15:00','16:00','17:00','18:00','19:00','20:00','21:00','22:00','23:00'],
_weekDays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday','Friday','Saturday','Sunday'],
_monthDays = ['01', '02', '03','04','05','06','07','08','09','10','11','12', '13', '14', '15','16','17','18','19','20','21','22','23','24','25','26','27','28','29','30','31'],
_yearsTxt = ['January', 'February', 'March', 'April','May','June','July','August','September','October','November','December'];

var showLineChart = function(pieChartList, activeBtn, dateFrom, dateTo) {
    var _activeBtn = activeBtn,
        a = [],
        c = [];
        // _switch = $("#highchart-switch").val();

    for (var i = 0; i < pieChartList.length; i++) {
        var piechart = pieChartList[i],
            success,
            successAlt,
            failled,
            failledAlt,
            _pointFormat,
            tempObjSuccess = {},
            tempObjFailed = {};

        // if(_switch == "volume") {
            success = parseInt(piechart.totalSuccess);
            failled = parseInt(piechart.totalFailed);

            _pointFormat = '<b>{point.y:.0f}</b>';
        // } else {
            if(piechart.totalAmountSuccess == null) {
                successAlt = 0.00;
            } else {
                successAlt = Number(Number(piechart.totalAmountSuccess).toFixed(2));
            }

            if(piechart.totalAmountFailed == null) {
                failledAlt = 0.00;
            } else {
                failledAlt = Number(Number(piechart.totalAmountFailed).toFixed(2));
            }

            // _pointFormat = '<b><i class="fa fa-inr"></i> {point.y:.2f}</b>';

            _pointFormatAlt = '<b><i class="fa fa-inr"></i> {point.z:.2f}</b>';
        // }

        if(isNaN(success)) {
            success = 0;
        }

        if(isNaN(failled)) {
            failled = 0;
        }

        tempObjSuccess.y = success;
        tempObjSuccess.z = successAlt;
        a.push(tempObjSuccess);

        tempObjFailed.y = failled;
        tempObjFailed.z = failledAlt;
        c.push(tempObjFailed);
    }
    
    $('#container').highcharts({
        title: {
            text: '',
            x: -20 //center
        },
        subtitle: {
            text: '',
            x: -20
        },
        xAxis: {
            title: {
                text: (function() {
                    if (_activeBtn == 'day') {
                        return 'Hours';
                    } else if (_activeBtn == 'week') {
                        return 'Week';
                    } else if (_activeBtn == 'month' || _activeBtn == 'lastMonth') {
                        return 'Month';
                    } else if(_activeBtn == 'year') {
                        return 'Year';
                    } else if(_activeBtn == "custom") {
                        if(dateFrom.value == dateTo.value) {
                            return dateFrom.value;
                        }
                    }
                })()
            },
            categories: (function() {
                if (_activeBtn == 'day') {
                    return hoursTxt;
                } else if (_activeBtn == 'week') {
                    return _weekDays;
                } else if (_activeBtn == 'month' || _activeBtn == 'lastMonth') {
                    return _monthDays;
                } else if(_activeBtn == "year") {
                    return _yearsTxt;
                } else if(_activeBtn == "custom") {
                    if(dateFrom.value == dateTo.value) {
                        return hoursTxt;
                    } else {
                        var oneDay = 24 * 60 * 60 * 1000,
                            timeInterval = splitDate(dateTo.value) - splitDate(dateFrom.value),
                            dayCount = timeInterval / oneDay + 1,
                            _dateArr = [];
    
                        for(var i = 0; i < dayCount; i++) {
                            var _result = _getDate(dateFrom.value, i);
                            _dateArr[i] = _result;
                        }
    
                        return _dateArr;
                    }
                }
            })(),
        },
        yAxis: {								
            title: {
                text: 'Number of Transactions'
            },
            plotLines: [{
                value: 0,
                width: 1,
                color: '#808080'
            }]
        },
        tooltip: {
            valueSuffix: '',
            headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
            pointFormat: '<tr><td style="padding:0"><span style="display:inline-block !important;"><span>{series.name}: </span><span>'+ _pointFormat +'</span></span><br /><span style="display:inline-block !important;"><span>Amount: </span><span>'+ _pointFormatAlt +'</span></span></td></tr>',
            footerFormat: '</table>',
            shared: false,
            useHTML: true
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            borderWidth: 0
        },
        
        series: [
            {
                name : 'Total Success',
                data: a,
                style: {
                    fontFamily: 'Roboto'
                },
                pointPlacement: 'between',
                pointRange: 1
            },
            {
                name : 'Total Failed',
                data: c,
                style: {
                    fontFamily: 'Roboto'
                },
                pointPlacement: 'between',
                pointRange: 1
            }
        ],
        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                    legend: {
                        layout: 'horizontal',
                        align: 'center',
                        verticalAlign: 'bottom'
                    },
                    yAxis: {
                        labels: {
                            align: 'left',
                            x: 0,
                            y: -5
                        },
                        title: {
                            text: null
                        }
                    }
                    
                }
            }]
        }
    });
}

var showHistogram = function(pieChartList, activeBtn, dateFrom, dateTo) {
    var _activeBtn = activeBtn,
        a = [],
        c = [];
        // _switch = $("#highchart-switch").val();

    for (var i = 0; i < pieChartList.length; i++) {
        var piechart = pieChartList[i],
            success,
            successAlt,
            failled,
            failledAlt,
            _pointFormat,
            tempObjSuccess = {},
            tempObjFailed = {};

        // if(_switch == "volume") {
            success = parseInt(piechart.totalSuccess);
            failled = parseInt(piechart.totalFailed);

            _pointFormat = '<b>{point.y:.0f}</b>';
        // } else {
            if(piechart.totalAmountSuccess == null) {
                successAlt = 0.00;
            } else {
                successAlt = Number(Number(piechart.totalAmountSuccess).toFixed(2));
            }

            if(piechart.totalAmountFailed == null) {
                failledAlt = 0.00;
            } else {
                failledAlt = Number(Number(piechart.totalAmountFailed).toFixed(2));
            }

            // _pointFormat = '<b><i class="fa fa-inr"></i> {point.y:.2f}</b>';

            _pointFormatAlt = '<b><i class="fa fa-inr"></i> {point.z:.2f}</b>';
        // }

        if(isNaN(success)) {
            success = 0;
        }

        if(isNaN(failled)) {
            failled = 0;
        }

        tempObjSuccess.y = success;
        tempObjSuccess.z = successAlt;
        a.push(tempObjSuccess);

        tempObjFailed.y = failled;
        tempObjFailed.z = failledAlt;
        c.push(tempObjFailed);
    }

    Highcharts.chart('container', {
        chart: {
          type: 'column'
        },
        title: {
            text: (function() {
                if (activeBtn == 'day') {
                    return 'Hours';
                } else if (activeBtn == 'week') {
                    return 'Week';
                } else if (activeBtn == 'month' || activeBtn == 'lastMonth') {
                    return 'Month';
                } else if(activeBtn == 'year') {
                    return 'Year';
                } else if(activeBtn == "custom") {
                    if(dateFrom.value == dateTo.value) {
                        return dateFrom.value;
                    }
                }
            })()
        },
        subtitle: {
          text: ''
        },
        xAxis: {
            categories: (function() {
                if (activeBtn == 'day') {
                    return hoursTxt;
                } else if (activeBtn == 'week') {
                    return _weekDays;
                } else if (activeBtn == 'month' || activeBtn == 'lastMonth') {
                    return _monthDays;
                } else if(activeBtn == 'year') {
                    return _yearsTxt;
                } else if(_activeBtn == "custom") {
                    if(dateFrom.value == dateTo.value) {
                        return hoursTxt;
                    } else {
                        var oneDay = 24 * 60 * 60 * 1000,
                            timeInterval = splitDate(dateTo.value) - splitDate(dateFrom.value),
                            dayCount = timeInterval / oneDay + 1,
                            _dateArr = [];
    
                        for(var i = 0; i < dayCount; i++) {
                            var _result = _getDate(dateFrom.value, i);
                            _dateArr[i] = _result;
                        }
    
                        return _dateArr;
                    }
                }
            })(),
          crosshair: true
        },
        yAxis: {
          min: 0,
          title: {
            text: ''
          }
        },
        tooltip: {
          headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
          pointFormat: '<tr><td style="padding:0"><span style="display:inline-block !important;"><span>{series.name}: </span><span>'+ _pointFormat +'</span></span><br /><span style="display:inline-block !important;"><span>Amount:</span><span>'+ _pointFormatAlt +'</span></span></td></tr>',
          footerFormat: '</table>',
          shared: false,
          useHTML: true
        },
        plotOptions: {
          column: {
            pointPadding: 0,
            borderWidth: 0,
            groupPadding: 0,
            shadow: false
          }
        },
        series: [
            {
              name: 'Total Success',
              data: a
            },
          {
              name: 'Total Failed',
              data: c
            }
        ]
      });
}

var splitDate = function(_date) {
    var $date = _date.split("-");
    $date = new Date(Number($date[2]), Number($date[1]) - 1, Number($date[0]));

    return $date.getTime();
}

var _getDate = function(_date, counter) {
    var $date = _date.split("-");
    var tempDate = Number($date[0]) + Number(counter);
    $date = new Date(Number($date[2]), Number($date[1]) - 1, tempDate);

    var dd = $date.getDate();
    var mm = $date.getMonth() + 1;
    var yyyy = $date.getFullYear();

    if(dd < 10) {
        dd = '0' + dd;
    } 

    if(mm < 10) {
        mm = '0' + mm;
    } 

    return dd + '/' + mm + '/' + yyyy;
}

$(document).ready(function() {

    var today = new Date();
    $(".datepick").attr("readonly", true);
    $('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
    $(".datepick").datepicker({
        prevText : "click for previous months",
        nextText : "click for next months",
        showOtherMonths : true,
        dateFormat : 'dd-mm-yy',
        selectOtherMonths : false,
        maxDate : new Date(),
        changeMonth: true,
        changeYear: true
    });

    


    var _allow = document.querySelector("#allowPayout").value;
    var _checkUserType = document.querySelector("#USER_TYPE").value;
    var _checkSubAdmin = document.querySelector("#subAdminTrue");

    if(_checkUserType == "RESELLER"){
        if(_allow != "true"){
            $('#saleOrRfundFlag').find('[value=payout]').remove();
            $('#saleOrRfundFlag').selectpicker('refresh');
        }
    }else{
        if(_allow != "true" && _checkUserType != "ADMIN" && _checkSubAdmin == null){
            $('#saleOrRfundFlag').find('[value=payout]').remove();
            $('#saleOrRfundFlag').selectpicker('refresh');
        }
    }


    

    $(".lpay_toggle").on("change", function(e) {
        var _this = $(this),
            _checkbox = _this.find("input[type=checkbox]"),
            _getChecked = _checkbox.is(":checked"),
            _label = _this.closest("label"),
            _elementId = _checkbox.attr("id");

        var updateCheckbox = function(that, arg1, arg2) {
            if(_getChecked) {
                _label.addClass("lpay_toggle_on");
                that.closest(".lpay_select_group").find(".txn-unit").text(arg1);
            } else {
                _label.removeClass("lpay_toggle_on");
                that.closest(".lpay_select_group").find(".txn-unit").text(arg2);
            }
        }

        if(_elementId == "saleOrRfundFlag") {
            updateCheckbox(_this, "Sale", "Refund");
            handleChange();
        } else {
            updateCheckbox(_this, "Amount", "Volume");
        }
    });

    $("#highchart-select").on("change", function() {
        var _value = $(this).val();            

        if(_value == "lineChart") {
            showLineChart(globalPieChartList, globalActiveBtn, globalDateFrom, globalDateTo);
        } else if(_value == "histogram") {
            showHistogram(globalPieChartList, globalActiveBtn, globalDateFrom, globalDateTo);
        }
    });

    $("#highchart-switch").on("change", function() {
        var _value = $("#highchart-select").val();

        if(_value == "lineChart") {
            showLineChart(globalPieChartList, globalActiveBtn, globalDateFrom, globalDateTo);
        } else if(_value == "histogram") {
            showHistogram(globalPieChartList, globalActiveBtn, globalDateFrom, globalDateTo);
        }
    });


    handleChange();

    $('.newteds button').click(function() {
        $('.newteds button').removeClass('btnActive');
        $(this).addClass('btnActive');

        $('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
        $('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
        $("#paymentsRegion").selectpicker("val", "ALL");
        handleChange();
    });



    var _dateValidate = function(obj) {
        if(splitDate(obj.dateFrom) > splitDate(obj.dateTo)) {
            alert("End date must be after start date.");            
            return false;
        } else if(obj.validateMonth) {
            var oneDay = 24 * 60 * 60 * 1000;
            var timeInterval = splitDate(obj.dateTo) - splitDate(obj.dateFrom);
            var dayCount = timeInterval / oneDay + 1;
    
            if(dayCount > 31) {
                alert("Please limit the date range to 1 month.");
                return false;
            }
        }
        return true;
    }

    $("body").on("click", "#btn-getData", function(e) {
        var result = _dateValidate({
            dateFrom : $("#dateFrom").val(),
            dateTo : $("#dateTo").val(),
            validateMonth : true
        });

        if(result) {
            $('.newteds button').removeClass('btnActive');
            handleChange();
        }
    });

    $("body").on("change", "#txnUnit", function(e) {
        e.preventDefault();
        $("body").removeClass("loader--inactive");
        showPieChart(globalPieChartSale, "txn-sale-chart", $("#txnUnit").is(":checked"), "label-pieChartSale", "sale-table");
    });

    $("body").on("change", "#txnUnitRefund", function(e) {
        e.preventDefault();
        $("body").removeClass("loader--inactive");
        showPieChart(globalPieChartRefund, "txn-refund-chart", $("#txnUnitRefund").is(":checked"), "label-pieChartRefund", "refund-table");
    });

    $("body").on("change", "#txnUnitHighest", function(e) {
        e.preventDefault();
        $("[data-hieghest]").addClass("d-none");
        var _isChecked = $(this).is(":checked");
        if(_isChecked == true){
            $("[data-hieghest='amount']").removeClass("d-none");
        }else{
            $("[data-hieghest='volume']").removeClass("d-none");
        }
    });

    $("body").on("click", "#btn-merchantHighest", function(e) {
        e.preventDefault();

        var dateFromHighest = document.getElementById("dateFrom").value,
            dateToHighest = document.getElementById("dateTo").value;
        
        if(dateFromHighest == globalDateFromHighest && dateToHighest == globalDateToHighest) {
            $("body").removeClass("loader--inactive");
            showMerchant({
                data: globalHighestData,
                containerId: "highest-merchant",
                resultType: $("#txnUnitHighest").is(":checked")
            });
        } else {
            var result = _dateValidate({
                dateFrom : $("#dateFrom").val(),
                dateTo : $("#dateTo").val(),
                validateMonth : true
            });
    
            if(result) {
                $("body").removeClass("loader--inactive");
                getHighestMerchant();
            }
        }
    });

    $("body").on("change", "#txnUnitLowest", function(e) {
        e.preventDefault();
        e.preventDefault();
        $("[data-lowest]").addClass("d-none");
        var _isChecked = $(this).is(":checked");
        if(_isChecked == true){
            $("[data-lowest='amount']").removeClass("d-none");
        }else{
            $("[data-lowest='volume']").removeClass("d-none");
        }
        
    });

    $("body").on("click", "#btn-merchantLowest", function(e) {
        e.preventDefault();

        var dateFromLowest = document.getElementById("dateFrom").value,
            dateToLowest = document.getElementById("dateTo").value;
        
        if(dateFromLowest == globalDateFromLowest && dateToLowest == globalDateToLowest) {
            $("body").removeClass("loader--inactive");
            showMerchant({
                data: globalLowestData,
                containerId: "lowest-merchant",
                resultType: $("#txnUnitLowest").is(":checked")
            });
        } else {
            var result = _dateValidate({
                dateFrom : $("#dateFrom").val(),
                dateTo : $("#dateTo").val(),
                validateMonth : true
            });
    
            if(result) {
                $("body").removeClass("loader--inactive");
                getLowestMerchant();
            }
        }
    });

    // SUBMERCHANT
    var _select = "<option value='ALL'>ALL</option>";
    $("[data-id='subMerchant']").find('option:eq(0)').before(_select);
    $("[data-id='subMerchant'] option[value='ALL']").attr("selected", "selected");

    // get sub merchant
    function getSubMerchant(_this, _url, _object, _selectLabel, _selectValue){
        var _merchant = _this.target.value;
        var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
        var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
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
                    if(_object.subUser == true){
                        if(obj.subUserList.length > 0){
                            _option += document.querySelector("#"+_subUserAttr).innerHTML = "<option value=''>Select Sub-Merchant</option>";
                            for(var i = 0; i < obj.subUserList.length; i++){
                                _option += document.querySelector("#"+_subUserAttr).innerHTML += "<option value="+obj.subUserList[i]["emailId"]+">"+obj.subUserList[i]["businessName"]+"</option>";
                            }
                            document.querySelector("[data-target="+_subUserAttr+"]").classList.remove("d-none");
                            document.querySelector("#"+_subUserAttr+" option[value='']").selected = true;
                            $("#"+_subUserAttr).selectpicker();
                            $("#"+_subUserAttr).selectpicker('refresh');
                        }else{
                            document.querySelector("[data-target="+_subUserAttr+"]").classList.add("d-none");
                            document.querySelector("#"+_subUserAttr).value = "";
                        }
                    }
                    if(_object.glocal == true){
                        if(obj.glocalFlag == true){
                            document.querySelector("[data-id=deliveryStatus]").classList.remove("d-none");
                            $("[data-id=deliveryStatus] select").selectpicker('val', 'All');
                        }else{
                            document.querySelector("[data-id=deliveryStatus]").classList.add("d-none");
                        }
                    }

                    if(_object.retailMerchantFlag == true){
                        $("#retailMerchantFlag").val(data.retailMerchantFlag);
                        document.querySelector("#retailMerchantFlag").value = data.retailMerchantFlag;
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

    var _checkMerchantNull = document.querySelector("#merchant");
    var _merchantLedgerNull = document.querySelector("#merchantPayIdLedger");
    var _checkPayoutMerchant = document.querySelector("#payoutMerchant");
    var _subMerchant = document.querySelector("#subMerchantLedger");

    if(_checkMerchantNull != null){
        document.querySelector("#merchant").addEventListener("change", function(_this){
            getSubMerchant(_this, "getSubMerchantList", {
                isSuperMerchant : true
            }, "ALL", "ALL");
        });
    }

    if(_checkPayoutMerchant != null){
        document.querySelector("#payoutMerchant").addEventListener("change", function(_this){
            getSubMerchant(_this, "getSubMerchantList", {
                isSuperMerchant : true
            }, "ALL", "ALL");
        });
    }

    if(_merchantLedgerNull != null){
        document.querySelector("#merchantPayIdLedger").addEventListener("change", function(_this){
            getSubMerchant(_this, "getSubMerchantList", {
                isSuperMerchant : true
            }, "ALL", "ALL");
            checkBalance("#ledgerAmount");
        });
    }

    

    if(_subMerchant != null){
        document.querySelector("#subMerchantLedger").addEventListener("change", function(_this){
            checkBalance("#ledgerAmount");
        })
    }
});

function statistics(activeBtn) {
    document.getElementById("dvTotalSuccess").innerHTML = "<img src='../image/loading_horizon.gif' width='20' height='16'>";
    document.getElementById("dvTotalFailed").innerHTML = "<img src='../image/loading_horizon.gif' width='20' height='16'>";
    document.getElementById("dvTotalRefunded").innerHTML = "<img src='../image/loading_horizon.gif' width='20' height='16'>";
    document.getElementById("dvRefundedAmount").innerHTML = "<img src='../image/loading_horizon.gif' width='20' height='16'>";
    document.getElementById("dvApprovedAmount").innerHTML = "<img src='../image/loading_horizon.gif' width='20' height='16'>";
    document.getElementById("dvTotalRejected").innerHTML = "<img src='../image/loading_horizon.gif' width='20' height='16'>";
    document.getElementById("dvTotalDropped").innerHTML = "<img src='../image/loading_horizon.gif' width='20' height='16'>";
    document.getElementById("dvTotalCancelled").innerHTML = "<img src='../image/loading_horizon.gif' width='20' height='16'>";
    document.getElementById("dvTotalFraud").innerHTML = "<img src='../image/loading_horizon.gif' width='20' height='16'>";
    document.getElementById("dvTotalInvalid").innerHTML = "<img src='../image/loading_horizon.gif' width='20' height='16'>";

    var token = document.getElementsByName("token")[0].value,
        saleRefundFlag = $("#saleOrRfundFlag").val();

    var merchantEmail = "";
    if($("#USER_TYPE").val() == "SUBUSER") {
        merchantEmail = $("#USER_EMAIL").val();
    } else {
        merchantEmail = $("#merchant").val();
    }

    $.ajax({
        url : "statisticsActionCapture",
        type : "POST",
        timeout: 0,
        data : {
            inputDays : activeBtn,
            emailId : merchantEmail,
            subMerchantId: document.getElementById("subMerchant").value,
            currency : document.getElementById("currency").value,
            dateFrom : document.getElementById("dateFrom").value,
            dateTo : document.getElementById("dateTo").value,
            paymentRegion : document.getElementById("paymentsRegion").value,
            saleReportFlag : saleRefundFlag,
            token : token,
            "struts.token.name" : "token",
        },
        success : function(data) {
            if(saleRefundFlag == "true") {
                $("#dvTotalSuccess").html(data.statistics.totalSuccess);
                $("#dvTotalGross").html(data.statistics.totalGrossSuccess);
                $("#dvGrossApprovedAmount").html(data.statistics.grossApprovedAmount);
                $("#dvTotalFailed").html(data.statistics.totalFailed);
                $("#dvApprovedAmount").html(data.statistics.approvedAmount);
                $("#dvTotalRejected").html(data.statistics.totalRejectedDeclined);
                $("#dvTotalDropped").html(data.statistics.totalDropped);
                $("#dvTotalCancelled").html(data.statistics.totalCancelled);
                $("#dvTotalFraud").html(data.statistics.totalFraud);
                $("#dvTotalInvalid").html(data.statistics.totalInvalid);

                $("#dvTotalRefunded").closest(".col-md-5ths").addClass("d-none");
                $("#dvRefundedAmount").closest(".col-md-5ths").addClass("d-none");
                $("#dvTotalTimeout").closest(".col-md-5ths").addClass("d-none");

                $("#dvTotalGross").closest(".col-md-5ths").removeClass("d-none");
                $("#dvApprovedAmount").closest(".col-md-5ths").removeClass("d-none");
                $("#dvTotalSuccess").closest(".col-md-5ths").removeClass("d-none");
                $("#dvTotalDropped").closest(".col-md-5ths").removeClass("d-none");
                $("#dvTotalCancelled").closest(".col-md-5ths").removeClass("d-none");
                $("#dvGrossApprovedAmount").closest(".col-md-5ths").removeClass("d-none");
            } else {                
                $("#dvTotalRefunded").html(data.statistics.totalSuccess);
                $("#dvRefundedAmount").html(data.statistics.approvedAmount);
                
                $("#dvTotalFailed").html(data.statistics.totalFailed);
                $("#dvTotalRejected").html(data.statistics.totalRejectedDeclined);
                $("#dvTotalInvalid").html(data.statistics.totalInvalid);
                $("#dvTotalFraud").html(data.statistics.totalFraud);
                $("#dvTotalTimeout").html(data.statistics.totalDropped);
                
                $("#dvApprovedAmount").closest(".col-md-5ths").addClass("d-none");
                $("#dvTotalSuccess").closest(".col-md-5ths").addClass("d-none");
                $("#dvGrossApprovedAmount").closest(".col-md-5ths").addClass("d-none");
                $("#dvTotalGross").closest(".col-md-5ths").addClass("d-none");
                $("#dvTotalSuccess").closest(".col-md-5ths").addClass("d-none");
                $("#dvTotalDropped").closest(".col-md-5ths").addClass("d-none");
                $("#dvTotalCancelled").closest(".col-md-5ths").addClass("d-none");
                // $("#dvTotalFraud").closest(".col-md-5ths").addClass("d-none");
                $("#dvTotalRefunded").closest(".col-md-5ths").removeClass("d-none");
                $("#dvRefundedAmount").closest(".col-md-5ths").removeClass("d-none");
                $("#dvTotalTimeout").closest(".col-md-5ths").removeClass("d-none");
            }
        },
        error : function(data) { }
    });
}

function statisticsRefund(activeBtn) {
    var token = document.getElementsByName("token")[0].value;

    var merchantEmail = "";
    if($("#USER_TYPE").val() == "SUBUSER") {
        merchantEmail = $("#USER_EMAIL").val();
    } else {
        merchantEmail = $("#merchant").val();
    }

    $.ajax({
        url : "statisticsActionRefund",
        type : "POST",
        timeout: 0,
        data : {
            inputDays : activeBtn,
            emailId : merchantEmail,
            subMerchantId: document.getElementById("subMerchant").value,
            currency : document.getElementById("currency").value,
            dateFrom : document.getElementById("dateFrom").value,
            dateTo : document.getElementById("dateTo").value,
            paymentRegion : document.getElementById("paymentsRegion").value,
            token : token,
            "struts.token.name" : "token",
        },
        success : function(data) {
            document.getElementById("dvTotalRefunded").innerHTML = data.statistics.totalRefunded;
            document.getElementById("dvRefundedAmount").innerHTML = data.statistics.refundedAmount;
            statisticsAll(activeBtn);
        },
        error : function(data) { }
    });
}


function statisticsAll(activeBtn) {
    var token = document.getElementsByName("token")[0].value;

    var merchantEmail = "";
    if($("#USER_TYPE").val() == "SUBUSER") {
        merchantEmail = $("#USER_EMAIL").val();
    } else {
        merchantEmail = $("#merchant").val();
    }

    $.ajax({
        url : "statisticsAction",
        type : "POST",
        timeout: 0,
        data : {
            inputDays : activeBtn,
            emailId : merchantEmail,
            subMerchantId: document.getElementById("subMerchant").value,
            currency : document.getElementById("currency").value,
            dateFrom : document.getElementById("dateFrom").value,
            dateTo : document.getElementById("dateTo").value,
            paymentRegion : document.getElementById("paymentsRegion").value,
            token : token,
            "struts.token.name" : "token",
        },
        success : function(data) {
            document.getElementById("dvTotalFailed").innerHTML = data.statistics.totalFailed;
            document.getElementById("dvTotalRejected").innerHTML = data.statistics.totalRejectedDeclined;
            document.getElementById("dvTotalDropped").innerHTML = data.statistics.totalDropped;
            document.getElementById("dvTotalCancelled").innerHTML = data.statistics.totalCancelled;
            document.getElementById("dvTotalFraud").innerHTML = data.statistics.totalFraud;
            document.getElementById("dvTotalInvalid").innerHTML = data.statistics.totalInvalid;				
        },
        error : function(data) { }
    });
}

var showPieChart = function(obj, containerId, txnUnit, customLabel, tableId) {
    var resObj = {};
    if(txnUnit == true) {
        var _txnUnitsText = '<span style="display: flex;width: 100%">Transactions: <b>{point.y:.2f}%</b></span><br /><span>Total Amount: <b>{point.z:.2f}</b></span>';
    }else{
        var _txnUnitsText = '<span style="display: flex;width: 100%">Transactions: <b>{point.y:.2f}%</b></span><br /><span>Total Volume: <b>{point.z:.2f}</b></span>';
    }

    if(txnUnit) {
        resObj.CreditCardValue = obj.totalCreditCardsTxnAmount;
        resObj.CreditCardPercentage = obj.totalCreditCardsTxnAmountPercentage;
        resObj.DebitCardValue = obj.totalDebitCardsTxnAmount;
        resObj.DebitCardPercentage = obj.totalDebitCardsTxnAmountPercentage;
        resObj.InternationalValue = obj.totalInternationalTxnAmount;
        resObj.InternationalPercentage = obj.totalInternationalTxnAmountPercentage;
        resObj.UPIValue = obj.totalUpiTxnAmount;
        resObj.UPIPercentage = obj.totalUpiTxnAmountPercentage;
        resObj.NetBankingValue = obj.totalNetBankingTxnAmount;
        resObj.NetBankingPercentage = obj.totalNetBankingTxnAmountPercentage;
        resObj.WalletValue = obj.totalWalletTxnAmount;
        resObj.WalletPercentage = obj.totalWalletTxnAmountPercentage;
        resObj.EMIValue = obj.totalEmiTxnAmount;
        resObj.EMIPercentage = obj.totalEmiTxnAmountPercentage;
        resObj.CashOnDeliveryValue = obj.totalCodTxnAmount;
        resObj.CashOnDeliveryPercentage = obj.totalCodTxnAmountPercentage;
    } else {
        resObj.CreditCardValue = obj.totalCreditCardsTransaction;
        resObj.CreditCardPercentage = obj.totalCreditCardsTransactionPercentage;
        resObj.DebitCardValue = obj.totalDebitCardsTransaction;
        resObj.DebitCardPercentage = obj.totalDebitCardsTransactionPercentage;
        resObj.InternationalValue = obj.totalInternationalTransaction;
        resObj.InternationalPercentage = obj.totalInternationalTransactionPercentage;
        resObj.UPIValue = obj.totalUpiTransaction;
        resObj.UPIPercentage = obj.totalUpiTransactionPercentage;
        resObj.NetBankingValue = obj.totalNetBankingTransaction;
        resObj.NetBankingPercentage = obj.totalNetBankingTransactionPercentage;
        resObj.WalletValue = obj.totalWalletTransaction;
        resObj.WalletPercentage = obj.totalWalletTransactionPercentage;
        resObj.EMIValue = obj.totalEmiTransaction;
        resObj.EMIPercentage = obj.totalEmiTransactionPercentage;
        resObj.CashOnDeliveryValue = obj.totalCodTransaction;
        resObj.CashOnDeliveryPercentage = obj.totalCodTransactionPercentage;
    }

    for(var key in resObj) {
        var _val = "";
        if(resObj[key] == null) {
            _val = 0;
        } else {
            _val = resObj[key];
        }

        $("#" + tableId).find('[data-id="'+ key +'"]').text(_val);
    }



    $("#" + tableId).find(".heading-value").text($("#" + tableId).closest(".pieChart").find(".txn-unit").text());

    Highcharts.chart(containerId, {
        chart: {
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            type: 'pie'
        },
        title: {
            text: ""
        },
        tooltip: {
            pointFormat: _txnUnitsText
        },
        accessibility: {
            point: {
                valueSuffix: '%'
            }
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    format: '{point.y:.2f}%',
                    distance: -50,
                    filter: {
                        property: 'percentage',
                        operator: '>',
                        value: 4
                    }
                },
                showInLegend: true
            }
        },
        series: [{
            name: 'Transactions',
            colorByPoint: true,
            data: [{
                name: 'Credit Card',
                y: resObj.CreditCardPercentage !== null ? Number(resObj.CreditCardPercentage) : 0,
                z: resObj.CreditCardValue !== null ? Number(resObj.CreditCardValue) : 0,
                color: '#7cb5ec'
            }, {
                name: 'Debit Card',
                y: resObj.DebitCardPercentage !== null ? Number(resObj.DebitCardPercentage) : 0,
                z: resObj.DebitCardValue !== null ? Number(resObj.DebitCardValue) : 0,
                color: '#434348'
            }, {
                name: 'International',
                y: resObj.InternationalPercentage !== null ? Number(resObj.InternationalPercentage) : 0,
                z: resObj.InternationalValue !== null ? Number(resObj.InternationalValue) : 0,
                color: '#5cb85c'
            }, {
                name: 'UPI',
                y: resObj.UPIPercentage !== null ? Number(resObj.UPIPercentage) : 0,
                z: resObj.UPIValue !== null ? Number(resObj.UPIValue) : 0,
                color: '#f7a35c'
            }, {
                name: 'EMI',
                y: resObj.EMIPercentage !== null ? Number(resObj.EMIPercentage) : 0,
                z: resObj.EMIValue !== null ? Number(resObj.EMIValue) : 0,
                color: '#8085e9'
            }, {
                name: 'Cash On Delivery',
                y: resObj.CashOnDeliveryPercentage !== null ? Number(resObj.CashOnDeliveryPercentage) : 0,
                z: resObj.CashOnDeliveryValue !== null ? Number(resObj.CashOnDeliveryValue) : 0,
                color: '#f15c80'
            }, {
                name: 'Wallet',
                y: resObj.WalletPercentage !== null ? Number(resObj.WalletPercentage) : 0,
                z: resObj.WalletValue !== null ? Number(resObj.WalletValue) : 0,
                color: '#e4d354'
            }, {
                name: 'Net Banking',
                y: resObj.NetBankingPercentage !== null ? Number(resObj.NetBankingPercentage) : 0,
                z: resObj.NetBankingValue !== null ? Number(resObj.NetBankingValue) : 0,
                color: '#2b908f'
            }]
        }]
    });

    setTimeout(function() {
        $("body").addClass("loader--inactive");						
    }, 1000);
}

var showMerchant = function(obj) {

    var merchantTxnAndAmt = obj.data;
    var _container = $("#" + obj.containerId);
    var _createListMerchant = "";
    var _createListVolume = "";
    _container.find("table .lowestMerchantBody_amount").html("");
    _container.find("table .lowestMerchantBody_volume").html("");
    var _array = ['First', 'Second', 'Third', 'Fourth', 'Fifth'];
    if(merchantTxnAndAmt != null){
        for(var i = 0; i < _array.length; i++){
    
            var _totalFirstTransactionAmount = "total"+_array[i]+"TransactionAmount";
            var _totalFirstTransactionVolume = "total"+_array[i]+"TransactionVolume";
            var _totalFirstAmountMerchantBusinessName = "txn"+_array[i]+"AmountMerchantBusinessName";
            var _totalFirstVolumeMerchantBusinessName = "txn"+_array[i]+"VolumeMerchantBusinessName";
            var _totalFirstAmountSuperMerchantBusinessName = "txn"+[_array[i]]+"AmountSuperMerchantBusinessName";
            var _totalFirstVolumeSuperMerchantBusinessName = "txn"+[_array[i]]+"VolumeSuperMerchantBusinessName";
            
            _createListMerchant  += "<tr><td class='merchantAmountName'>"+merchantTxnAndAmt[_totalFirstAmountMerchantBusinessName]+"</td><td>"+merchantTxnAndAmt[_totalFirstAmountSuperMerchantBusinessName]+"</td><td><div class='txn-label'><div><i class='fa fa-inr'></i><span>"+merchantTxnAndAmt[_totalFirstTransactionAmount]+"</span></div></div></td></tr>";
            _createListVolume += "<tr><td class='merchantVolumeName'>"+merchantTxnAndAmt[_totalFirstVolumeMerchantBusinessName]+"</td><td>"+merchantTxnAndAmt[_totalFirstVolumeSuperMerchantBusinessName]+"</td><td><div class='txn-label'><div><span>"+merchantTxnAndAmt[_totalFirstTransactionVolume]+"</span></div></div></td></tr>";
        }
        _container.find(".data-unavailable").addClass("d-none");
        _container.find("table .lowestMerchantBody_amount").html(_createListMerchant);
        _container.find("table .lowestMerchantBody_volume").html(_createListVolume);
        _container.find("table .lowestMerchantBody_volume").closest(".col-md-12").addClass("d-none");
    }
    if(merchantTxnAndAmt != null) {
        if(obj.resultType) {
        _container.find(".data-unavailable").addClass("d-none");
        _container.find(".txn-merchant").removeClass("d-none");
        }
    } else {
        _container.find(".data-unavailable").removeClass("d-none");
        _container.find(".txn-merchant").addClass("d-none");
    }

    var _getAllMerchantAmount = document.querySelectorAll(".merchantAmountName");
    var _getAllMerchantVolume = document.querySelectorAll(".merchantVolumeName");

    _getAllMerchantAmount.forEach(function(index, array, element){
        var _value = index.innerText;
        if(_value == "null"){
            index.closest("tr").classList.add("d-none");
        }
    })
    _getAllMerchantVolume.forEach(function(index, array, element){
        var _value = index.innerText;
        if(_value == "null"){
            index.closest("tr").classList.add("d-none");
        }
    })

}

    setTimeout(function() {
        $("body").addClass("loader--inactive");
    }, 1000);

var getLowestMerchant = function(activeBtn) {
    var token = document.getElementsByName("token")[0].value;

    var merchantEmail = "";
    if($("#USER_TYPE").val() == "SUBUSER") {
        merchantEmail = $("#USER_EMAIL").val();
    } else {
        merchantEmail = $("#merchant").val();
    }

    $.ajax({
        url : "getLowestMerchant",
        type : "POST",
        data : {
            inputDays : activeBtn,
            emailId : merchantEmail,
            subMerchantId: document.getElementById("subMerchant").value,
            currency : document.getElementById("currency").value,
            dateFrom : document.getElementById("dateFrom").value,
            dateTo : document.getElementById("dateTo").value,
            paymentRegion : document.getElementById("paymentsRegion").value,
            saleReportFlag : $("#saleOrRfundFlag").val(),
            token : token,
            "struts.token.name" : "token"
        },
        success : function(data) {
            globalDateFromLowest = changeDateFormat(data.dateFrom);
            globalDateToLowest = changeDateFormat(data.dateTo);

            globalLowestData = data.merchantTxnAndAmt;

            showMerchant({
                data: globalLowestData,
                containerId: "lowest-merchant",
                resultType: $("#txnUnitLowest").is(":checked")
            });

            setTimeout(function() {
                $("body").addClass("loader--inactive");						
            }, 1000);
        },
        error : function() {
            alert("Something went wrong");
            setTimeout(function() {
                $("body").addClass("loader--inactive");						
            }, 1000);
        }
    });
}

var changeDateFormat = function(date) {
    var tempDate = date.split("-");
    return tempDate[2] + "-" + tempDate[1] + "-" + tempDate[0];
}

var getHighestMerchant = function(activeBtn) {
    var token = document.getElementsByName("token")[0].value;
    
    var merchantEmail = "";
    if($("#USER_TYPE").val() == "SUBUSER") {
        merchantEmail = $("#USER_EMAIL").val();
    } else {
        merchantEmail = $("#merchant").val();
    }

    $.ajax({
        url : "getHighestMerchant",
        type : "POST",
        data : {
            inputDays : activeBtn,
            emailId : merchantEmail,
            subMerchantId: document.getElementById("subMerchant").value,
            currency : document.getElementById("currency").value,
            dateFrom : document.getElementById("dateFrom").value,
            dateTo : document.getElementById("dateTo").value,
            paymentRegion : document.getElementById("paymentsRegion").value,
            saleReportFlag : $("#saleOrRfundFlag").val(),
            token : token,
            "struts.token.name" : "token"
        },
        success : function(data) {            
            globalDateFromHighest = changeDateFormat(data.dateFrom);
            globalDateToHighest = changeDateFormat(data.dateTo);

            globalHighestData = data.merchantTxnAndAmt;

            showMerchant({
                data: globalHighestData,
                containerId: "highest-merchant",
                resultType: $("#txnUnitHighest").is(":checked")
            });

            setTimeout(function() {
                $("body").addClass("loader--inactive");						
            }, 1000);
        },
        error : function() {
            alert("Something went wrong");
            setTimeout(function() {
                $("body").addClass("loader--inactive");						
            }, 1000);
        }
    });
}

var getPieChartRefundData = function(activeBtn) {
    var token = document.getElementsByName("token")[0].value;

    var merchantEmail = "";
    if($("#USER_TYPE").val() == "SUBUSER") {
        merchantEmail = $("#USER_EMAIL").val();
    } else {
        merchantEmail = $("#merchant").val();
    }

    $.ajax({
        url : "getPieChartRefundData",
        type : "POST",
        data : {
            inputDays : activeBtn,
            emailId : merchantEmail,
            subMerchantId: document.getElementById("subMerchant").value,
            dateFrom : document.getElementById("dateFrom").value,
            dateTo : document.getElementById("dateTo").value,
            token : token,
            "struts.token.name" : "token"
        },
        success : function(data) {
            globalPieChartRefund = data.pieChart;
            globalDateFromRefund = data.dateFrom;
            globalDateToRefund = data.dateTo;

            
        },
        error : function() {
            alert("Something went wrong");
            setTimeout(function() {
                $("body").addClass("loader--inactive");						
            }, 1000);
        }
    });
}

var getPieChartSaleData = function(activeBtn) {
    var token = document.getElementsByName("token")[0].value;
    var merchantEmail = "";
    if($("#USER_TYPE").val() == "SUBUSER") {
        merchantEmail = $("#USER_EMAIL").val();
    } else {
        merchantEmail = $("#merchant").val();
    }

    $.ajax({
        url : "getPieChartSaleData",
        type : "POST",
        data : {
            inputDays : activeBtn,
            emailId : merchantEmail,
            subMerchantId: document.getElementById("subMerchant").value,
            currency : document.getElementById("currency").value,
            dateFrom : document.getElementById("dateFrom").value,
            dateTo : document.getElementById("dateTo").value,
            paymentRegion : document.getElementById("paymentsRegion").value,
            saleReportFlag : $("#saleOrRfundFlag").val(),
            token : token,
            "struts.token.name" : "token"
        },
        success : function(data) {
            globalPieChartSale = data.pieChart;
            showPieChart(globalPieChartSale, "txn-sale-chart", $("#txnUnit").is(":checked"), "label-pieChartSale", "sale-table");
        },
        error : function() {
            alert("Something went wrong");
            setTimeout(function() {
                $("body").addClass("loader--inactive");						
            }, 1000);
        }
    });
}

function lineChart(activeBtn) {
    var token = document.getElementsByName("token")[0].value;

    var merchantEmail = "";
    if($("#USER_TYPE").val() == "SUBUSER") {
        merchantEmail = $("#USER_EMAIL").val();
    } else {
        merchantEmail = $("#merchant").val();
    }
    
    $.ajax({
        url : "lineChartAction",
        type : "POST",
        timeout: 0,
        data : {
            inputDays : activeBtn,
            emailId : merchantEmail,
            subMerchantId: document.getElementById("subMerchant").value,
            currency : document.getElementById("currency").value,
            dateFrom : document.getElementById("dateFrom").value,
            dateTo : document.getElementById("dateTo").value,
            paymentRegion : document.getElementById("paymentsRegion").value,
            saleReportFlag : $("#saleOrRfundFlag").val(),
            token : token,
            "struts.token.name" : "token",
        },
        success : function(data) {
            globalPieChartList = data.pieChart;
            globalActiveBtn = activeBtn;
            globalDateFrom = document.getElementById("dateFrom");
            globalDateTo = document.getElementById("dateTo");

            showLineChart(data.pieChart, activeBtn, globalDateFrom, globalDateTo);
        },
        error : function(data) {
            setTimeout(function() {
                $("body").addClass("loader--inactive");						
            }, 1000);
        }
    });
}


//download data
var downloadReportFile = function() {
    var _getAllData = document.querySelectorAll("[data-name]");
    _getAllData.forEach(function(index, element, array) {
        _getAllData[element].classList.add("cursor-pointer");

        _getAllData[element].addEventListener("click", function(e) {
            document.querySelector("#downloadForm").innerHTML = "";
            var _getAllFilter = document.querySelectorAll(".dashboard-filter [name]"),
                _getButton = document.querySelector(".newteds .btnActive"),
                _formElement = "";

            _getAllFilter.forEach(function(index) {
                var _checkVisible = index.closest(".form-group").classList.toString();
                if(_checkVisible.indexOf("d-none") == -1) {
                var _name = index.getAttribute("name");
                _formElement += "<input type='hidden' name='"+_name+"' value='"+index.value+"' />";
                }
            });
            
            if($("#USER_TYPE").val() == "SUBUSER") {
            _formElement += "<input type='hidden' name='merchants' value='"+$("#USER_EMAIL").val()+"' />";
            }

            var _getClicked = array[element].getAttribute("data-name");
            _formElement += "<input type='hidden' name='transactionType' value='"+_getClicked+"' />";

            if(_getButton != null) {
            _formElement += "<input type='hidden' name='inputDays' value='"+_getButton.innerText+"' />";
            } else {
            _formElement += "<input type='hidden' name='inputDays' value='custom' />";
            }

            _formElement += '<input type="hidden" name="saleReportFlag" value="'+ $("#saleOrRfundFlag").val() +'">';
            document.querySelector("#downloadForm").innerHTML += _formElement;
            document.querySelector("#downloadForm").submit();
        });
    });
}

var loggedUserType = document.getElementById("logged-usertype").value,
    reportPermission = document.getElementById("report-permission").value;

if(loggedUserType == "SUBUSER") {
    if(reportPermission == "true") {
        downloadReportFile();
    }
} else {
    downloadReportFile();
}