<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Debit Report</title>
    <link rel="stylesheet" href="../css/jquery-ui.css">
    <script src="../js/jquery-latest.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <script src="../js/daterangepicker.js" type="text/javascript"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <script src="../js/bootstrap-select.min.js"></script>
    <script src="../js/common-scripts.js"></script>
    <script src="../js/user-script.js"></script>

    <style>
        .response-popup {
            position: fixed;
            top: 0px;
            right: 0;
            left: 0;
            bottom: 0px;
            background-color: rgba(0, 0, 0, 0.8);
            z-index: 99;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .lpay_table{
            white-space: nowrap;
        }

        .response-popup--inner {
            width: 400px;
            background-color: #fff;
            padding: 30px;
            display: flex;
            align-items: center;
            flex-direction: column;
        }

        .response-popup--inner .response-msg {
            margin-top: 20px;
            margin-bottom: 20px;
            font-size: 12px;
            text-align: center;
        }

        .response-btn a {
            background: #23527c;
            padding: 8px 18px;
            color: #fff;
            text-decoration: none;
            font-size: 14px;
            border-radius: 5px;
        }
        .debit_popup_inner{
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
        }
        .debit_popup_container .debit_popup_box{
            position: static !important;
            max-height: 100% !important;

        }
        .debit_popup_container .button-wrapper{
            top: 15px;
        }
    </style>
    
</head>
<!-- /.edit-permission -->
<body>
    <s:hidden id="userType" value="%{#session.USER.UserType.name()}" />
    <section class="debit-report lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Debit Report</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
				<div class="col-md-3 mb-20 debit-report-input">
					<div class="lpay_select_group">
						<label for="">Select Merchant</label>
				        <s:select name="merchantEmailId" data-download="merchantPayId" data-var="merchantPayId" class="selectpicker"
					   id="merchant" data-submerchant="subMerchant" data-user="subUser" headerKey="ALL" data-live-search="true" headerValue="ALL"
					   list="merchantList" listKey="emailId"
					   listValue="businessName" autocomplete="off" />
					</div>
				</div>
			</s:if>
			<s:else>
				<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
				   <div class="col-md-3 mb-20 debit-report-input">
					    <div class="lpay_select_group">
                            <label for="">Select Merchant</label>
                            <s:select name="merchantEmailId" onchange="removeError(this)" data-download="merchantPayId" class="selectpicker"
							id="merchant" headerKey="" data-var="merchantPayId" data-submerchant="subMerchant" data-user="subUser"  data-live-search="true" headerValue="ALL"
							list="merchantList" listKey="payId"
							listValue="businessName" autocomplete="off" />
					    </div>
				   </div>
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 d-none debit-report-input">
						<div class="lpay_select_group ">
							<label for="">Select Merchant</label>
						    <s:select name="merchantEmailId" data-download="merchantPayId" data-var="merchantEmailId" data-live-search="true" class="selectpicker" id="merchant"
							list="merchantList" data-submerchant="subMerchant" data-user="subUser" listKey="emailId"
							listValue="businessName" autocomplete="off" />
						</div>
					</div>
				</s:else>
			</s:else>
			<s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20 debit-report-input" data-target="subMerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchant" data-download="subMerchantPayId" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser"  name="subMerchantEmailId" class="selectpicker" id="subMerchant" list="subMerchantList" listKey="emailId" headerKey="ALL" headerValue="ALL"
							listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none debit-report-input" data-target="subMerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchantEmailId" data-download="subMerchantPayId" headerValue="ALL" data-var="subMerchantPayId" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
			<s:if test="%{#session.SUBUSERFLAG == true}">
				<div class="col-md-3 mb-20 debit-report-input" data-target="subUser">
					<div class="lpay_select_group">
					   <label for="">Sub User</label>
					   <s:select data-id="subUser" data-download="subUserPayId" headerValue="ALL" data-var="subUserPayId" name="subUserPayId" class="selectpicker" id="subUser" list="subUserList" listKey="emailId" listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 debit-report-input d-none" data-target="subUser"> 
					<div class="lpay_select_group">
					   <label for="">Sub User</label>
					   <select name="subUserPayId" data-download="subUserPayId" data-var="subUserPayId" id="subUser" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
			</s:else>
            <!-- /.col-md-3 -->	
           <!--  <div class="col-md-3 mb-20 debit-report-input">
                <div class="lpay_select_group">
                    <label for="">Registration Status</label>
                    <select name="registrationStatus" class="selectpicker" data-var="status" data-download="registrationStatus">
                        <option value="All">ALL</option>
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="DEACTIVATED">DEACTIVATED</option>
                    </select>
                </div> -->
                <!-- /.lpay_select_group -->  
            <!-- </div> -->
            <!-- /.col-md-3 -->	
            <!-- <div class="col-md-3 mb-20 debit-report-input">
                <div class="lpay_select_group">
                    <label for="">Transaction Status</label>
                    <select name="transactionStatus" class="selectpicker" data-var="transactionStatus" data-download="transactionStatus">
                        <option value="paid">Paid</option>
                        <option value="futureInvoice">Future Invoice</option>
                        <option value="defaulted">Defaulted</option>
                    </select>
                </div> -->
                <!-- /.lpay_select_group -->  
            <!-- </div> -->
            <!-- /.col-md-3 -->	
            <div class="col-md-3 mb-20 debit-report-input">
				<div class="lpay_input_group">
                    <label for="">Date From</label>
                    <s:textfield type="text" data-required="true" data-download="dateFrom" data-var="dateFrom" id="dateFrom" name="dateFrom"
                    class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20 debit-report-input">
				<div class="lpay_input_group">
                    <label for="">Date To</label>
                    <s:textfield type="text" data-download="dateTo" data-var="dateTo" id="dateTo" name="dateTo"
                    class="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->	
            <div class="col-md-3 mb-20 debit-report-input">
				<div class="lpay_input_group">
				  <label for="">UMRN Number</label>
                    <s:textfield id="umrn" data-var="umrnNumber" class="lpay_input" name="umrn"
                    type="text" value="" autocomplete="off" ></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
            <div class="col-md-3 mb-20 debit-report-input">
				<div class="lpay_input_group">
				  <label for="">Order ID</label>
                    <s:textfield id="orderId" data-reg="^[0-9]{6}$" data-var="orderId" class="lpay_input" name="orderId"
                    type="text" value="" oninput="removeError(this)" autocomplete="off" ></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
            <div class="col-md-12 text-center mb-10">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="view">View</button>
                <button class="lpay_button lpay_button-md lpay_button-primary" id="downlaod-button" onclick="createDownloadForm(this)">Download</button>
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 lpay_table_style-2">
                <div class="lpay_table">
                    <table id="datatable" class="display" cellspacing="0" width="100%">
                        <thead>
                            <tr class="lpay_table_head">
                                <th>Merchant Name</th>
                                <th>Sub Merchant Name</th>
                                <th>Order ID</th>
                                <th>Pg Ref Number</th>
                                <th>UMRN Number</th>
                                <th>Registration Date</th>
                                <th>Total Amount</th>
                                <th>Download</th>
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
    <div class="debit_popup_container d-none">
        <div class="debit_popup_inner">
            <div class="debit_popup_box">
                <div class="default-heading">
                    <h3>eNACH Transaction Data</h3>
                </div>
                <!-- /.default-heading -->
                <div class="col-md-12">
                    <div class="lpay_table" id="lpay_table_popup">
                        <table id="datatablePopup" class="display" cellspacing="0" width="100%">
                            <thead>
                                <tr class="lpay_table_head">
                                    <th data-id="merchantName">Merchant Name</th>
                                    <th>Sub Merchant Name</th>
                                    <th data-id="orderId">Order ID</th>
                                    <th data-id="pgRefNum">Pg Ref Num</th>
                                    <th data-id="pgRefNum">Pg Ref Num (Registration)</th>
                                    <th data-id="pgRefNum">UMRN Number</th>
                                    <th data-id="registrationDate">Registration Date</th>
                                    <th data-id="dueDate">Due Date</th>
                                    <th data-id="registrationDate">Customer Name</th>
                                    <th data-id="registrationDate">Email</th>
                                    <th data-id="registrationDate">Mobile</th>
                                    <th data-id="registrationDate">Status</th>
                                    <th data-id="totalAmount">Total Amount</th>
                                    <th data-id="action">Action</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.debit_popup_box -->
        </div>
        <!-- /.debit_popup_inner -->
        <div class="button-wrapper d-none">
            <button class="lpay_button lpay_button-md lpay_button-secondary close-btn">Close</button>
            <!-- /.lpay_button lpay_button-md lpay_button-primary -->
        </div>
        <!-- /.button-wrapper -->
    </div>
    <!-- /.debit_popup_container -->

    <div class="response-popup d-none">
        <div class="response-popup--inner">
            <div class="response-icon">
                <img src="" alt="">
            </div>
            <div class="response-msg"></div>
            <div class="response-btn">
                <a href="#" id="btn-close-response">OK</a>
            </div>
        </div>
    </div>
    <!-- /.response-popup -->

    <form action="NA" method="post" id="downloadForm"> 

    </form>

    <script src="../js/debit-report.js"></script>
</body>
</html>