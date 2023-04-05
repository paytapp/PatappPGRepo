<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>View Surcharge Report</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link rel="stylesheet" href="../css/common-style.css">
	<script type="text/javascript" src="../js/jquery.min.js"></script>
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
</head>
<body>

	<form action="" method="post" style="display: none;" id="downloadFileForm">
		<input type="text" value="" name="payId" id="payId">		
	</form>

	<section class="view-surcharge lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">View Smart Router</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4">
				<div class="lpay_select_group">
				   <label for="">Select Merchant</label>
				   <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
				   		<s:select headerValue="Select Merchant" headerKey=""
					   name="emailId" class="selectpicker" id="merchants"
					   list="listMerchant" listKey="payId" data-live-search="true"
					   listValue="businessName" autocomplete="off" />
					</s:if>
					<s:else>
						<s:select headerValue="Select Merchant" headerKey=""
						name="emailId" class="selectpicker" id="merchants"
						list="listMerchant" listKey="payId" data-live-search="true"
						listValue="businessName" autocomplete="off" />
					</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3">
				<button class="lpay_button lpay_button-md lpay_button-secondary lpay_button-with-input" id="submitBtn">Submit</button>
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-12">
				<div class="download-btn mt-30 d-none">
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="csv-download">XLSX</button>
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="pdf-download">PDF</button>
				</div>
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div id="tableData" class="d-none">
					
				</div>
				<!-- /#tableData -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<span class="empty-data d-none mt-30">
					No Rules Found
				</span> <!-- /.noData -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<script type="text/javascript" src="../js/view-surcharge.js"></script>
</body>
</html>