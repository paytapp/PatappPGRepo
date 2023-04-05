<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>SubAdmin List</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script src="../js/jquery.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script type="text/javascript" src="../js/moment.js"></script>
<script type="text/javascript" src="../js/daterangepicker.js"></script>

<script src="../js/jquery.popupoverlay.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<style>
.displayNone {
	display: none;
}
table.dataTable.display tbody tr.odd {
    background-color: #e6e6ff !important;
}
table.dataTable.display tbody tr.odd > .sorting_1{
	 background-color: #e6e6ff !important;
}
table.display td.center{
	text-align: left !important;
}
.btn:focus{
		outline: 0 !important;
	}
</style>
<script type="text/javascript">
function decodeVal(text){	
	  return $('<div/>').html(text).text();
	}
$(document).ready(
		function() {				
			populateDataTable();
			
			$("#submit").click(
					function(env) {
						/* var table = $('#authorizeDataTable')
								.DataTable(); */
						$('#searchAgentDataTable').empty();
						

						populateDataTable();

					});
		});	
					
function populateDataTable() {		
	var token  = document.getElementsByName("token")[0].value;
	$('#searchAgentDataTable')
			.DataTable(
					{
						dom : 'BTftlpi',
						buttons : [ {
							extend : 'copyHtml5',
							exportOptions : {
								columns : [':visible :not(:last-child)']
							}
						}, {
							extend : 'csvHtml5',
							title : 'Sub Admin',
							exportOptions : {
								columns : [ ':visible :not(:last-child)']
							}
						}, {
							extend : 'pdfHtml5',
							title : 'Sub Admin',
							exportOptions : {
								columns : [ ':visible :not(:last-child)']
							},
							customize: function (doc) {
								    //doc.content[1].table.widths = Array(doc.content[1].table.body[0].length + 1).join('*').split('');
								   doc.defaultStyle.alignment = 'center';
			     					doc.styles.tableHeader.alignment = 'center';



								  }
						}, {
							extend : 'print',
							title : 'Sub Admin',
							exportOptions : {
								columns : [ ':visible :not(:last-child)']
							}
						},{
							extend : 'colvis',
							//           collectionLayout: 'fixed two-column',
							columns : [ 0, 1, 2, 3, 4]
						}],
						"ajax" : {
							"url" : "searchSubAdminAction",
							"type" : "POST",
							"data" : {
						
									token:token,
								    "struts.token.name": "token",
									}
						},
						"bProcessing" : true,
						"bLengthChange" : true,
						"bDestroy" : true,
						"iDisplayLength" : 10,
						"order" : [ [ 1, "desc" ] ],
						"aoColumns" : [										
										{
											"mData" : "agentEmailId",
											"sWidth" : '25%',
										},
										{
											"mData" : "agentFirstName",
											"sWidth" : '20%'
										},
										{
											"mData" : "agentLastName",
											"sWidth" : '20%'
										},
										{
											"mData" : "agentMobile",
											"sWidth" : '20%'
										},
										{
											"mData" : "agentIsActive",
											"sWidth" : '10%'
										},	
										{
											"mData" : null,
											"sClass" : "center",
											"bSortable" : false,
											"mRender" : function() {
												return '<button class="btn btn-info btn-xs">Edit</button>';
											}
										},
										{
											"mData" : "payId",
											"sWidth" : '25%',
											"visible" : false,
										}]
					});

	 $(function() {

		var table = $('#searchAgentDataTable').DataTable();
		$('#searchAgentDataTable tbody')
				.on(
						'click',
						'td',
						function() {

							var columnVisible = table.cell(this).index().columnVisible;
							var rowIndex = table.cell(this).index().row;
							var row = table.row(rowIndex).data();

							var emailAddress = table.cell(rowIndex, 0).data();
							var firstName = table.cell(rowIndex, 1).data();
							var lastName = table.cell(rowIndex, 2).data();
							/* var mobile = table.cell(rowIndex, 3).data();
							var isActive = table.cell(rowIndex, 4).data(); */					

							if (columnVisible == 5) {
								document.getElementById('emailAddress').value = decodeVal(emailAddress);
								document.getElementById('firstName').value = firstName;
								document.getElementById('lastName').value = lastName;
								/* document.getElementById('mobile').value = mobile;
								document.getElementById('isActive').value = isActive; */
								document.agentDetails.submit();
							}							
						});
	});
}
</script>
<script type="text/javascript">
	function MM_openBrWindow(theURL, winName, features) { //v2.0
		window.open(theURL, winName, features);
	}

	function displayPopup() {
		document.getElementById('light3').style.display = 'block';
		document.getElementById('fade3').style.display = 'block';
	}
</script>

</head>
<body>
	<table width="100%" align="left" cellpadding="0" cellspacing="0"
		class="txnf">
		<tr>
			<td align="left"><s:actionmessage /></td>
		</tr>
		<tr>
			<td align="left"><h2>Sub Admin List</h2></td>
		</tr>
		<tr>
			<td align="left"><table width="100%" border="0" align="center"
					cellpadding="0" cellspacing="0">
					<tr>
						<td colspan="5" align="left" valign="top">&nbsp;</td>
					</tr>
					<tr>
						<td colspan="5" align="center" valign="top"><table
								width="100%" border="0" cellpadding="0" cellspacing="0">
							</table></td>
					</tr>
				</table></td>
		</tr>
		<tr>
			<td align="left" style="padding: 10px;">
				<div class="scrollD">
					<table width="100%" border="0" cellpadding="0" cellspacing="0"
						id="searchAgentDataTable" class="display">
						<thead>
							<tr class="boxheadingsmall">
								<th>Email</th>
								<th>First Name</th>
								<th>Last Name</th>
								<th>Phone</th>
								<th>Is Active</th>
								<th>Action</th>
								<th></th>
							</tr>
						</thead>
					</table>
				</div>
			</td>
		</tr>
	</table>
	<s:form name="agentDetails" action="editSubAdmin">
		<s:hidden name="emailId" id="emailAddress" value="" />
		<s:hidden name="firstName" id="firstName" value="" />
		<s:hidden name="lastName" id="lastName" value="" />
		<%-- <s:hidden name="mobile" id="mobile" value="" />
		<s:hidden name="isActive" id="isActive" value="" /> --%>
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>
</body>
</html>