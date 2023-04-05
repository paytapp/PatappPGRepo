<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>

<style>

.heading{
   text-align: center;
    color: #002163;
    font-weight: bold;
    font-size: 22px;
}
.samefnew {
    width: 18.9% !important;
    float: left;
    color: #333;
    line-height: 22px;
    margin: -8px 0 0 10px !important;
}
.cust {
    width: 20% !important;
    float: left;
    color: #333;
    line-height: 22px;
    margin: -8px 0 0 0px !important;
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
.form-control {
    display: block;
    width: 102% !important;
    height: 28px;
    padding: 3px 4px;
    font-size: 14px;
    line-height: 1.42857143;
    color: #555;
    background-color: #fff;
    background-image: none;
    border: 1px solid #ccc;
    border-radius: 4px;
    -webkit-box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    box-shadow: inset 0 1px 1px rgba(0,0,0,.075);
    -webkit-transition: border-color ease-in-out .15s,-webkit-box-shadow ease-in-out .15s;
    -o-transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
    transition: border-color ease-in-out .15s,box-shadow ease-in-out .15s;
    margin-left: -2% !important;
}
table.dataTable.display tbody tr.odd {
    background-color: #e6e6ff !important;
}
table.dataTable.display tbody tr.odd > .sorting_1{
	 background-color: #e6e6ff !important;
}	

</style>

<title>Refund Preview Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
	<link href="../css/Jquerydatatable.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/loader.css">
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script type="text/javascript" src="../js/moment.js"></script>
	<script type="text/javascript" src="../js/daterangepicker.js"></script>
	<link href="../css/loader.css" rel="stylesheet" type="text/css" />
	<script src="../js/jquery.popupoverlay.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
	<script src="../js/commanValidate.js"></script>
	<link href="../css/select2.min.css" rel="stylesheet" />
    <script src="../js/jquery.select2.js" type="text/javascript"></script>
<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />
	
<script type="text/javascript">
$(document).ready(function(){
 
  // Initialize select2
  $("#merchant").select2();
});
</script>

<script type="text/javascript">

$(function() {
	
	var table = $('#deltaRefundPreviewReportDatatable').DataTable({
		
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
				title : 'Refund Preview Report',
				exportOptions : {
					
					columns : [':visible']
				},
			},
			{
				extend : 'pdfHtml5',
				orientation : 'landscape',
				title : 'Refund Preview Report',
				exportOptions : {
					columns : [':visible']
				}
			},
			{
				extend : 'print',
				title : 'Refund Preview Report',
				exportOptions : {
					columns : [ 0, 1, 2, 3, 4, 5]
				}
			}
		],
				"searching": true,
				"paging": true,
                "lengthMenu": [ [10, 25, 50, 100, -1], [10, 25, 50, 100, "All"] ],
                "pagingType": "full_numbers",
                "pageLength": 10,
	});
	
    $('#submit').on('click', function() {
		 var merchant = document.getElementById("merchant").value;
		 var dateFrom = document.getElementById("dateFrom").value;
		 
		 if(merchant == "Select Merchant") {
			 alert("Select a merchant !!");
			 return;
		 }
		 
		 var token  = document.getElementsByName("token")[0].value;
		 $("body").removeClass("loader--inactive");
		 
		 //table.destroy();
         //$('#gstReportDatatable').empty();
         var buttonCommon = {
        exportOptions: {
            format: {
                body: function ( data, column, row, node ) {
                    // Strip $ from salary column to make it numeric
                    return column === 0 ? "'"+data : (column === 4 ? "'" + data: column === 3 ? "'" + data:data);
                }
            }
        }
    };
		 
		 
        var table = $('#deltaRefundPreviewReportDatatable').DataTable({
			dom: 'BTrftlpi',
	               
				buttons : [
					$.extend( true, {}, buttonCommon,{
						extend : 'copyHtml5',
						exportOptions : {
							columns : [':visible']
						}
					}),
					$.extend( true, {}, buttonCommon,{
						extend: 'csvHtml5',
						title : 'Refund Preview Report',
						exportOptions : {
							columns : [':visible']
						},
					}),
					$.extend( true, {}, buttonCommon,{
						extend : 'pdfHtml5',
						orientation : 'landscape',
						title : 'Refund Preview Report',
						exportOptions : {
							columns : [':visible']
						}
					}),
					{
						extend : 'print',
						title : 'Refund Preview Report',
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
			"destroy": true,
			"ajax": {
			    "url": "refundPreviewAction",
			    "type": "POST",
			    "data": {
					"merchant":merchant,
					"dateFrom":dateFrom,
					"struts.token.name": "token",
					},
         
					success: function (response) {
						$("body").addClass("loader--inactive");
							
		               }  
			  },
			  
			  
			  "columns": [
				  { "data": "pgRefNo" },
		            { "data": "refundFlag" },
		            { "data": "amount" },
		            { "data": "orderId" },
		            { "data": "payId" },
		            { "data": "saleDate" },
		            { "data": "settledDate" }
		        ]
        });
    });
	$('#submitApprove').on('click', function() {
		if (confirm("Are you sure you want to approve the transactions??")) {
		var merchant = document.getElementById("merchant").value;
		 var dateFrom = document.getElementById("dateFrom").value;
		 //var dateTo = document.getElementById("dateTo").value;
		 
		 var token  = document.getElementsByName("token")[0].value;
		 
		 $.ajax({
			 "url": "refundApproveCheckAction",
				    "type": "POST",
				    "timeout" : 0,
				    "data": {
						"merchant":merchant,
						"dateFrom":dateFrom,
						"struts.token.name": "token",
						},
						success: function(response){
							if(response.response == "false") {
								if (confirm("There is no reco transaction available on the selected date !! Do you want to continue for refund ??")) {
									$.ajax({
											 "url": "refundApproveAction",
												    "type": "POST",
												    "data": {
														"merchant":merchant,
														"dateFrom":dateFrom,
														"struts.token.name": "token",
														},
														success: function(response){
															if(response.response != null) {
																alert(response.response);
										       					$('#deltaRefundPreviewReportDatatable').DataTable().clear().draw();
																
															}
										    			}
										 }); 
								} 
									} else if(response.response == "true") {
										$.ajax({
											 "url": "refundApproveAction",
												    "type": "POST",
												    "data": {
														"merchant":merchant,
														"dateFrom":dateFrom,
														"struts.token.name": "token",
														},
														success: function(response){
															if(response.response != null) {
																alert(response.response);
										       					$('#deltaRefundPreviewReportDatatable').DataTable().clear().draw();
																
															}
										    			}
										 });
									}
				    			}
					 });
	 			} 
		 
			});
});
</script>


</head>
<body id="mainBody">
	
	<h2 class="heading">Delta Refund Preview Report</h2>
	<br>
	<br>
	 <table class="table98 padding0">
        
        <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
    
	
     
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
									headerKey="Select Merchant" headerValue="Select Merchant" list="merchantList"
									listKey="emailId" listValue="businessName" autocomplete="off" />
							</s:if>
							<s:else>
								<s:select name="merchant" class="form-control" id="merchant"
									headerKey="Select Merchant" headerValue="Select Merchant" list="merchantList"
									listKey="emailId" listValue="businessName" autocomplete="off" />
							</s:else>
						</div>
					</div>
					
					<div class="samefnew">
						<div style="margin-left: 5px !important;">Date From:</div>
						<div class="txtnew">
							<s:textfield type="text" id="dateFrom" name="dateFrom" class="form-control" autocomplete="off" readonly="true"
                              style="margin-left: 5px !important;"/>
						</div>
						
					</div>

					<%-- <div class="samefnew">
						<div style="margin-left: 12px !important;">Date To:</div>
						<div class="txtnew">
							<s:textfield type="text" id="dateTo" name="dateTo" class="form-control" autocomplete="off" readonly="true" 
							style="margin-left: 15px !important;"/>
						</div>
						
					</div> --%>
					
					<div class="samefnew-btn">
						&nbsp;<br>
						<div class="txtnew">
								<div>
									 
									  <input type="button" id="submit" value="Submit"
								       class="btn btn-sm btn-block btn-success submit-button" style="margin-left: 35px; 
									   width: 18% !important;">
									   
									    <input type="button" id="submitApprove" value="Approve"
								         class="btn btn-sm btn-block btn-primary submit-button" style="width: 17% !important;">
									
							    </div>
						</div>
						
					</div>

					
				</div>
			</td>
		</tr>
		
		<tr>
				<td align="center" valign="top">
				<s:actionmessage id="success" class="success success-text" />
			
		</tr>
		
		 <tr>
						<td align="left" valign="top" style="padding: 10px; border-right: 1px solid #e0d2d2c4;"><br>
							<div class="scrollD">
								<table id="deltaRefundPreviewReportDatatable" align="center" class="display" cellspacing="0" width="100%">
									<thead>
										<tr class="boxheadingsmall" style="font-size: 11px;">
											<th style="text-align:left;" data-orderable="false">PG Ref Number</th>
											<th style="text-align:left;" data-orderable="false">Refund Flag</th>
											<th style="text-align:left;" data-orderable="false">Amount</th>
											<th style="text-align:left;" data-orderable="false">Order Id</th>
										    <th style="text-align:left;" data-orderable="false">Pay Id</th>
											<th style="text-align:left;" data-orderable="false">Sale Date</th>
											<th style="text-align:left;" data-orderable="false">Settled Date</th>
										</tr>
									</thead>								
								</table>
							</div>
						</td>
					</tr>		
	    </table>
                
    
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
				minDate : "05-12-18",
				maxDate : new Date()
			});
			/* $("#dateTo").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			}); */
		});
		$(function() {
			var today = new Date();
			//$('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
			$('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));	

		});		
	});	
</script>


</body>
</html>