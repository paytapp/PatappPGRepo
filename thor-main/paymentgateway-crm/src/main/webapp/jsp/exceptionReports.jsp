<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>Exception Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/moment.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>

<script src="../js/jszip.min.js" type="text/javascript"></script>
<script src="../js/vfs_fonts.js" type="text/javascript"></script>
<script src="../js/buttons.colVis.min.js" type="text/javascript"></script>

<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>

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
        .nav-tabs>li>a:hover{border-top: 0px solid transparent;
	}

.heading{
   text-align: center;
    color: black;
    font-weight: bold;
    font-size: 22px;
}
.samefnew {
    width: 15.5% !important;
    float: left;
    font: bold 13px arial !important;
    color: #333;
    line-height: 22px;
    margin: 0 0 0 10px;
}
.cust {
    width: 20% !important;
    float: left;
    font: bold 13px arial !important;
    color: #333;
    line-height: 22px;
    margin: 0 0 0 0px !important;
}
.submit-button{
	width:10% !important;
	height:28px !important;
	margin-top:-4px !important;
}
.MerchBx {
    min-width: 92%;
    margin: 15px;
    margin-top: 25px !important;
    padding: 0;
}

table.dataTable thead .sorting {
    background: none !important;
}
.sorting_asc {
    background:none !important;
}
table.dataTable thead .sorting_desc {
    background: none !important;
}
table.dataTable thead .sorting {
     cursor: default !important;
}
table.dataTable thead .sorting_desc, table.dataTable thead .sorting {
    cursor: default !important;
}
		

</style>

<title>Exception Report</title>
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/moment.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jszip/3.1.3/jszip.min.js" type="text/javascript"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/pdfmake/0.1.36/vfs_fonts.js" type="text/javascript"></script>
<script src="https://cdn.datatables.net/buttons/1.5.2/js/buttons.colVis.min.js" type="text/javascript"></script>

<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>
	
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

<script type="text/javascript">

$(function() {
	
	var bankTable = $('#bankExceptionDatatable').DataTable({
		
		dom: 'Bfrtip',
	               destroy : true,
	               buttons : [
	            	   {
										extend : 'copyHtml5',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									},
									{
										extend : 'csvHtml5',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									},
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									},
									{
										extend : 'print',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									}
								],
				"searching": false,
	});
	
    $('#banksubmit').on('click', function() {
		 var merchant = document.getElementById("merchant").value;
		 var acquirer = document.getElementById("acquirer").value;
		 var status = document.getElementById("status").value;
		 var dateFrom = document.getElementById("dateFrom").value;
		 var dateTo = document.getElementById("dateTo").value;
		 
		 var token  = document.getElementsByName("token")[0].value;
		 
		 //table.destroy();
         //$('#gstReportDatatable').empty();
		 
		 var buttonCommon = {
        exportOptions: {
            format: {
                body: function ( data, column, row, node ) {
                    // Strip $ from salary column to make it numeric
                    return column === 0 ? "'"+data : (column === 1 ? "'" + data: column === 3 ? "'" + data:data);
                }
            }
        }
    };
		 
        var bankTable = $('#bankExceptionDatatable').DataTable({
			dom: 'Bfrtip',
	               buttons : [
	            	   $.extend( true, {}, buttonCommon,{
										extend : 'copyHtml5',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									}),
									$.extend( true, {}, buttonCommon,{
										extend : 'csvHtml5',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									}),
									$.extend( true, {}, buttonCommon,{
										extend : 'pdfHtml5',
										//orientation : 'landscape',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									}),
									{
										extend : 'print',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									}
								],
			"searching": false,
			"destroy": true,
			"ajax": {
			    "url": "bankExceptionReportAction",
			    "type": "POST",
			    "data": {
					"merchant":merchant,
				    "acquirer":acquirer,
					"status":status,
					"dateFrom":dateFrom,
					"dateTo":dateTo,
					"length" :"10",
					"start" : "0", 
					"struts.token.name": "token",
					},						
			  },
				  "columns": [
					  { "data": "pgRefNo" },
			            { "data": "txnId" },
			            { "data": "orderId" },
			            { "data": "acqId" },
			            { "data": "createdDate" },
			            { "data": "status" },
			            { "data": "exception" }
						
			        ]
			
        });
    });
});

</script>



<script type="text/javascript">

$(function() {
	
	var merchantTable = $('#merchantExceptionDatatable').DataTable({
		
		dom: 'Bfrtip',
	               destroy : true,
	               buttons : [
									{
										extend : 'copyHtml5',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5]
										}
									},
									{
										extend : 'csvHtml5',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5]
										}
									},
									{
										extend : 'pdfHtml5',
										//orientation : 'landscape',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5]
										}
									},
									{
										extend : 'print',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5]
										}
									}
								],
				"searching": false,
	});
	
    $('#merchantsubmit').on('click', function() {
		 /* var merchant = document.getElementById("merchantMarchant").value;
		 var acquirer = document.getElementById("merchantAcquirer").value;
		 var status = document.getElementById("merchantStatus").value;
		 var dateFrom = document.getElementById("merchantDateFrom").value;
		 var dateTo = document.getElementById("merchantDateTo").value;
		 
		 var token  = document.getElementsByName("token")[0].value; */
		 
		 //table.destroy();
         //$('#gstReportDatatable').empty();
		 
         var buttonCommon = {
        exportOptions: {
            format: {
                body: function ( data, column, row, node ) {
                    // Strip $ from salary column to make it numeric
                    return column === 0 ? "'"+data : (column === 1 ? "'" + data: column === 3 ? "'" + data:data);
                }
            }
        }
    };
		 
        var merchantTable = $('#merchantExceptionDatatable').DataTable({
			dom: 'Bfrtip',
	               buttons : [
	            	   $.extend( true, {}, buttonCommon,{
										extend : 'copyHtml5',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									}),
									$.extend( true, {}, buttonCommon,{
										extend : 'csvHtml5',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									}),
									$.extend( true, {}, buttonCommon,{
										extend : 'pdfHtml5',
										//orientation : 'landscape',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									}),
									{
										extend : 'print',
										title : 'Exception Report',
										exportOptions : {
											columns : [ 0, 1, 2, 3, 4, 5, 6]
										}
									}
							],
			"searching": false,
			destroy: true,
			"ajax": {
				    "url": "exceptionReportAction",
				    "type": "POST",
				    "data": function (merchantTable){
						return generatePostData(merchantTable);
					}						
				  },
				  "columns": [
					  { "data": "pgRefNo" },
			            { "data": "txnId" },
			            { "data": "orderId" },
			            { "data": "acqId" },
			            { "data": "createdDate" },
			            { "data": "status" },
			            { "data": "exception" }
						
			        ]
			
        });
    });
});

</script>
	
	
</head>
<body id="mainBody">

	<h2 class="heading">Exception Report</h2>
	<br>
	<br>
	 <table class="table98 padding0">
        
        <tr>
          <td align="center">&nbsp;</td>
            <td height="10" align="center">
				<ul class="nav nav-tabs" style="border-bottom:none;">
					<li class="active"><a href="#BankExceptionReport">Bank Exception Report</a></li>
					<li><a href="#MerchantEndExceptionReport">Merchant End Exception Report</a></li>
			    </ul>
			</td>
        </tr>
 
        <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
    
	<!----------------------------FIRST TAB CONTENT------------------------->
      <section id="BankExceptionReport" class="tab-content active">
        <div>
            <br/>
               
            <table id="mainTable" width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="txnf">
		
		    <tr>
			    <td colspan="5" align="left" valign="top">
				<div class="MerchBx">
					
					<div class="cust">
						Merchant:<br>
						<div class="txtnew">
							<s:if
								test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
								<s:select name="merchant" class="form-control" id="merchant"
									headerKey="ALL" headerValue="ALL" list="merchantList"
									listKey="payId" listValue="businessName" autocomplete="off" />
							</s:if>
							<s:else>
								<s:select name="merchant" class="form-control" id="merchant"
									headerKey="ALL" headerValue="ALL" list="merchantList"
									listKey="payId" listValue="businessName" autocomplete="off" />
							</s:else>
						</div>
					</div>
					
					<div class="cust">
						Acquirer<br>
						<div class="txtnew">
							<s:select headerKey="ALL" headerValue="ALL" class="form-control" list="@com.paymentgateway.commons.util.AcquirerType@values()" 
                              listValue="code" listKey="code" id="acquirer" name="acquirer" value="acquirer" autocomplete="off"/>
						</div>
					</div>

					<%-- <div class="samefnew">
						Currency:<br>
						<div class="txtnew">
							<s:select name="currency" id="currency" headerValue="ALL"
								headerKey="ALL" list="currencyMap" class="form-control" />
						</div>
					</div> --%>
					
					<div class="samefnew">
						Status:<br>
						<div class="txtnew">
							<s:select name="status" id="status" headerValue="ALL"
								headerKey="ALL" list="#{'PENDING':'PENDING', 'RESOLVED':'RESOLVED'}" class="form-control" />
						</div>
					</div>
					
					<div class="samefnew">
						Date From:<br>
						<div class="txtnew">
							<s:textfield type="text" id="dateFrom" name="dateFrom" class="form-control" autocomplete="off" readonly="true" />
						</div>
					</div>

					<div class="samefnew">
						Date To:<br>
						<div class="txtnew">
							<s:textfield type="text" id="dateTo" name="dateTo" class="form-control" autocomplete="off" readonly="true" />
						</div>
					</div>
					
					<div class="samefnew-btn">
						&nbsp;<br>
						<div class="txtnew">
							<input type="button" id="banksubmit" value="Submit"
								class="btn btn-sm btn-block btn-success submit-button">
								</input>
						</div>
					</div>
					
					<!-- <div class="samefnew-btn">
						&nbsp;<br>
						<div class="txtnew">
							<input type="button" id="bankRefresh" value="Refresh"
								class="btn btn-sm btn-block btn-success submit-button">
								</input>
						</div>
					</div> -->

					
				</div>
			</td>
		</tr>
		
		 <tr>
						<td align="left" valign="top" style="padding: 10px; border-right: 1px solid #e0d2d2c4;"><br>
							<div class="scrollD">
								<table id="bankExceptionDatatable" align="center" class="display" cellspacing="0" width="100%">
									<thead>
										<tr class="boxheadingsmall" style="font-size: 11px;">
											<th style="text-align:left;" data-orderable="false">PG Ref No</th>
											<th style="text-align:left;" data-orderable="false">Txn ID</th>
											<th style="text-align:left;" data-orderable="false">Order ID</th>
										    <th style="text-align:left;" data-orderable="false">Acquirer</th>
											<th style="text-align:left;" data-orderable="false">Create Date</th>
											<th style="text-align:left;" data-orderable="false">Status</th>
											<th style="text-align:left;" data-orderable="false">Exception</th>
										</tr>
									</thead>
								
								</table>
							</div>
						</td>
					</tr>
		
		
	</table>
                
            </div>
        </section>
       
	   <!----------------------------SECOND TAB CONTENT------------------------->
       <section id="MerchantEndExceptionReport" class="tab-content hide">
        <div>
            <br/>
             <table id="mainTable" width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="txnf" style="border:none;">
		
		    <tr>
			    <td colspan="5" align="left" valign="top">
				<div class="MerchBx">
					
					<div class="cust">
						Merchant:<br>
						<div class="txtnew">
							<s:if
								test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
								<s:select name="merchantMarchant" class="form-control" id="merchantMarchant"
									headerKey="" headerValue="ALL" list="merchantList"
									listKey="payId" listValue="businessName" autocomplete="off" />
							</s:if>
							<s:else>
								<s:select name="merchant" class="form-control" id="merchant"
									headerKey="" headerValue="ALL" list="merchantList"
									listKey="emailId" listValue="businessName" autocomplete="off" />
							</s:else>
						</div>
					</div>
					
					<div class="cust">
						Acquirer<br>
						<div class="txtnew">
							<s:select headerKey="ALL" headerValue="ALL" class="form-control" list="@com.paymentgateway.commons.util.AcquirerType@values()" 
                              listValue="code" listKey="code" id="merchantAcquirer" name="merchantAcquirer" value="acquirer" />
						</div>
					</div>

					<div class="samefnew">
						Status:<br>
						<div class="txtnew">
							<s:select name="merchantStatus" id="merchantStatus" headerValue="ALL"
								headerKey="ALL" list="#{'PENDING':'PENDING', 'RESOLVED':'RESOLVED'}" class="form-control" />
						</div>
					</div>
					
					<div class="samefnew">
						Date From:<br>
						<div class="txtnew">
							<s:textfield type="text" id="merchantDateFrom" name="dateFrom" class="form-control" autocomplete="off" readonly="true" />
						</div>
					</div>

					<div class="samefnew">
						Date To:<br>
						<div class="txtnew">
							<s:textfield type="text" id="merchantDateTo" name="dateTo" class="form-control" autocomplete="off" readonly="true" />
						</div>
					</div>
					
					<div class="samefnew-btn">
						&nbsp;<br>
						<div class="txtnew">
							<input type="button" id="merchantsubmit" value="Submit"
								class="btn btn-sm btn-block btn-success submit-button">
								</input>
						</div>
					</div>

					
				</div>
			</td>
		</tr>
		
		<tr>
						<td align="left" valign="top" style="padding: 10px; border-right: 1px solid #e0d2d2c4;"><br>
							<div class="scrollD">
								<table id="merchantExceptionDatatable" align="center" class="display" cellspacing="0" width="100%">
									<thead>
										<tr class="boxheadingsmall" style="font-size: 11px;">
											<th style="text-align:left;" data-orderable="false">PG Ref No</th>
											<th style="text-align:left;" data-orderable="false">Txn ID</th>
											<th style="text-align:left;" data-orderable="false">Order ID</th>
										    <th style="text-align:left;" data-orderable="false">Acquirer</th>
											<th style="text-align:left;" data-orderable="false">Create Date</th>
											<th style="text-align:left;" data-orderable="false">Status</th>
											<th style="text-align:left;" data-orderable="false">Exception</th>
										</tr>
									</thead>
								
								</table>
							</div>
						</td>
					</tr>
	
		
	</table>
              
        </div>
        </section>
		     
       
    
        </td>
    </table>
	
	

<script type="text/javascript">
	    function handleChange() {
			
     }

	$(document).ready(function() {
		
		$(function() {
			$("#merchantDateFrom").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			$("#merchantDateTo").datepicker({
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
			$('#merchantDateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
			$('#merchantDateTo').val($.datepicker.formatDate('dd-mm-yy', today));
			

		});
	});
	
	function generatePostData(d) {		
		var token = document.getElementsByName("token")[0].value;
		var merchant = document.getElementById("merchantMarchant").value;
		 var acquirer = document.getElementById("merchantAcquirer").value;
		 var status = document.getElementById("merchantStatus").value;
		 var dateFrom = document.getElementById("merchantDateFrom").value;
		 var dateTo = document.getElementById("merchantDateTo").value;
		
		
		var obj = {
			merchant:merchant,
		    acquirer:acquirer,
			status:status,
			dateFrom:dateFrom,
			dateTo:dateTo,
			draw : d.draw,
			length :d.length,
			start : d.start, 
			token : token,
			"struts.token.name" : "token",
		};

		return obj;
	}
	
			
	</script>
	
	
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


</body>
</html>