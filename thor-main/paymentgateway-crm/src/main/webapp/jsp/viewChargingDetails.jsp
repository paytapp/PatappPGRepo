<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>View Charging Details</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script type="text/javascript" src="../js/jquery.min.js"></script>
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
	<style>
		.lpay_table_wrapper .inner-heading{
			display: block;
		}
	</style>
</head>
<body>
	<section class="view-charging-detail lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">View Charging Details</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3">
				<div class="lpay_select_group">
					<label for="">Select Merchant</label>
					<s:if
					test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
					<s:select headerValue="Select Merchant" headerKey="" name="payId" class="selectpicker" id="merchants"
						list="listMerchant" listKey="payId" data-live-search="true"
						listValue="businessName" autocomplete="off" />
					</s:if>
					<s:else>
							<s:select headerValue="Select Merchant" headerKey=""
							name="payId" class="selectpicker" id="merchants"
							list="listMerchant" listKey="payId" data-live-search="true"
							listValue="businessName" autocomplete="off" />
					</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3">
				<button class="lpay_button lpay_button-md lpay_button-secondary lpay_button-with-input" id="submitBtn" >Submit</button>
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12">
				<div class="download-btn mt-30 d-none">
					<button class="lpay_button lpay_button-md lpay_button-secondary" id="csv-download">CSV</button>
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
				<span class="empty-data mt-20 d-none">
					No Charging Details Found
				</span> <!-- /.noData -->
			</div>
			<!-- /.col-md-12 -->
		</div>
			<!-- /.row -->
	</section>
		<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
		<form data-id="downlaodFile" action="downloadExcelAction">
			<input type="hidden" id="payIdCsv" name="payId">
		</form>
	<script type="text/javascript" src="../js/view-charging.js"></script>

</body>
</html>