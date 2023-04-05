<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Merchant Underwriting</title>
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<style>
    .edit-permission{
        z-index: -1;
        position: absolute;
    }
    td.my_class{
        cursor: pointer;
        text-decoration: underline;
        color: #2591c7;
        font-weight: 400;
    }
    .lpay_table .dataTables_filter{
		display: block !important;
	}
</style>
</head>
<div class="edit-permission"><s:property value="%{editingpermission}"/></div>
<!-- /.edit-permission -->
<body class="bodyColor">

    <div class="merchantAssign lpay_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Merchant Underwriter Filter</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-3 txtnew col-sm-4 col-xs-6">
                <div class="form-group lpay_select_group">
                    <label for="merchant">Business Type:</label> <br />
                    <s:select headerKey="ALL" data-live-search="true" headerValue="ALL" name="industryTypes"  id="industryTypes"
                    class="form-control selectpicker"  list="industryTypes" value="ALL"/>
                </div>
                <!-- /.form-group lpay_select_group -->
            </div>
            <div class="col-md-3 txtnew col-sm-4 col-xs-6">
                <div class="form-group lpay_select_group">
                    <label for="merchant">Status:</label> <br />
                    <select name="merchantStatus" id="merchantStatus" class="form-control selectpicker">
                        <option value="All">ALL</option>
                        <option value="Approved">Approved</option>
                        <option value="Rejected">Rejected</option>
                    </select>
                </div>
                <!-- /.form-group lpay_select_group -->
            </div>
            <div class="col-md-3 txtnew col-sm-4 col-xs-6 ">
                <div class="form-group lpay_select_group d-none approver">
                    <label for="merchant">By Whom:</label> <br />
                    <select name="byWhom" id="byWhom" class="form-control selectpicker">
                        <option value="ALL">ALL</option>
                        <option value="admin">Admin</option>
                        <option value="maker">Maker</option>
                        <option value="checker">Checker</option>
                    </select>
                </div>
                <!-- /.form-group lpay_select_group -->
            </div>
        </div>
        <!-- /.row -->
    </div>
    <!-- /.merchantAssign -->
    <div class="merchantAssign lpay_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Merchant Underwriter Lists</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12">
                <div class="lpay_table">
                    <table id="datatable" class="display" cellspacing="0" width="100%">
                        <thead class="lpay_table_head">
                            <tr>
                                <th>Pay Id</th>
                                <th>Business Name</th>
                                <th>Email</th>
                                <th>Status</th>
                                <th>User Type</th>
                                <th>Mobile</th>
                                <th>Reg. Date</th>
                                <th>Maker</th>
                                <th>M. Status</th>
                                <th>M. Status Date</th>
                                <th>Checker</th>
                                <th>C. Status</th>
                                <th>C. Status Date</th>
                            </tr>
                        </thead>
                    </table>
                </div>
                <!-- /.lpay_table -->
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.merchantAssign-table -->
        </div>
    <s:form name="merchant" action="fetchMPADataByPayIdAction">
		<s:hidden name="payId" id="hidden" value="" />	
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>
    <!-- /.merchantAssign -->
    <s:form name="merchantEdit" action="mpaMerchantSetup" id="merchantEditForm">
		<s:hidden name="payId" id="merchantPayIdEdit" value="" />	
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>
    
    <s:hidden name="token" value="%{#session.customToken}" />
    <script src="../js/under-writer.js"></script>
</body>
</html>