<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
<title>Dashboard</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script src="../js/jquery.min.js"></script>
<link href="../css/custom.css" rel="stylesheet">
<link href="../css/welcomePage.css" rel="stylesheet">
<script src="../js/highcharts.js"></script>
<script src="../js/highchart.exporting.js"></script>
<script type="text/javascript">
//to show new loader --harpreet
$.ajaxSetup({
       global: false,
       beforeSend: function () {
       	toggleAjaxLoader();
       },
       complete: function () {
       	toggleAjaxLoader();
       }
   });
   
function relodDateYearly() {
	var currentDate = new Date();
	var first = currentDate.getDate();
	var last = first - 365;
	var dateTo = new Date(currentDate.setDate(first));
	var dateFrom = new Date(currentDate.setDate(last));
	var token = document.getElementsByName("token")[0].value;

	$.post("chartAction", {
		dateFrom : dateFrom,
		dateTo : dateTo,
		emailId : document.getElementById("merchant").value,
		currency : document.getElementById("currency").value,
		token : token,
		"struts.token.name" : "token"
	}).done(
			function(data) {
				var visacards = parseInt(data.pieChart.visa)
				var master = parseInt(data.pieChart.mastercard)
				var amexcard = parseInt(data.pieChart.amex)
				var mestrocard = parseInt(data.pieChart.maestro)
				var ezeeClickcard = parseInt(data.pieChart.ezeeClick)
				var wallet = parseInt(data.pieChart.other)
				var netbanking = parseInt(data.pieChart.net)
			$(function () {
			    $('#container_piechart').highcharts({
			        chart: {
			            type: 'pie',
			            options3d: {
			                enabled: true,
			                alpha: 45,
			                beta: 0
			            }
			        },
			        title: {
			            text: ''
			        },
			        tooltip: {
			            pointFormat: '{point.percentage:.1f}%</b>'
			        },
			        plotOptions: {
			            pie: {
			                allowPointSelect: true,
			                cursor: 'pointer',
			                depth: 35,
			                dataLabels: {
			                    enabled: true,
			                    format: '{point.name}'
			                }
			            }
			        },
			        series: [{
			            type: 'pie',
			            name: '',
			            data: [
			                ['MASTER ', master],
			                ['NETBANKING', netbanking],
			                {
			                    name: 'VISA ',
			                    y: visacards,
			                    sliced: true,
			                    selected: true
			                },
			                ['AMEX ', amexcard],
			                ['MESTRO ', mestrocard],
			                ['EZEECLICK ', ezeeClickcard],
			                ['WALLET ', wallet],
			                ['DINERS ', 0]
			               
			            ]
			        }]
			    });
			});
		});
	}

		
</script>
<script type="text/javascript">
function relodDateMonthly() {
	var currentDate = new Date();
	var first = currentDate.getDate();
	var last = first - 30;
	var dateTo = new Date(currentDate.setDate(first));
	var dateFrom = new Date(currentDate.setDate(last));
	var token = document.getElementsByName("token")[0].value;
	$.post("chartAction", {
		dateFrom : dateFrom,
		dateTo : dateTo,
		emailId : document.getElementById("merchant").value,
		currency : document.getElementById("currency").value,
		token : token,
		"struts.token.name" : "token"
	}).done(
			function(data) {
				var visacards = parseInt(data.pieChart.visa)
				var master = parseInt(data.pieChart.mastercard)
				var amexcard = parseInt(data.pieChart.amex)
				var mestrocard = parseInt(data.pieChart.maestro)
				var ezeeClickcard = parseInt(data.pieChart.ezeeClick)
				var wallet = parseInt(data.pieChart.other)
				var netbanking = parseInt(data.pieChart.net)
	
			$(function () {
			    $('#container_piechart').highcharts({
			        chart: {
			            type: 'pie',
			            options3d: {
			                enabled: true,
			                alpha: 45,
			                beta: 0
			            }
			        },
			        title: {
			            text: ''
			        },
			        tooltip: {
			            pointFormat: '{point.percentage:.1f}%</b>'
			        },
			        plotOptions: {
			            pie: {
			                allowPointSelect: true,
			                cursor: 'pointer',
			                depth: 35,
			                dataLabels: {
			                    enabled: true,
			                    format: '{point.name}'
			                }
			            }
			        },
			        series: [{
			            type: 'pie',
			            name: '',
			            data: [
			                ['MASTER ', master],
			                ['NETBANKING', netbanking],
			                {
			                    name: 'VISA ',
			                    y: visacards,
			                    sliced: true,
			                    selected: true
			                },
			                ['AMEX ', amexcard],
			                ['MESTRO ', mestrocard],
			                ['EZEECLICK ', ezeeClickcard],
			                ['WALLET ', wallet],
			                ['DINERS ', 0]
			               
			            ]
			        }]
			    });
			});
		});
	}

		
</script>
<script type="text/javascript">
function relodDateHandler() {
	var currentDate = new Date();
	var first = currentDate.getDate();
	var last = first - 6;
	var dateTo = new Date(currentDate.setDate(first));
	var dateFrom = new Date(currentDate.setDate(last));
	var token = document.getElementsByName("token")[0].value;
	$.post("chartAction", {
		dateFrom : dateFrom,
		dateTo : dateTo,
		emailId : document.getElementById("merchant").value,
		currency : document.getElementById("currency").value,
		token : token,
		"struts.token.name" : "token"
	}).done(
			function(data) {
				var visacards = parseInt(data.pieChart.visa)
				var master = parseInt(data.pieChart.mastercard)
				var amexcard = parseInt(data.pieChart.amex)
				var mestrocard = parseInt(data.pieChart.maestro)
				var ezeeClickcard = parseInt(data.pieChart.ezeeClick)
				var wallet = parseInt(data.pieChart.other)
				var netbanking = parseInt(data.pieChart.net)
	
			$(function () {
			    $('#container_piechart').highcharts({
			        chart: {
			            type: 'pie',
			            options3d: {
			                enabled: true,
			                alpha: 45,
			                beta: 0
			            }
			        },
			        title: {
			            text: ''
			        },
			        tooltip: {
			            pointFormat: '{point.percentage:.1f}%</b>'
			        },
			        plotOptions: {
			            pie: {
			                allowPointSelect: true,
			                cursor: 'pointer',
			                depth: 35,
			                dataLabels: {
			                    enabled: true,
			                    format: '{point.name}'
			                }
			            }
			        },
			        series: [{
			            type: 'pie',
			            name: '',
			            data: [
			                ['MASTER ', master],
			                ['NETBANKING', netbanking],
			                {
			                    name: 'VISA ',
			                    y: visacards,
			                    sliced: true,
			                    selected: true
			                },
			                ['AMEX ', amexcard],
			                ['MESTRO ', mestrocard],
			                ['EZEECLICK ', ezeeClickcard],
			                ['WALLET ', wallet],
			                ['DINERS ', 0]
			               
			            ]
			        }]
			    });
			});
		});
	}	
</script>
<script>
	$(document).ready(function() {
		handleChange();
		handlePiChart();
		 $("#buttonDay").click(function(env) {
			  drawChart();
			

		});

		
		$("#button").click(function(env) {
			relodDateHandler();
		     weeklyBar();
		   

		});

		$("#buttonMonthly").click(function(env) {
			relodDateMonthly();
			 monthlyBar();
			
			
		});
		$("#buttonYearly").click(function(env) {
			relodDateYearly();
			yearlyBar();
			
		});
	
	});
	function handleChange() {
		  lineChart();
		 statistics();
		 drawChart();
	}
	 function handlePiChart(){
		 drawChart();
		 drawBarChart();
		
	}
	
	function statistics() {
		var token = document.getElementsByName("token")[0].value;
		$
				.ajax({
					url : "statisticsAction",
					type : "POST",
					data : {
						inputDays : 'year',
						emailId : document.getElementById("merchant").value,
						currency : document.getElementById("currency").value,
						token : token,
						"struts.token.name" : "token",
					},
					success : function(data) {
						document.getElementById("dvTotalSuccess").innerHTML = data.statistics.totalSuccess;
						document.getElementById("dvTotalFailed").innerHTML = data.statistics.totalFailed;
						document.getElementById("dvTotalRefunded").innerHTML = data.statistics.totalRefunded;
						document.getElementById("dvRefundedAmount").innerHTML = data.statistics.refundedAmount;
						document.getElementById("dvApprovedAmount").innerHTML = data.statistics.approvedAmount;
					    drawChart();
					   
					}
				});
		
		
	}
</script>
<script type="text/javascript">
var currentDate = new Date();
var first = currentDate.getDate();
var last = currentDate.getDate() + 1;
var dateFrom = new Date(currentDate.setDate(first));
var dateTo = new Date(currentDate.setDate(last));
function drawChart() {
	var token = document.getElementsByName("token")[0].value;
	$.ajax({
		url : "chartAction",
		type : "POST",
		timeout : 0,
		data : {
			dateFrom : dateFrom,
			dateTo : dateTo,
			emailId : document.getElementById("merchant").value,
			currency : document.getElementById("currency").value,
			token : token,
			"struts.token.name" : "token",
		},
		success : function(data) {
			var visacards = parseInt(data.pieChart.visa)
			var master = parseInt(data.pieChart.mastercard)
			var amexcard = parseInt(data.pieChart.amex)
			var mestrocard = parseInt(data.pieChart.maestro)
			var ezeeClickcard = parseInt(data.pieChart.ezeeClick)
			var wallet = parseInt(data.pieChart.other)
			var netbanking = parseInt(data.pieChart.net)
	
			$(function () {
			    $('#container_piechart').highcharts({
			        chart: {
			            type: 'pie',
			            options3d: {
			                enabled: true,
			                alpha: 45,
			                beta: 0
			            }
			        },
			        title: {
			            text: ''
			        },
			        tooltip: {
			            pointFormat: '{point.percentage:.1f}%</b>'
			        },
			        plotOptions: {
			            pie: {
			                allowPointSelect: true,
			                cursor: 'pointer',
			                depth: 35,
			                dataLabels: {
			                    enabled: true,
			                    format: '{point.name}'
			                }
			            }
			        },
			        series: [{
			            type: 'pie',
			            name: '',
			            data: [
			                ['MASTER ', master],
			                ['NETBANKING', netbanking],
			                {
			                    name: 'VISA ',
			                    y: visacards,
			                    sliced: true,
			                    selected: true
			                },
			                ['AMEX ', amexcard],
			                ['MESTRO ', mestrocard],
			                ['EZEECLICK ', ezeeClickcard],
			                ['WALLET ', wallet],
			                ['DINERS ', 0]
			               
			            ]
			        }]
			    });
			});
			drawBarChart();
		}
	});
}
		
</script>
<script type="text/javascript">
var currentDate = new Date();
var first = currentDate.getDate();
var last = currentDate.getDate() + 1;
var dateFrom = new Date(currentDate.setDate(first));
var dateTo = new Date(currentDate.setDate(last));
function drawBarChart() {
	var token = document.getElementsByName("token")[0].value;
	$.ajax({
		url : "barChartAction",
		type : "POST",
		timeout : 0,
		data : {
			dateFrom : dateFrom,
			dateTo : dateTo,
			emailId : document.getElementById("merchant").value,
			currency : document.getElementById("currency").value,
			token : token,
			"struts.token.name" : "token",
		},
		success : function(data) {
			var cridet = parseInt(data.pieChart.totalCredit)
			var debit = parseInt(data.pieChart.totalDebit)
			var netbaking = parseInt(data.pieChart.net)
			var otherTotal = parseInt(data.pieChart.other)
			$(function () {
			    $('#container_bar').highcharts({
			        chart: {
			            type: 'bar'
			        },
			        title: {
			            text: ''
			        },
			        subtitle: {
			            text: ''
			        },
			        xAxis: {
			            categories: ['CREDIT CARDS', 'DEBIT CARDS', 'NET BANKING', 'OTHER'],
			            title: {
			                text: null
			            }
			        },
			        yAxis: {
			            min: 0,
			            title: {
			                text: 'Number Of Transaction',
			                align: 'high'
			            },
			            labels: {
			                overflow: 'justify'
			            }
			        },
			        tooltip: {
			          
			        },
			        plotOptions: {
			            bar: {
			                dataLabels: {
			                    enabled: true
			                }
			            }
			        },
			        legend: {
			            layout: 'vertical',
			            align: 'left',
			            verticalAlign: 'bottom',
			            floating: true,
			            borderWidth: 1,
			            backgroundColor: ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'),
			            shadow: true
			        },
			        credits: {
			            enabled: false
			        },
			        series: [  {
			        	 name: 'Transaction',
			            data: [cridet, debit, netbaking, otherTotal]
			        }]
			    });
			});
		}
	});
}
</script>
<script type="text/javascript">
function weeklyBar(){
	var currentDate = new Date();
	var first = currentDate.getDate() + 1;
	var last = first - 6;
	var dateTo = new Date(currentDate.setDate(first));
	var dateFrom = new Date(currentDate.setDate(last));
	var token = document.getElementsByName("token")[0].value;
	$.post("barChartAction", {
		dateFrom : dateFrom,
		dateTo : dateTo,
		emailId : document.getElementById("merchant").value,
		currency : document.getElementById("currency").value,
		token : token,
		"struts.token.name" : "token"
	}).done(
			function(data) {
				var cridet = parseInt(data.pieChart.totalCredit)
				var debit = parseInt(data.pieChart.totalDebit)
				var netbaking = parseInt(data.pieChart.net)
				var otherTotal = parseInt(data.pieChart.other)
			$(function () {
			    $('#container_bar').highcharts({
			        chart: {
			            type: 'bar'
			        },
			        title: {
			            text: ''
			        },
			        subtitle: {
			            text: ''
			        },
			        xAxis: {
			            categories: ['CREDIT CARDS', 'DEBIT CARDS', 'NET BANKING', 'OTHER'],
			            title: {
			                text: null
			            }
			        },
			        yAxis: {
			            min: 0,
			            title: {
			                text: 'Number Of Transaction',
			                align: 'high'
			            },
			            labels: {
			                overflow: 'justify'
			            }
			        },
			        tooltip: {
			          
			        },
			        plotOptions: {
			            bar: {
			                dataLabels: {
			                    enabled: true
			                }
			            }
			        },
			        legend: {
			            layout: 'vertical',
			            align: 'right',
			            verticalAlign: 'top',
			            x: -230,
			            y: 145,
			            floating: true,
			            borderWidth: 1,
			            backgroundColor: ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'),
			            shadow: true
			        },
			        credits: {
			            enabled: false
			        },
			        series: [  {
			        	 name: 'Transaction',
			            data: [cridet, debit, netbaking, otherTotal]
			        }]
			    });
			});
		});
	}

</script>

<script type="text/javascript">
function monthlyBar(){
	var currentDate = new Date();
	var first = currentDate.getDate() + 1;
	var last = first - 30;
	var dateTo = new Date(currentDate.setDate(first));
	var dateFrom = new Date(currentDate.setDate(last));
	var token = document.getElementsByName("token")[0].value;
	$.post("barChartAction", {
		dateFrom : dateFrom,
		dateTo : dateTo,
		emailId : document.getElementById("merchant").value,
		currency : document.getElementById("currency").value,
		token : token,
		"struts.token.name" : "token"
	}).done(
			function(data) {
				var cridet = parseInt(data.pieChart.totalCredit)
				var debit = parseInt(data.pieChart.totalDebit)
				var netbaking = parseInt(data.pieChart.net)
				var otherTotal = parseInt(data.pieChart.other)
			$(function () {
			    $('#container_bar').highcharts({
			        chart: {
			            type: 'bar'
			        },
			        title: {
			            text: ''
			        },
			        subtitle: {
			            text: ''
			        },
			        xAxis: {
			            categories: ['CREDIT CARDS', 'DEBIT CARDS', 'NET BANKING', 'OTHER'],
			            title: {
			                text: null
			            }
			        },
			        yAxis: {
			            min: 0,
			            title: {
			                text: 'Number Of Transaction',
			                align: 'high'
			            },
			            labels: {
			                overflow: 'justify'
			            }
			        },
			        tooltip: {
			          
			        },
			        plotOptions: {
			            bar: {
			                dataLabels: {
			                    enabled: true
			                }
			            }
			        },
			        legend: {
			            layout: 'vertical',
			            align: 'right',
			            verticalAlign: 'top',
			            x: -230,
			            y: 145,
			            floating: true,
			            borderWidth: 1,
			            backgroundColor: ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'),
			            shadow: true
			        },
			        credits: {
			            enabled: false
			        },
			        series: [  {
			        	 name: 'Transaction',
			            data: [Credit, debit, netbaking, otherTotal]
			        }]
			    });
			});
		});
	}

</script>
<script type="text/javascript">
function yearlyBar(){
	var currentDate = new Date();
	var first = currentDate.getDate() + 1;
	var last = first - 365;
	var dateTo = new Date(currentDate.setDate(first));
	var dateFrom = new Date(currentDate.setDate(last));
	var token = document.getElementsByName("token")[0].value;
	$.post("barChartAction", {
		dateFrom : dateFrom,
		dateTo : dateTo,
		emailId : document.getElementById("merchant").value,
		currency : document.getElementById("currency").value,
		token : token,
		"struts.token.name" : "token"
	}).done(
			function(data) {
				var cridet = parseInt(data.pieChart.totalCredit)
				var debit = parseInt(data.pieChart.totalDebit)
				var netbaking = parseInt(data.pieChart.net)
				var otherTotal = parseInt(data.pieChart.other)
			$(function () {
			    $('#container_bar').highcharts({
			        chart: {
			            type: 'bar'
			        },
			        title: {
			            text: ''
			        },
			        subtitle: {
			            text: ''
			        },
			        xAxis: {
			            categories: ['CREDIT CARDS', 'DEBIT CARDS', 'NET BANKING', 'OTHER'],
			            title: {
			                text: null
			            }
			        },
			        yAxis: {
			            min: 0,
			            title: {
			                text: 'Number Of Transaction',
			                align: 'high'
			            },
			            labels: {
			                overflow: 'justify'
			            }
			        },
			        tooltip: {
			          
			        },
			        plotOptions: {
			            bar: {
			                dataLabels: {
			                    enabled: true
			                }
			            }
			        },
			        legend: {
			            layout: 'vertical',
			            align: 'right',
			            verticalAlign: 'top',
			            x: -230,
			            y: 145,
			            floating: true,
			            borderWidth: 1,
			            backgroundColor: ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'),
			            shadow: true
			        },
			        credits: {
			            enabled: false
			        },
			        series: [  {
			        	  name: 'Transaction',
			            data: [cridet, debit, netbaking, otherTotal]
			        }]
			    });
			});
		});
	}

</script>

<script type="text/javascript">
function lineChart(){
	var token = document.getElementsByName("token")[0].value;
	$.ajax({
		url : "lineChartAction",
		type : "POST",
		data : {
			inputDays : 'year',
			emailId : document.getElementById("merchant").value,
			currency : document.getElementById("currency").value,
			token : token,
			"struts.token.name" : "token",
		},
		success : function(data) {
			var a=[];
			var b=[];
			var c=[];
			var pieChartList = data.pieChart;
			for (var i = 0; i < pieChartList.length; i++) {
				var piechart = pieChartList[i];
				var success =parseInt(piechart.totalSuccess);
				var refund =parseInt(piechart.totalRefunded);
				var failled =parseInt(piechart.totalFailed);

				if(isNaN(success)){
					success=0;
				}
				if(isNaN(refund)){
					refund=0;
				}
				if(isNaN(failled)){
						failled=0;
				}
				a.push(success);
				b.push(refund);
				c.push(failled);
				
				
			}
$(function () {
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
            categories: ['January', 'February', 'March', 'April','May','June','July','August','September','October','November','December']
        },
        yAxis: {
            title: {
                text: 'Number Of Transaction'
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
            name: 'Total Success',
            data:a
        }, {
            name: 'Total Refunded',
            data: b
        }, {
            name: 'Total Failed',
            data:c
        }]
    });
});
		}
});
}
</script>
</head>
<body onload="handleChange();handlePiChart();" style="margin:0px; padding:0px;">
	<div id="page-inner">


                <div class="row">
                    <div class="col-md-1 col-xs-12">&nbsp;</div>
                    <div class="col-md-8 col-xs-12">
                     <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                        <h1 class="page-headerr">
                            Dashboard
                        </h1>
                        </s:if>	
                         <s:else>
                           <h1 class="page-headerr">
                           Super Admin Dashboard
                        </h1>
                         </s:else>		
                    </div>
                    <div class="col-md-2 col-xs-6">
                        <div class="page-headerr">
                        <s:select name="merchants" class="form-control"
									id="merchant" headerKey="ALL MERCHANTS" headerValue="ALL MERCHANTS"
									listKey="emailId" listValue="businessName"
									list="merchantList" autocomplete="off" onchange="handleChange();"/>
                        </div>			
                        
                    </div>
                    <div class="col-md-1 col-xs-6">
                        <div class="page-headerr" style="border-bottom:none;">
                        <s:select name="currency" id="currency" list="currencyMap" class="form-control" onchange="handleChange();"/>
                        </div>			
                        
                    </div> 
                </div>
				
				
                <!-- /. ROW  -->

                <div class="row">
                    <div class="col-md-2 col-sm-12 col-xs-12 margin-right">
                        <div class="panel  text-center no-boder bg-color-green">
                            <div class="panel-left pull-left blue">
                                <i class="fa fa-bar-chart-o fa-5x"></i>
                                
                            </div>
                            <div class="panel-right pull-right">
								<h3 id="dvTotalSuccess"><s:property value="%{statistics.totalSuccess}"/></h3>
                               <strong> Total Success</strong>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-2 col-sm-12 col-xs-12 margin-right">
                        <div class="panel  text-center no-boder bg-color-blue">
                              <div class="panel-left pull-left green">
                                <i class="fa fa-thumbs-down fa-5x"></i>
								</div>
                                
                            <div class="panel-right pull-right">
							<h3 id="dvTotalFailed"><s:property value="%{statistics.totalFailed}"/></h3>
                               <strong> Total Failed</strong>

                            </div>
                        </div>
                    </div>
                    <div class="col-md-2 col-sm-12 col-xs-12 margin-right">
                        <div class="panel  text-center no-boder bg-color-red">
                            <div class="panel-left pull-left red">
                                <i class="fa fa fa-share-square-o fa-5x"></i>
                               
                            </div>
                            <div class="panel-right pull-right">
							 <h3 id="dvTotalRefunded"><s:property value="%{statistics.totalRefunded}"/></h3>
                               <strong> Total Refunded </strong>

                            </div>
                        </div>
                    </div>
                    <div class="col-md-2 col-sm-12 col-xs-12 margin-right">
                        <div class="panel  text-center no-boder bg-color-brown">
                            <div class="panel-left pull-left brown">
                                <i class="fa fa-share fa-5x"></i>
                                
                            </div>
                            <div class="panel-right pull-right">
							<h3 id="dvRefundedAmount"><s:property value="%{statistics.refundedAmount}"/></h3>
                             <strong>Refunded Amount</strong>

                            </div>
                        </div>
                    </div>
                    <div class="col-md-2 col-sm-12 col-xs-12 margin-right">
                        <div class="panel  text-center no-boder bg-color-magenta">
                            <div class="panel-left pull-left magenta">
                                <i class="fa fa-line-chart fa-5x"></i>
                                
                            </div>
                            <div class="panel-right pull-right">
							<h3 id="dvApprovedAmount"><s:property value="%{statistics.approvedAmount}"/></h3>
                             <strong>Approved Amount</strong>

                            </div>
                        </div>
                    </div>
                </div>
                <div class="row">
				<div class="col-md-12 col-xs-12">
					<div class="panel panel-default">
						<div class="borderbottom">
						
						<div class="row">
						<div class="col-md-12 col-xs-12">Monthly Transaction</div>						
						</div>
						
						</div>
						<div class="panel-body scrollD">
                       <div id="container" style="min-width: 310px; height: 200px; margin: 0 auto"></div>
						</div>							
					</div>		
				</div> 
				</div> 	
                <div class="njnew">
				<div class="row"><div class="col-md-12 text-center"><div class="newteds">
									<button type="button" id="buttonDay" class="newround">Day</button>
									<button type="button" id="button"
										class="newround">Week</button>
									<button type="button" id="buttonMonthly"
										class="newround">Month</button>
									<button type="button" id="buttonYearly"
										class="newround">Year</button>
								</div></div></div>
                  <div class="paddrr">              
                <div class="row">
                    <div class="col-md-7 col-sm-12 col-xs-12">
                        <div class="panel panel-default">
                     <div class="borderbottom">
                               Success Transaction
                            </div>
                            <div class="panel-body scrollD">
                             <div id="container_bar" style="min-width: 310px; max-width: 800px; height: 200px; margin: 0 auto"></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-5 col-sm-12 col-xs-12">
                        <div class="panel panel-default">
                           <div class="borderbottom">
                              Transaction Ratio 
                            </div>
                            <div class="panel-body scrollD">
                             <div id="container_piechart" style="height: 200px"></div>
                            </div>
                        </div>
                    </div>
    
                <div class="clear"></div>
                </div>
                <div class="clear"></div>
					</div>
                    </div>           
     </div>
</body>
</html>