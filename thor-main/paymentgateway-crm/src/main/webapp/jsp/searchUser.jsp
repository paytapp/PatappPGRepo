<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>User List</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script src="../js/jquery.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>  
<script type="text/javascript" src="../js/pdfmake.js"></script>
<style>
	.lpay_table .dataTables_filter{
		display: block !important;
	}
.displayNone {
	display: none;
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
						$('#searchUserDataTable').empty();
						

						populateDataTable();

					});
		});	
					
function populateDataTable() {		
	var token  = document.getElementsByName("token")[0].value;
	$('#searchUserDataTable')
			.DataTable(
					{
						language: {
							search: "",
							searchPlaceholder: "Search"
						},
						dom : 'BTftlpi',
						buttons : [ {
							extend : 'copyHtml5',
							exportOptions : {
								columns : [ ':visible' ]
							}
						}, {
							extend : 'csvHtml5',
							title : 'Search User',
							exportOptions : {
								columns : [ ':visible' ]
							}
						}, {
							extend : 'pdfHtml5',
							title : 'Search User',
							exportOptions : {
								columns : [ ':visible' ]
							}
						}, {
							extend : 'print',
							title : 'Search User',
							exportOptions : {
								columns : [ 0, 1, 2, 3, 4, 5, 6]
							}
						},{
							extend : 'colvis',
							//           collectionLayout: 'fixed two-column',
							columns : [ 1, 2, 3, 4, 5, 6]
						}],
						"ajax" : {
							"url" : "searchUserAction",
							"type" : "POST",
							"data" : {
								/* emailId : document
									.getElementById("emailId").value,
								phoneNo : document
									.getElementById("phoneNo").value */
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
											"mData" : "payId",
											"sWidth" : '25%',
										},								
										{
											"mData" : "emailId",
											"sWidth" : '25%',
										},
										{
											"mData" : "firstName",
											"sWidth" : '20%'
										},
										{
											"mData" : "lastName",
											"sWidth" : '20%'
										},
										{
											"mData" : "mobile",
											"sWidth" : '20%'
										},
										{
											"mData" : "isActive",
											"sWidth" : '10%'
										},
										{
											"mData" : "subUserType",
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

		var table = $('#searchUserDataTable').DataTable();
		$('#searchUserDataTable tbody')
				.on(
						'click',
						'td',
						function() {

							var columnVisible = table.cell(this).index().columnVisible;
							var rowIndex = table.cell(this).index().row;
							var row = table.row(rowIndex).data();
							
							var payId = table.cell(rowIndex, 0).data();
							var emailAddress = table.cell(rowIndex, 1).data();
							var firstName = table.cell(rowIndex, 2).data();
							var lastName = table.cell(rowIndex, 3).data();
							var mobile = table.cell(rowIndex, 4).data();
							var isActive = table.cell(rowIndex, 5).data();					

							if (columnVisible == 7) {
								document.getElementById('emailAddress').value = decodeVal(emailAddress);
								document.getElementById('firstName').value = firstName;
								document.getElementById('lastName').value = lastName;
								document.getElementById('mobile').value = mobile;
								document.getElementById('isActive').value = isActive;
								document.userDetails.submit();
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

	<section class="search-user lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">User List</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
				<table width="100%" border="0" cellpadding="0" cellspacing="0" id="searchUserDataTable"
					class="display">
					<thead class="lpay_table_head">
						<tr>	
							<th>PayId</th>		
							<th>Email</th>
							<th>First Name</th>
							<th>Last Name</th>
							<th>Phone</th>
							<th>Is Active</th>
							<th>Sub User Type</th>
							<th>Action</th>
							<th></th>				
						</tr>
					</thead>
				</table>
				</div>
				<!-- /.lpay_table -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<s:form name="userDetails" action="editUserCallAction">	
		<s:hidden name="emailId" id="emailAddress" value="" />
		<s:hidden name="firstName" id="firstName" value="" />
		<s:hidden name="lastName" id="lastName" value="" />
		<s:hidden name="mobile" id="mobile" value="" />
		<s:hidden name="isActive" id="isActive" value="" />
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>	
	</s:form>
</body>
</html>