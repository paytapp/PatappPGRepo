<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Historical Data</title>
	<script src="../js/jquery.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <link rel="stylesheet" href="../css/jquery-ui.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <!-- <script src="../js/common-scripts.js"></script> -->
    <!-- <script src="../js/user-script.js" type="text/javascript"></script> -->
</head>
<body>
	<section class="single-account lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o"
                        aria-hidden="true"></i></span>
                    <h2 class="heading_text">Historic Data Filter</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- col-md-12 -->
            <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Select Merchant</label>
				   		<s:select
							name="merchantPayId"
							data-download="merchantPayId"
							data-var="merchantPayId"
							class="selectpicker"
							id="merchant"
							data-submerchant="subMerchant"
							headerKey="ALL"
							data-live-search="true"
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
				<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
					<div class="col-md-3 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
							<s:select
								name="merchantPayId"
								data-download="merchantPayId"
								class="selectpicker"
								id="merchant"
								headerKey="ALL"
								data-var="merchantPayId"
								data-submerchant="subMerchant"
								data-live-search="true"
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
								name="merchantPayId"
								data-download="merchantPayId"
								data-var="merchantPayId"
								data-live-search="true"
								class="selectpicker"
								id="merchant"
								list="merchantList"
								data-submerchant="subMerchant"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
					</div>
				</s:else>
			</s:else>
			<s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
				<div class="col-md-3 mb-20" data-target="subMerchant">
					<div class="lpay_select_group">
						<label for="">Sub Merchant</label>
						<s:select
					   		data-id="subMerchant"
							data-download="subMerchantPayId"
							data-var="subMerchantPayId"
							data-submerchant="subMerchant"
							name="subMerchantPayId"
							class="selectpicker"
							id="subMerchant"
							list="subMerchantList"
							listKey="payId"
							listValue="businessName"
							autocomplete="off"
							headerKey="ALL"
							headerValue="ALL" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchantPayId" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
            <!-- /.col-md-3 -->	
			<s:if test="%{#session.USER.UserType.name()=='ADMIN'}">
				<div class="col-md-3 mb-20" data-id="reportTypeName">
					<div class="lpay_select_group">
						<label for="">Report Type</label>
						<select class="selectpicker" data-download="reportTypeName" data-var="reportTypeName" name="reportTypeName" id="reportTypeName">
							<option value="ALL">ALL</option>
							<option value="SearchTransaction">Search Transaction</option>
							<option value="Sale">Sale Captured</option>
							<option value="Refund">Refund Captured</option>
							<option value="Settled">Settled Transaction</option>
							<option value="SummaryReport">Summary Report</option>
							<option value="Booking">Booking Report</option>
							<option value="CustomCapture">Custom Capture Report</option>
							<option value="MIS">MIS Report</option>
							<option value="eCollection">E-Collection</option>
							<option value="BankException">Bank Exception</option>
						</select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
			</s:if>
			<s:elseif test="%{#session.USER.UserType.name()=='SUBADMIN'}">
				<div class="col-md-3 mb-20" data-id="reportTypeName">
					<div class="lpay_select_group">
						<label for="">Report Type</label>
						<select class="selectpicker" data-download="reportTypeName" data-var="reportTypeName" name="reportTypeName" id="reportTypeName">
							<option value="ALL">ALL</option>
							<s:if test="%{#session['USER_PERMISSION'].contains('Download Transaction')}">
								<option value="SearchTransaction">Search Transaction</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Sale Capture')}">
								<option value="Sale">Sale Captured</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Refund Capture')}">
								<option value="Refund">Refund Captured</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Settled')}">
								<option value="Settled">Settled Transaction</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Custom Capture Report')}">
								<option value="CustomCapture">Custom Capture Report</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Booking Report')}">
								<option value="Booking">Booking Report</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Summary Report')}">
								<option value="SummaryReport">Summary Report</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('MIS Report')}">
								<option value="MIS">MIS Report</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('eCollection Report')}">
								<option value="eCollection">eCollection</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Bank Exception')}">
								<option value="BankException">Bank Exception</option>
							</s:if>
						</select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
			</s:elseif>
			<s:elseif test="%{#session.USER.UserType.name()=='MERCHANT'}">
				<div class="col-md-3 mb-20" data-id="reportTypeName">
					<div class="lpay_select_group">
						<label for="">Report Type</label>
						<select class="selectpicker" data-download="reportTypeName" data-var="reportTypeName" name="reportTypeName" id="reportTypeName">
							<option value="ALL">ALL</option>
							<option value="SearchTransaction">Search Transaction</option>
							<option value="Sale">Sale Captured</option>
							<option value="Refund">Refund Captured</option>
							<option value="Settled">Settled Transaction</option>
							<option value="SummaryReport">Summary Report</option>
							<option value="Booking">Booking Report</option>
							<s:if test="%{#session['USER'].capturedMerchantFlag == true}">
								<option value="CustomCapture">Custom Capture Report</option>
							</s:if>
							<s:if test="%{#session['USER'].virtualAccountFlag == true}">
								<option value="eCollection">eCollection</option>
							</s:if>
						</select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
			</s:elseif>
			<s:elseif test="%{#session.USER.UserType.name()=='RESELLER'}">
				<div class="col-md-3 mb-20" data-id="reportTypeName">
					<div class="lpay_select_group">
						<label for="">Report Type</label>
						<select class="selectpicker" data-download="reportTypeName" data-var="reportTypeName" name="reportTypeName" id="reportTypeName">
							<option value="ALL">ALL</option>
							<s:if test="%{#session['USER_PERMISSION'].contains('Quick Search')}">
								<option value="SearchTransaction">Search Transaction</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Sale Capture')}">
								<option value="Sale">Sale Captured</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Refund Capture')}">
								<option value="Refund">Refund Captured</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Settled')}">
								<option value="Settled">Settled Transaction</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Custom Capture Report')}">
								<option value="CustomCapture">Custom Capture Report</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Booking Report')}">
								<option value="Booking">Booking Report</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Summary Report')}">
								<option value="SummaryReport">Summary Report</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('MIS Report')}">
								<option value="MIS">MIS Report</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('eCollection Transaction')}">
								<option value="eCollection">eCollection</option>
							</s:if>
							<s:if test="%{#session['USER_PERMISSION'].contains('Bank Exception')}">
								<option value="BankException">Bank Exception</option>
							</s:if>
						</select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
			</s:elseif>
            <div class="col-md-3 mb-20">
				<div class="lpay_input_group">
                    <label for="">Created Date</label>
                    <s:textfield type="text" data-download="createDate" data-var="createDate" id="createDate" name="createDate"
                    class="lpay_input datepick" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			  <!-- /.col-md-3 mb-20 -->
			<div class="col-md-12 mb-20 text-center">
				<button class="lpay_button lpay_button-md lpay_button-primary" id="submit_data">
					Submit
				</button>
				<!-- /.lpay_button lpay_button-md lpay_button-primary -->
			</div>
			<!-- /.col-md-12 -->					
            <div class="col-md-12 lpay_table_style-2">
                <div class="lpay_table">
                    <table id="historical_table" class="display" cellspacing="0" width="100%">
                        <thead>
                            <tr class="lpay_table_head">
                                <th>Date From</th>
                                <th>Date To</th>
								<th>Create Date</th>
								<th>Report Type</th>
                                <th>File Name</th>
                                <th>Action</th>
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

	<form method="POST" action="downlodGeneratedReportFileAction" id="downlodGeneratedReportFileAction">
		<input type="hidden" name="dateFrom" id="dateFrom-download">
		<input type="hidden" name="dateTo" id="dateTo-download">
		<input type="hidden" name="createDate" id="createDate-download">
		<input type="hidden" name="reportTypeName" id="reportTypeName-download">
		<input type="hidden" name="reportFileName" id="reportFileName-download">
	</form>

    <form action="downloadCustomerQRReport" id="downloadComposite"></form>
	<script src="../js/historical-scripts.js"></script>
</body>
</html>