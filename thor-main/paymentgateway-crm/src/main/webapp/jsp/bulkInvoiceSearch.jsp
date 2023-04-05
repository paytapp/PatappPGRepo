<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Bulk Invoice Search</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/bootstrap-flex.css">

	<script src="../js/jquery.min.js" type="text/javascript"></script>
	<script src="../js/moment.js" type="text/javascript"></script>
	<script src="../js/daterangepicker.js" type="text/javascript"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/commanValidate.js"></script>
	<script src="../js/jquery.popupoverlay.js"></script>
	<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
	<script src="../js/pdfmake.js" type="text/javascript"></script>

	<script src="../js/bootstrap-select.min.js"></script>
	<!--  loader scripts -->
</head>

<body>
	<form action="bulkInvoiceFileDownload" method="post" style="display: none;" id="downloadFileForm">
		<input type="text" value="" name="filename" id="filename">		
	</form>
	<input type="hidden" id="isSuperMerchant">
	<section class="bulk-invoice-search lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Bulk Invoice Search</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date From</label>
				<s:textfield 
				type="text"
				id="dateFrom"
				name="dateFrom"
				class="lpay_input datepicker"
				autocomplete="off"
				readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-4 mb-20">
			  <div class="lpay_input_group">
				<label for="">Date To</label>
				<s:textfield 
				type="text"
				id="dateTo"
				name="dateTo"
				class="lpay_input datepicker"
				autocomplete="off"
				readonly="true" />
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
				   <label for="">Select Merchant</label>
				   <s:textfield type="hidden" id="userType" value="%{#session.USER.UserType.name()}"></s:textfield>
					   <s:select name="merchant"
						   id="merchants"
						   class="selectpicker"
						   headerKey="ALL"
						   headerValue="ALL"
						   data-live-search="true"
						   list="merchantList"
						   listKey="payId"
						   listValue="businessName"
						   autocomplete="off"
						   data-token="businessName" />
						   </div>
						   </div>
				   </s:if>
				   <s:if test="%{#session.USER.UserType.name()=='MERCHANT' || #session.USER.UserType.name()=='SUBUSER'}">
					<div class="col-md-4 mb-20 d-none">
						<div class="lpay_select_group">
						   <label for="">Select Merchant</label>
					   <s:select name="merchant"
						   id="merchants"
						   class="selectpicker"
						   data-live-search="true"
						   list="merchantList"
						   listKey="payId"
						   listValue="businessName"
						   autocomplete="off" />
						</div>
						<!-- /.lpay_select_group -->  
						
					</div>
				   </s:if>
				<s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
				<div class="col-md-4 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchant" name="subMerchantId" class="selectpicker textFL_merch" id="subMerchant"
							list="subMerchantList" listKey="payId"  headerKey="ALL"	headerValue="ALL"
							listValue="businessName" onchange="handleChange();" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-4 mb-20 d-none" data-id="submerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchantId" id="subMerchant" class="textFL_merch"></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
			<!-- /.col-md-3 -->
			<div class="col-md-12 text-center">
				<input type="submit" onclick="reloadTable();" id="searchInvoiceBtn" class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<section class="bulk-invoice-search lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Bulk Invoice Search Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="bulk-invoice-search" class="lpay_custom_table" style="width: 100%;">
						<thead class="lpay_table_head">
							<tr>
								<th style='text-align: center'>File Name</th>
								<th style='text-align: center'>Merchant</th>
								<th style='text-align: center'>Sub Merchant</th>
								<th style='text-align: center'>Date</th>
								<th style='text-align: center'>Total Records</th>
								<th style='text-align: center'>Total Success</th>
								<th style='text-align: center'>Total Unsent</th>
								<th style='text-align: center'>Total Pending</th>
								<th style='text-align: center'>Download</th>
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
	<script src="../js/bulkInvoiceSearch.js"></script>
</body>
</html>