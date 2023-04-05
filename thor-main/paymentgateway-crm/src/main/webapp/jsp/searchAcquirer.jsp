<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Search Acquirer</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script src="../js/jquery.js"></script>
<script src="../js/jquery.dataTables.js"></script>
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
.lpay_table .dataTables_filter{ display: block !important; }
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
						language: {
							search: "",
							searchPlaceholder: "Search..."
						},
						dom : 'BTftlpi',
						buttons : [ {
							extend : 'copyHtml5',
							exportOptions : {
								columns : [':visible :not(:last-child)']
							}
						}, {
							extend : 'csvHtml5',
							title : 'Acquirer List',
							exportOptions : {
								columns : [':visible :not(:last-child)']
							}
						}, {
							extend : 'pdfHtml5',
							title : 'Acquirer List',
							orientation : 'landscape',
							exportOptions : {
								columns : [':visible :not(:last-child)']
							},
							customize: function (doc) {
							    doc.defaultStyle.alignment = 'center';
		     					doc.styles.tableHeader.alignment = 'center';
							  }
						}, {
							extend : 'print',
							title : 'Acquirer List',
							orientation : 'landscape',
							exportOptions : {
								columns : [':visible :not(:last-child)']
							}
						},{
							extend : 'colvis',
							//           collectionLayout: 'fixed two-column',
							columns : [ 0, 1, 2, 3]
						}],
						"ajax" : {
							"url" : "searchAcquirerAction",
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
											"mData" : "acquirerEmailId",
											"sWidth" : '25%',
										},
										{
											"mData" : "acquirerFirstName",
											"sWidth" : '25%'
										},
										{
											"mData" : "acquirerLastName",
											"sWidth" : '25%'
										},
										{
											"mData" : "acquirerBusinessName",
											"sWidth" : '25%'
										},
										
										
									]
					});

	 $(function() {

		var table = $('#searchAgentDataTable').DataTable();
		$('body').on('click','#searchAgentDataTable tbody td', function() {
			var columnVisible = table.cell(this).index().columnVisible;
			var rowIndex = table.cell(this).index().row;
			var row = table.row(rowIndex).data();

			var emailAddress = table.cell(rowIndex, 0).data();
			var firstName = table.cell(rowIndex, 1).data();
			var lastName = table.cell(rowIndex, 2).data();
			var businessName = table.cell(rowIndex, 3).data();
			var accountNo = table.cell(rowIndex, 4).data();
					

			if (columnVisible == 4) {
				document.getElementById('emailAddress').value = decodeVal(emailAddress);
				document.getElementById('firstName').value = firstName;
				document.getElementById('lastName').value = lastName;
				document.getElementById('businessName').value = businessName;
				document.getElementById('accountNo').value = accountNo;
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

	<section class="search-acquirer-table lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Acquirer List</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table width="100%" id="searchAgentDataTable" class="display">
						<thead class="lpay_table_head">
							<tr>
								<th>Email</th>
								<th>First Name</th>
								<th>Last Name</th>
								<th>Business Name</th>
							</tr>
						</thead>
					</table>
					<!-- /#searchAgentDataTable.display -->
				</div>
				<!-- /.lpay_table -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<!-- <table width="100%" align="left" cellpadding="0" cellspacing="0"
		class="txnf">
		<tr>
			<td align="left"><s:actionmessage /></td>
		</tr>
		<tr>
			<td align="left"><h2>Acquirer List</h2></td>
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
						
					</table>
				</div>
			</td>
		</tr>
	</table> -->
	<s:form name="agentDetails" action="editAcquirerAction">
		<s:hidden name="emailId" id="emailAddress" value="" />
		<s:hidden name="firstName" id="firstName" value="" />
		<s:hidden name="lastName" id="lastName" value="" />
		<s:hidden name="businessName" id="businessName" value="" />
        <s:hidden name="accountNo" id="accountNo" value="" />
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>
</body>
</html>