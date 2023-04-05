var globalPieChartList,
    globalActiveBtn,
    globalPieChartSale,
    globalPieChartDateFrom,
    globalPieChartDateTo,
    globalPieChartRefund,
    globalDateFromRefund,
    globalDateToRefund,
    globalPieChartRefund;


function handleChange(btnAction) {
    $("body").removeClass("loader--inactive");

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
    var _activeBtn = activeBtn, a = [], b = [], c = [];

    for (var i = 0; i < pieChartList.length; i++) {
        var piechart = pieChartList[i];
        var success = parseInt(piechart.totalSuccess);
        var refund = parseInt(piechart.totalRefunded);
        var failled = parseInt(piechart.totalFailed);

        if(isNaN(success)) {
            success = 0;
        }

        if(isNaN(refund)) {
            refund = 0;
        }

        if(isNaN(failled)) {
            failled = 0;
        }

        a.push(success);
        b.push(refund);
        c.push(failled);
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
                            return "One day data";
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
            valueSuffix: ''
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle',
            borderWidth: 0
        },
        
        series: [{
            name : 'Total Success',
            data: a,
            style: {
                fontFamily: 'Roboto'
            }	
        },
        {
            name : 'Total Refunded',
            data: b,
            style: {
                fontFamily: 'Roboto'
            }
        },
        {
            name : 'Total Failed',
            data: c,
            style: {
                fontFamily: 'Roboto'
            }
        }]
    });
}

var showHistogram = function(pieChartList, activeBtn) {
    var _activeBtn = activeBtn, a = [], b = [], c = [];

    for (var i = 0; i < pieChartList.length; i++) {
        var piechart = pieChartList[i];
        var success = parseInt(piechart.totalSuccess);
        var refund = parseInt(piechart.totalRefunded);
        var failled = parseInt(piechart.totalFailed);

        if(isNaN(success)) {
            success = 0;
        }

        if(isNaN(refund)) {
            refund = 0;
        }

        if(isNaN(failled)) {
            failled = 0;
        }

        a.push(success);
        b.push(refund);
        c.push(failled);
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
                        return "One day data";
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
          pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
            '<td style="padding:0"><b>{point.y:.0f}</b></td></tr>',
          footerFormat: '</table>',
          shared: true,
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
              name: 'Total Refunded',
              data: b
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
    $("#highchart-select").on("change", function() {
        var _value = $(this).val();            

        if(_value == "lineChart") {
            showLineChart(globalPieChartList, globalActiveBtn, globalDateFrom, globalDateTo);
        } else if(_value == "histogram") {
            showHistogram(globalPieChartList, globalActiveBtn, globalDateFrom, globalDateTo);
        }
    });

    var today = new Date();

    $(".date-input").each(function() {
        var dateInputId = $(this).attr("id");

        $("#" + dateInputId).datepicker({
            prevText : "click for previous months",
            nextText : "click for next months",
            showOtherMonths : true,
            changeMonth : true,
            changeYear : true,
            dateFormat : 'dd-mm-yy',
            selectOtherMonths : false,
            maxDate : new Date()
        });
        $("#" + dateInputId).val($.datepicker.formatDate('dd-mm-yy', today));
    });


    handleChange("btnActive");

    $('.newteds button').click(function() {
        $('.newteds button').removeClass('btnActive');
        $(this).addClass('btnActive');

        $('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
        $('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
        $("#paymentsRegion").selectpicker("val", "ALL");
        handleChange("btnActive");
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
            handleChange("customActive");
        }
    });
});

function lineChart(activeBtn) {
    var token = document.getElementsByName("token")[0].value;
    
    $.ajax({
        url : "lineChartAction",
        type : "POST",
        timeout: 0,
        data : {
            inputDays : activeBtn,
            emailId : document.getElementById("merchant").value,
            currency : document.getElementById("currency").value,
            dateFrom : document.getElementById("dateFrom").value,
            dateTo : document.getElementById("dateTo").value,
            paymentRegion : document.getElementById("paymentsRegion").value,
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
    
function statistics(activeBtn) {
    var token = document.getElementsByName("token")[0].value;
    $.ajax({
        url : "statisticsAction",
        type : "POST",
        timeout: 0,
        data : {
            inputDays : activeBtn,
            emailId : document.getElementById("merchant").value,
            currency : document.getElementById("currency").value,
            dateFrom : document.getElementById("dateFrom").value,
            dateTo : document.getElementById("dateTo").value,
            paymentRegion : document.getElementById("paymentsRegion").value,
            token : token,
            "struts.token.name" : "token",
        },
        success : function(data) {					
            document.getElementById("dvTotalSuccess").innerHTML = data.statistics.totalSuccess;
            document.getElementById("dvTotalFailed").innerHTML = data.statistics.totalFailed;
            document.getElementById("dvTotalRefunded").innerHTML = data.statistics.totalRefunded;
            document.getElementById("dvRefundedAmount").innerHTML = data.statistics.refundedAmount;
            document.getElementById("dvApprovedAmount").innerHTML = data.statistics.approvedAmount;
        },
        error : function(data) { }
    });
}