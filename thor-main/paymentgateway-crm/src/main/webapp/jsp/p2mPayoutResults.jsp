<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>P2M Payout Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/common-scripts.js"></script>
<script src="../js/user-script.js"></script>
<!-- <script src="../js/tabs.js"></script> -->
<!-- <script src="../js/saleCapture.js"></script> -->


</head>
<body id="mainBody">
	<section class="sale-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">P2M Payout Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			
			<!-- /.col-md-3 mb-20 -->
            <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                        <label for="">Select Merchant</label>
                        <s:select
							name="payId"
							data-download="payId"
                        	class="selectpicker"
							id="merchants"
							data-var="payId"
							data-live-search="true"
							headerKey="ALL"
							headerValue="ALL"
							list="merchantList"
							listKey="payId"
							listValue="businessName"
							autocomplete="off"
							/>
                    </div>
                </div>
            </s:if>
            <s:else>
                <div class="col-md-3 mb-20 d-none">
                    <div class="lpay_select_group ">
                        <label for="">Select Merchant</label>
                        <s:select
                            name="payId"
                            data-download="payId"
                            data-var="payId"
                            data-live-search="true"
                            class="selectpicker"
                            id="merchant"
                            list="merchantList"
                            listKey="payId"
                            listValue="businessName"
                            autocomplete="off"
                        />
                    </div>
                </div>
            </s:else>
			
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
                    <label for="">Order ID</label>
                    <s:textfield id="orderId" data-download="orderId" data-var="orderId" class="lpay_input" name="orderId"
                    type="text" value="" autocomplete="off" ></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-3 mb-20 -->
            <div class="col-md-3 mb-20">
				<div class="lpay_input_group">
                    <label for="">RRN Number</label>
                    <s:textfield id="rrn" data-download="rrn" data-var="rrn" class="lpay_input" name="rrn"
                    type="text" value="" autocomplete="off"></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-3 mb-20 -->
			
            <div class="col-md-3 mb-20">
				<div class="lpay_input_group">
				  <label for="">Date From</label>
				  <s:textfield type="text" data-download="dateFrom" data-var="dateFrom" id="dateFrom" name="dateFrom"
				  class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
                    <label for="">Date To</label>
                    <s:textfield type="text" data-download="dateTo" data-var="dateTo" id="dateTo" name="dateTo"
                    class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-12 text-center">
				<input type="button" id="submit" value="View" class="lpay_button lpay_button-md lpay_button-secondary" />
				<input type="button" id="downloadButton" value="Download" class="lpay_button lpay_button-md lpay_button-primary" />
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="sale-transaction lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">P2m Payout Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="txnResultDataTable" class="display" cellspacing="0" width="100%">
						<thead class="lpay_table_head">
							<tr>
								<th>Merchant Name</th>
								<th>Date</th>
								<th>RRN</th>
								<th>Order Id</th>
								<th>Payer VPA</th>
								<th>Payee VPA</th>
								<th>Amount</th>
                                <th>Status</th>
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


	<s:form id="downloadp2MPayoutReportAction" name="downloadp2MPayoutReportAction" action="downloadp2MPayoutReportAction">

	</s:form>
	<script src="../js/p2mScript.js"></script>
	<script>
		function removeSpaces(string) {
			return string.split(' ').join('');
		}

		var allowAlphaNumericSpecial = function(that) {
			that.value = that.value.replace(/[^a-zA-Z0-9@\-\/_+ =*.:;?]/g, '');
		}
	</script>
</body>
</html>