<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html dir="ltr" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Merchant Accounts</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
</head>
<body>
	<table width="100%" align="left" cellpadding="0" cellspacing="0" class="formbox">		
		<tr>
		<td align="left" style="padding:10px;"><br /><br />
        <div class="scrollD">
	<table id="datatable" class="display" cellspacing="0" width="100%">
		<thead>
			<tr class="boxheadingsmall">
				<th>Pay Id</th>
				<th>Email</th>
				<th>Business Name </th>
				<th>Status</th>
				<th>UserType</th>
				<th>Mobile</th>
				<th>Registration Date</th>
				<th>Edit</th>
				<th>Pay Id</th>
			</tr>
		</thead>
	</table>
    </div>
    </td></tr></table>
    	<s:form name="merchant" action="adminSetup">
		<s:hidden name="payId" id="hidden" value="" />	
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>
	<script type="text/javascript">
	$(document).ready(function() {
		$(function() {
		renderTable();
		});
	});
	function handleChange() {
		reloadTable();
	}
	function decodeVal(text) {
		return $('<div/>').html(text).text();
	}
	function renderTable() {
			var token  = document.getElementsByName("token")[0].value;
			$('#datatable').dataTable({
				dom : 'BTftlpi',
				buttons : [ {
					extend : 'copyHtml5',
					exportOptions : {
						columns : [ ':visible' ]
					}
				}, {
					extend : 'csvHtml5',
					title : 'Merchant List',
					exportOptions : {
						columns : [ 8, 1, 2, 3, 4, 5, 6 ]
					}
				}, {
					extend : 'pdfHtml5',
					title : 'Merchant List',
					exportOptions : {
						columns : [ ':visible' ]
					}
				}, {
					extend : 'print',
					title : 'Merchant List',
					exportOptions : {
						columns : [ 0, 1, 2, 3, 4, 5, 6]
					}
				},{
					extend : 'colvis',
					//           collectionLayout: 'fixed two-column',
					columns : [ 1, 2, 3, 4, 5, 6,7]
				}],			
				"ajax" : {
					"url" : "adminList",
					"type" : "POST",
					"data" : generatePostData
				},
				"bProcessing" : true,
				"bLengthChange" : true,
				"bAutoWidth" : false,
				"iDisplayLength" : 10,
				"order": [[ 5, "desc" ]],
				"aoColumns" : [ {
					"mData" : "payId"
				}, {
					"mData" : "emailId"
				}, 
				{
					"mData" : "businessName"
				},{
					"mData" : "status"
				},{
					"mData" : "userType"
				},	{
					"mData" : "mobile"
				},{
					"mData" : "registrationDate"
				},
				{
					"mData" : null,
					"sClass" : "center",
					"bSortable" : false,
					"mRender" : function() {
						return '<button class="btn btn-info btn-xs" onclick="ajaxindicatorstart1()">Edit</button>';
					}
				},{
					"data" : null,
					"visible" : false,
					"className" : "displayNone",
					"mRender" : function(row) {
			              return "\u0027" + row.payId;
			       }
				} ]
			});
			
			$(document).ready(function() {
				var table = $('#datatable').DataTable();
					$('#datatable tbody').on('click','td',function(){
						var rows = table.rows();
						var columnVisible = table.cell(this).index().columnVisible;
						var rowIndex = table.cell(this).index().row;
						if (columnVisible == 7) {
							var payId = table.cell(rowIndex, 0).data();
							if(payId.length>20){
								return false;
							}
							for(var i =0;i<payId.length;i++){
								var code  = payId.charCodeAt(i);
								if (code < 48 || code > 57){
									return false;
								}
						    }
						document.getElementById('hidden').value = payId;
					    document.merchant.submit();
						}
				});
			});
		}
		function reloadTable() {
			var tableObj = $('#datatable');
			var table = tableObj.DataTable();
			table.ajax.reload();
		}
		function generatePostData() {
			var token = document.getElementsByName("token")[0].value;
			var obj = {				
					token : token,
			};
			return obj;
		}
	</script>
 </body>
</html>