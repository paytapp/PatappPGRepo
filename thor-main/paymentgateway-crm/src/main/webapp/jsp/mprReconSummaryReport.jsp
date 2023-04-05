<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>

<style>

.nav {
	    margin-bottom: 18px;
	    margin-left: 0;
		list-style: none;
	}

.nav > li > a {
		display: block;
	}

.nav-tabs{
		*zoom: 1;
	}

.nav-tabs:before,
.nav-tabs:after {
		display: table;
		content: "";
	}

.nav-tabs:after {
		clear: both;
	}

.nav-tabs > li {
		float: left;
	}

.nav-tabs > li > a {
		padding-right: 12px;
		padding-left: 12px;
		margin-right: 2px;
		line-height: 14px;
	}

.nav-tabs {
		border-bottom: 1px solid #ddd;
	}

.nav-tabs > li {
		margin-bottom: -1px;
	}

.nav-tabs > li > a {
		padding-top: 8px;
	    padding-bottom: 8px;
		line-height: 18px;
		border: 1px solid transparent;
		-webkit-border-radius: 4px 4px 0 0;
		-moz-border-radius: 4px 4px 0 0;
		border-radius: 4px 4px 0 0;
	}

.nav-tabs > li > a:hover {
		border-color: #eeeeee #eeeeee #dddddd;
	}

.nav-tabs > .active > a,
.nav-tabs > .active > a:hover {
		color: #555555;
		cursor: default;
		background-color: #ffffff;
		border: 1px solid #ddd;
	    border-bottom-color: transparent;
	}

li {
		line-height: 18px;
	}

.tab-content.active{
		display: block;
	}

.tab-content.hide{
				display: none;
}
.nav-tabs>li>a:hover{border-top: 0px solid transparent;}
.uploadButton{
    border: none;
	background-color: #002163;
    border-radius: 5px;
    width: 25%;
    font-size: 18px;
	color:white;
}
.heading{
   text-align: center;
    color: black;
    font-weight: bold;
    font-size: 22px;
}
.txtnew {
    /* display: inline-block; */
    /* max-width: 100%; */
    display: block;
    text-align: left;
}
.form-control{
	margin-left: 0 !important;
	width: 100% !important;
}
.divalignment{
	  margin-top: -30px !important;
  }
  
.case-design{
	  text-decoration:none;
	  cursor: default !important;
}
 .my_class:hover{
	  color: white !important;
 }
.multiselect {
    width: 210px;
	display:block;
	margin-left:-20px;	
 }
  .selectBox {
  position: relative;
 }

#checkboxes {
  display: none;
  border-radius: 5px;
  border: 1px #dadada solid;
  height:170px;
  overflow-y: scroll;
  position:Absolute;
  background:#fff;
  z-index:1;
  margin-left:2px;
  margin-right:10px;
}

#checkboxes label {
  width: 74%;
}
#checkboxes input {
  width:18%;

}
#acqCheckbox {
  display: none;
  border-radius: 5px;
  border: 1px #dadada solid;
  height:300px;
  overflow-y: scroll;
  position:Absolute;
  background:#fff;
  z-index:1;
  margin-left:2px;
  margin-right:10px;
}



#acqCheckbox label {
  width: 74%;
}
#acqCheckbox input {
  width:18%;

}
.selectBox select {
  width: 95%;
  
}
.overSelect {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
}
.download-btn {
	background-color:#002163;
	display: block;
    width: 100%;
    height: 30px;
    padding: 3px 4px;
    font-size: 14px;
    line-height: 1.42857143;
    color: #fff;
    border: 1px solid #ccc;
    border-radius: 4px;
	margin-top:30px;
}
.form-control{
	margin-left: 0!important;
	width: 100% !important;
}
.padding10{
	padding: 10px;
}
.disabled {
    color:#fff;
	border-color: #a0a0a0;
	background-color: #a0a0a0;
}
label{
	margin-bottom: -10px !important;
}	
.aboveHead{
	display: block;
    text-align: left;
}	
.recon-btn{
	background-color: #002163;
    width: 45%;
    padding: 3px 4px;
    font-size: 15px;
    line-height: 1.42857143;
    color: #fff;
    border: 1px solid #ccc;
    border-radius: 4px;
}
#mprReconSummaryDatatable_info{
	margin-left: -23% !important;
}
#mprReconSummaryDatatable_length{
	margin-left: -13% !important;
}
</style>

<title>MPR Recon Summary Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/loader.css">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/moment.js"></script>
<script type="text/javascript" src="../js/daterangepicker.js"></script>
<link href="../css/loader.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery.popupoverlay.js"></script>
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>  
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script> 
<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>
	
<script type="text/javascript">
$(document).ready(function(){
   $("body").removeClass("loader--inactive");
  // Initialize select2
  $("#merchant").select2();
});
</script>

	
<script>
	$(document).ready(function() {
		$('.nav-tabs > li > a').click(function(event){
		event.preventDefault();//stop browser to take action for clicked anchor

		//get displaying tab content jQuery selector
		var active_tab_selector = $('.nav-tabs > li.active > a').attr('href');

		//find actived navigation and remove 'active' css
		var actived_nav = $('.nav-tabs > li.active');
		actived_nav.removeClass('active');

		//add 'active' css into clicked navigation
		$(this).parents('li').addClass('active');

		//hide displaying tab content
		$(active_tab_selector).removeClass('active');
		$(active_tab_selector).addClass('hide');

		//show target tab content
		var target_tab_selector = $(this).attr('href');
		$(target_tab_selector).removeClass('hide');
		$(target_tab_selector).addClass('active');
	     });
	  });
</script>

<!--------------------------------FIRST AJAX----------------------------------------------->
<script type="text/javascript">
$(function() {
	
	 table = $('#mprReconSummaryDatatable').DataTable({
		
		dom: 'BTrftlpi',
	               destroy : true,
	               buttons : [
							{
								extend : 'copyHtml5',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend: 'csvHtml5',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								},
							},
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend : 'print',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								}
							}
						],
				"searching": false,
				"paging": true,
                "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
                "pagingType": "full_numbers",
                "pageLength": 10,
	});
	
    $('#Submit').on('click', function() {   
	     var acquirer = document.getElementById("acquirer").value;
		 var paymentType = document.getElementById("paymentType").value;
		 var dateFrom = document.getElementById("dateFrom").value;
		 var token  = document.getElementsByName("token")[0].value;
		 $("body").removeClass("loader--inactive");
         table = $('#mprReconSummaryDatatable').DataTable({
			 "columnDefs": [
				        {
							"className": "dt-center", 
						"targets": "_all"
						}
				],
            dom: 'BTrftlpi',			
		               buttons : [
							{
								extend : 'copyHtml5',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend: 'csvHtml5',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									
									columns : [':visible']
								},
							},
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend : 'print',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								}
							}
						],
			"searching": false,
			"paging": true,
            "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
            "pagingType": "full_numbers",
            "pageLength": 10,
			destroy: true,                                  
			ajax: function (data, callback, settings) {
                           $.ajax({
                                         "url": "mprReconSummaryReportAction",
                                       "type": "POST",
                                       timeout: 0,
                                       "data": {
                                            "acquirer":acquirer,
                                            "paymentType": paymentType,
                                                "dateFrom": dateFrom,
                                            "struts.token.name": "token"
                                        },
                                        success:function(data){
										 $("body").removeClass("loader--inactive");
                                         callback(data);
										 document.getElementById("saleAmuntFoot").innerHTML = data.totalSaleMprAmount;
										 document.getElementById("saleTxnAmt").innerHTML = data.totalSaleTxn;
										 document.getElementById("refundAmuntFoot").innerHTML = data.totalRefundMprAmount;
										 document.getElementById("refundTxnFoot").innerHTML = data.totalRefundTxn;
										 document.getElementById("refundNetTxn").innerHTML = data.netTxn;
                                         document.getElementById("netAmuntFoot").innerHTML = data.netAmount;
                                         document.getElementById("amountCreditFoot").innerHTML = data.creditNodalAmount;
                                         document.getElementById("creditDateFoot").innerHTML = data.reconDate;
                                         document.getElementById("diffrenceFoot").innerHTML = data.differenceAmount;
										 
                                        },
										error:function(data) {
											  $("body").removeClass("loader--inactive");
										  }
                                        });
                                  },
				  
		"columns": [
				    { "data": "acquirer" },
					{ "data": "merchant" },
		            { "data": "nodalAccount" },
					{ "data": "paymentType" },
		            { "data": "saleTxn" },
					{ "data": "saleMprAmount" },
		            { "data": "refundTxn" },
					{ "data": "refundMprAmount" },
		            { "data": "netTxn" },
					{ "data": "netMprAmount" },
		            { "data": "amountCreditNodal" },
					{ "data": "amountCreditDate" },
		            { "data": "amountNodalDifference" },
		    
		        ]
        });
    });
});
</script>

<style>
.messageClass{
	text-align: center;
    color: black;
    background: gainsboro;
    font-size: 14px;
}
#loading {width: 100%;height: 100%;top: 0px;left: 0px;position: fixed;display: block; z-index: 99}
#loading-image {position: absolute;top: 40%;left: 55%;z-index: 100; width:10%;}
</style>
	
</head>
<body id="mainBody">
	
	<h2 class="pageHeading">MPR Reconcilation Summary Report</h2>
	<h6 class="messageClass" id="responseMessage"></h6>
	<br>
	<br>
	 <table class="table98 padding0">
        
        <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
        <ul class="nav nav-tabs" style="border-bottom:none;">
        <li class="active"><a href="#mprReconReport" onclick="disableMsg();">MPR Recon Summary Report</a></li>
        <li><a href="#utilityReport" onclick="enableMsg();">Nodal Amount Report</a></li>
    </ul>
    </td>
        </tr>
 
  <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
    
	<!----------------------------FIRST TAB CONTENT------------------------->
      <section id="mprReconReport" class="tab-content active">
        <div>
            <br/>
               <div class="container">

					<div class="row padding10">
						
						<div class="form-group  col-md-3 col-sm-4 txtnew  col-xs-6">
                                  <label for="aquirer" class="aboveHead">Acquirer</label> <br />
                                          <div class="txtnew">
                                            <s:select headerKey="ALL" headerValue="ALL" list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()" 
                                                  name="acquirer" value="name" class="form-control" id="acquirer" listValue="name" listKey="code"
												  />
                                           </div>
                         </div>
					
						<div class="form-group  col-md-3 col-sm-4 txtnew  col-xs-6">
							<label for="email" class="aboveHead">Payment Type:</label> <br />
							   <select class="form-control" style="margin-left: 2px !important; width:75% !important;" id="paymentType">
							    <option>ALL</option>
							    <option>CC-DC</option>
								<option>UPI</option>
							   </select>
						</div>

				
						<div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
							<label for="dateFrom" style="margin-left:-40% !important;" class="aboveHead">MPR Date:</label> <br />
							<s:textfield type="text" readonly="true" id="dateFrom"
								name="dateFrom" class="form-control" onchange="handleChange();" 
								autocomplete="off" style="margin-left: -40% !important; width:150% !important;"/>
						</div>
					
					</div>
					
					<div>
						<div class="form-group col-md-2 col-sm-3 txtnew col-xs-6 col-md-offset-5">
							   <button class="download-btn" id="Submit">Submit</button>
						</div>
						
					</div>
					
				</div>
            </div>
			
			<table>
		    <tr>
				<div class="scrollD">
					<table id="mprReconSummaryDatatable" align="center" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="boxheadingsmall" style="font-size: 11px;">
									<th style="text-align:center;" data-orderable="false">Acquirer Name</th>
									<th style="text-align:center;" data-orderable="false">Merchant</th>
									<th style="text-align:center;" data-orderable="false">Nodal Account</th>
									<th style="text-align:center;" data-orderable="false">Payment Type</th>
									<th style="text-align:center;" data-orderable="false">Sale No of Txn</th>
									<th style="text-align:center;" data-orderable="false">Sale MPR Amount</th>
									<th style="text-align:center;" data-orderable="false">Refund No Of txn</th>
									<th style="text-align:center;" data-orderable="false">Refund MPR Amount</th>
									<th style="text-align:center;" data-orderable="false">Total No of txn</th>
									<th style="text-align:center;" data-orderable="false">Net Amount in MPR</th>
									<th style="text-align:center;" data-orderable="false">Amount Credit in Nodal</th>
									<th style="text-align:center;" data-orderable="false">Amount credited date</th>
									<th style="text-align:center;" data-orderable="false">Diffrence</th>

								</tr>
							</thead>
							<tbody id="tableContent">
									
							</tbody>
							
							<tfoot>
                                <tr align="center" class="display" cellspacing="0" width="100%" style="background:#002163;">
                                   <th></th>
                                   <th></th>
                                   <th></th>
                                   <th></th>
                                   <th style="text-align:center;" data-orderable="false"><p id="saleTxnAmt" style="color:white;">0.00</p></th>
                                   <th style="text-align:center;" data-orderable="false"><p id="saleAmuntFoot" style="color:white;">0.00</p></th>
                                   <th style="text-align:center;" data-orderable="false"><p id="refundTxnFoot" style="color:white;">0.00</p></th>
                                   <th style="text-align:center;" data-orderable="false"><p id="refundAmuntFoot" style="color:white;">0.00</p></th>
                                   <th style="text-align:center;" data-orderable="false"><p id="refundNetTxn" style="color:white;">0.00</p></th>
                                   <th style="text-align:center;" data-orderable="false"><p id="netAmuntFoot" style="color:white;">0.00</p></th>
                                   <th style="text-align:center;" data-orderable="false"><p id="amountCreditFoot" style="color:white;">0.00</p></th>
                                   <th style="text-align:center;" data-orderable="false"><p id="creditDateFoot" style="color:white;">0.00</p></th>
                                   <th style="text-align:center;" data-orderable="false"><p id="diffrenceFoot" style="color:white;">0.00</p></th>
                                </tr>
                            </tfoot>
					</table>
				</div>
						
			</tr>
		    </table>
        </section>
       
	   <!----------------------------SECOND TAB CONTENT------------------------->
       <section id="utilityReport" class="tab-content hide">
        <div>
            <br/>
			<div class="container" >

					<div class="row padding10">
					
					
						<div class="form-group col-md-3 txtnew col-sm-4 col-xs-6">
							<label for="merchant" style="margin-left: 2px;" class="aboveHead">Recon Date:</label> <br />
							<s:textfield type="text" readonly="true" id="createDate" name="createDate"
								class="form-control" onchange="handleChange();" autocomplete="off"/>
						</div>


						<div class="form-group  col-md-3 col-sm-4 txtnew  col-xs-6">
								<label for="aquirer" class="aboveHead">Acquirer</label> <br />
								<div class="txtnew">
							<s:select headerKey="Select Acquirer" headerValue="Select Acquirer" class="form-control" list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()" 
                              listValue="name" listKey="code" id="aquirer" name="aquirer" value="aquirer" />
						</div>
						</div>
					
						<div class="form-group  col-md-3 col-sm-4 txtnew  col-xs-6">
							<label for="email" class="aboveHead">Payment Type:</label> <br />
							   <select class="form-control" style="margin-left: 2px !important; width:95% !important;" id="paymentUtil">
							    <option>Select Payment</option>
							    <option>CC-DC</option>
								<option>UPI</option>
							   </select>
						</div>

				
						<div class="form-group  col-md-3 col-sm-4 txtnew col-xs-6">
							<label for="dateFrom" class="aboveHead">Nodal Amount:</label> <br />
							   <input class="form-control" id="nodalAmount" onkeypress="return Validate(event);">
						</div>
					
					</div>
					
					<div>
						<div class="form-group col-sm-3 txtnew col-xs-6 col-md-offset-5">
							   <button class="recon-btn" id="utilitySubmit">Submit</button>
							    <button class="recon-btn" id="viewRecon">View</button>
						</div>
					</div>
					
				</div>
             
        </div>
		
		<table>
		    <tr>
				<div class="scrollD">
					<table id="utilitiesSummaryDatatable" align="center" class="display" cellspacing="0" width="100%">
							<thead>
								<tr class="boxheadingsmall" style="font-size: 11px;">
						
									<th style="text-align:center;" data-orderable="false">Acquirer</th>
									<th style="text-align:center;" data-orderable="false">Payment Type</th>
									<th style="text-align:center;" data-orderable="false">Nodal Amount</th>
									<th style="text-align:center;" data-orderable="false">Recon Date</th>
								</tr>
							</thead>
							<tbody id="tableContent">
									
							</tbody>
					</table>
				</div>
						
			</tr>
		    </table>
        </section>
		
      </td>
    </table>
<script type="text/javascript">
	 function handleChange() {
     }
	$(document).ready(function() {
		
		$(function() {
			$("#dateFrom").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			$("#dateTo").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
		});
		$(function() {
			var today = new Date();
			$('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
			$('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
			

		});
	});
	
			
</script>
<script type="text/javascript">
	 function handleChange() {
     }
	$(document).ready(function() {
		
		$(function() {
			$("#createDate").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			
		});
		$(function() {
			var today = new Date();
			$('#createDate').val($.datepicker.formatDate('dd-mm-yy', today));
			//$('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
			

		});
	});
	
			
</script>

<script type="text/javascript">

$(function() {
	
	 utilityTable = $('#utilitiesSummaryDatatable').DataTable({
		
		dom: 'BTrftlpi',
	               destroy : true,
	               buttons : [
							{
								extend : 'copyHtml5',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend: 'csvHtml5',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								},
							},
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend : 'print',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								}
							}
						],
				"searching": true,
				"paging": true,
                "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
                "pagingType": "full_numbers",
                "pageLength": 10,
	});
	
	
	
	
	 $('#utilitySubmit').on('click', function() { 
	     document.getElementById("responseMessage").style.display = "block";
		 var createDate = document.getElementById("createDate").value;
		 var acquirer = document.getElementById("aquirer").value;
		 
		   if(acquirer == "Select Acquirer") {
			alert("Select Acquirer");
			return;
		  }
		    
		 var paymentType = document.getElementById("paymentUtil").value;
		 
		  if(paymentType == "Select Payment") {
			alert("Select Payment Type");
			return;
		  }
		 var nodalAmount = document.getElementById("nodalAmount").value;
		 
		 if(nodalAmount == ""){
			 alert("Please enter nodal amount");
			 return;
		 }
		 var token  = document.getElementsByName("token")[0].value;
		 $("body").removeClass("loader--inactive");
		 
		 table = $('#utilitiesSummaryDatatable').DataTable({
			 "columnDefs": [
				        {
							"className": "dt-center", 
						"targets": "_all"
						}
				],
            dom: 'BTrftlpi',			
		               buttons : [
							{
								extend : 'copyHtml5',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend: 'csvHtml5',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									
									columns : [':visible']
								},
							},
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend : 'print',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								}
							}
						],
			"searching": true,
			"paging": true,
            "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
            "pagingType": "full_numbers",
            "pageLength": 10,
			destroy: true,                                  
			ajax: function (data, callback, settings) {
         $.ajax({				
				    "url": "nodalAmountAction",
				    "type": "POST",
				    timeout: 0,
				    "data": {
						
						"createDate":createDate,
					    "acquirer":acquirer,
					    "paymentType": paymentType,
						"nodalAmount": nodalAmount,
					    "struts.token.name": "token"
					},
					
					        success:function(data){
								        $("body").removeClass("loader--inactive");
                                        callback(data);
										document.getElementById("responseMessage").innerHTML = data.response;
										 
                                },
							error:function(data) {
						             $("body").removeClass("loader--inactive");
					            }
		        });
				   
				  },

				  
		"columns": [
		          
				    { "data": "acquirer", 
					  "width": "20%"
					},	
		            { "data": "paymentType",
					  "width": "15%"
					},
					{ "data": "nodalCreditAmount",
                      "width": "20%"
					},
		            { "data": "reconDate",
                      "width": "20%"
					}
		        ]
        });
	
});
	
    $('#viewRecon').on('click', function() { 
       //document.getElementById("utilitiesSummaryDatatable").style.display = "block";
        document.getElementById("responseMessage").style.display = "none";	   

	
		 var createDate = document.getElementById("createDate").value;
		 var acquirer = document.getElementById("aquirer").value;
		 
		
		 var paymentType = document.getElementById("paymentUtil").value;
		 
		 
		 var token  = document.getElementsByName("token")[0].value;
		 $("body").removeClass("loader--inactive");
         table = $('#utilitiesSummaryDatatable').DataTable({
			 "columnDefs": [
				        {
							"className": "dt-center", 
						"targets": "_all"
						}
				],
            dom: 'BTrftlpi',			
		               buttons : [
							{
								extend : 'copyHtml5',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend: 'csvHtml5',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									
									columns : [':visible']
								},
							},
							{
								extend : 'pdfHtml5',
								orientation : 'landscape',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								}
							},
							{
								extend : 'print',
								title : 'MPR Recon Summary Report',
								exportOptions : {
									columns : [':visible']
								}
							}
						],
			"searching": true,
			"paging": true,
            "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
            "pagingType": "full_numbers",
            "pageLength": 10,
			destroy: true,  
			ajax: function (data, callback, settings) {
							$.ajax({
										"url": "viewNodalAmountAction",
										"type": "POST",
										timeout: 0,
										"data": {
												"createDate":createDate,
					                            "acquirer":acquirer,
					                            "paymentType": paymentType,
					                            "struts.token.name": "token"
										},
										success:function(data){
											$("body").removeClass("loader--inactive");
											callback(data);
										},
										error:function(data) {
											$("body").removeClass("loader--inactive");
											}
									});
							 },			
				  
		"columns": [
		          
				    { "data": "acquirer", 
					  "width": "20%"
					},	
		            { "data": "paymentType",
					  "width": "15%"
					},
					{ "data": "nodalCreditAmount",
                      "width": "20%"
					},
		            { "data": "reconDate",
                      "width": "20%"
					}
		        ]
        });
		
		
    });
});
</script>

<script>
function Validate(event) {
	  var regex = /^\d+$/;
	    var key = String.fromCharCode(!event.charCode ? event.which : event.charCode);
	    if (!regex.test(key)) {
	       event.preventDefault();
	       return false;
	    }
} 
</script>
	
<script>
function disableMsg(){
	 document.getElementById("responseMessage").style.display = "none";
}

function enableMsg(){
	document.getElementById("responseMessage").style.display = "block";
}
</script>

</body>
</html>