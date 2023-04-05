<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>Login History</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
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
			$('#loginHistoryDataTable').dataTable({
				dom : 'BTftlpi',
				buttons : [ {
					extend : 'copyHtml5',
					exportOptions : {
						columns : [ ':visible' ]
					}
				}, {
					extend : 'csvHtml5',
					title : 'Login History',
					exportOptions : {
						columns : [ ':visible' ]
					}
				}, {
					extend : 'pdfHtml5',
					orientation: 'landscape',
					title : 'Login History',
					exportOptions : {
						columns : [ ':visible' ]
					},
					customize: function (doc) {
					    doc.content[1].table.widths = Array(doc.content[1].table.body[0].length + 1).join('*').split('');
					    doc.defaultStyle.alignment = 'center';
     					doc.styles.tableHeader.alignment = 'center';
					  }
				}, {
					extend : 'print',
					title : 'Login History',
					exportOptions : {
						columns : [ ':visible']
					}
				},{
					extend : 'colvis',
					//           collectionLayout: 'fixed two-column',
					columns : [0, 1, 2, 3, 4, 5, 6]
				} ],
				"ajax" : {
					"url" : "loginHistoryAction",
					"type" : "POST",
					data : function(d) {
						return generatePostData(d);
					}
				},
				"bProcessing" : true,
				"bLengthChange" : true,
				"bDestroy" : true,
			        "serverSide": true,
			        "paginationType": "full_numbers", 
			        "lengthMenu": [[10, 25, 50, -1], [10, 25, 50, "All"]],
			        "order" : [ [ 1, "desc" ] ], 
				"columns" : [
				{
					"data" : "businessName",
					
				}, {
					"data" : "ip",
					"width" : '14%'
				}, {
					"data" : "browser",
					"width" : '13%'
				}, {
					"data" : "os",
					"width" : '10%'
				},{
					"data" : "status",
				
				}, {
					"data" : "timeStamp",
				}, {
					"data" : "failureReason"
				}]
			});
		}
		function reloadTable() {
			var tableObj = $('#loginHistoryDataTable');
			var table = tableObj.DataTable();
			table.ajax.reload();
		}
		function generatePostData(a) {
			var token = document.getElementsByName("token")[0].value;
			var merchantEmailId = document.getElementById("merchant").value;
			var obj = {
			    emailId : merchantEmailId,
				draw :   a.draw,
				length : a.length,
				start : a.start,
				token : token,
				"struts.token.name" : "token",
			}
			return obj;
		}
</script>
</head>
<body>

	<s:hidden value="%{#session.USER.UserType}"></s:hidden>

	<section class="login-history lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-9">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Login History</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-8 -->
			<div class="col-md-3">
				<div class="lpay_select_group">
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
						<s:select name="merchants" class="selectpicker showMerchant" id="merchant"
						headerKey="ALL MERCHANTS" headerValue="ALL MERCHANTS" data-live-search="true" listKey="emailId"
						listValue="businessName" list="merchantList" autocomplete="off" onchange="handleChange();" />
					</s:if>
					<s:elseif test="%{#session.USER.UserType.name()=='RESELLER'}">
						<s:select name="merchants" class="selectpicker" id="merchant"
						listKey="emailId" listValue="businessName" data-live-search="true" list="merchantList" autocomplete="off" onchange="handleChange();" />
					</s:elseif>
					<s:if test="%{#session.USER.UserType.name() =='MERCHANT'}">
						<s:select name="merchants" class="selectpicker" id="merchant"
						listKey="emailId" listValue="businessName" data-live-search="true" list="merchantList" autocomplete="off" onchange="handleChange();" />
					</s:if>
					<s:elseif test="%{#session.USER.UserType.name()=='SUBUSER'}">
						<s:select name="merchants" class="selectpicker" id="merchant"
						listKey="emailId" listValue="businessName" data-live-search="true" list="merchantList" autocomplete="off" onchange="handleChange();" />
					</s:elseif>
				</div>
				<!-- /.lpay_select_group --> 
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="loginHistoryDataTable" class="display" cellspacing="0" width="100%">
						<thead class="lpay_table_head">
							<tr>
								<th>Business Name</th>
								<th>IP address</th>
								<th>Browser</th>
								<th>OS</th>
								<th>Status</th>
								<th>Date</th>
								<th>Failed Login Reason</th>
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

</body>
</html>