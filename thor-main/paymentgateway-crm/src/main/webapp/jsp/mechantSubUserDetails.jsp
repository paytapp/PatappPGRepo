<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html dir="ltr" lang="en-US">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Merchant Subusers</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<link href="../fonts//css/font-awesome.min.css" rel="stylesheet">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>
</head>
<body>
	<section class="merchant-subuser lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Merchant Sub-user Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3">
				<div class="lpay_select_group">
				   <label for="">Merchant List</label>
				   <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
					<s:select name="merchants" data-live-search="true" class="form-control selectpicker merchant-div" id="merchant" headerKey="ALL" headerValue="ALL" listKey="emailId" listValue="businessName" list="merchantList" autocomplete="off" onchange="handleChange();" />
				   </s:if>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="merchant-subuser lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Merchant Sub-user Lists</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="datatable" class="display" cellspacing="0" width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th>Pay Id</th>
								<th>Email</th>
								<th>Business Name </th>
								<th>Status</th>
								<th>UserType</th>
								<th>Mobile</th>
								<th>Registration Date</th>
								<th>Pay Id</th>
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
	<script src="../js/merchant-subuser.js"></script>
 </body>
</html>