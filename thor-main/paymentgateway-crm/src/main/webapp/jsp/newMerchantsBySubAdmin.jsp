<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>New Merchants List</title>
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>

<style>
    .lpay_table .dataTables_filter{
		display: block !important;
	}
</style>

</head>
<!-- /.edit-permission -->
<body class="bodyColor">
    <section class="merchant-subadmin lpay_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">New Merchant List</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12">
                <div class="lpay_table">
                    <table width="100%" id="merchantList">
                        <thead class="lpay_table_head">
                            <tr>
                                <th>Pay Id</th>
                                <th>Business Name</th>
                                <th>Status</th>
                                <th>MPA Status</th>
                                <th>Registration Date</th>
                                <th>Created BY</th>
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
    <s:form name="merchant" action="mpaFormFillingBySubAdmin">
		<s:hidden name="payId" id="hidden" value="" />	
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>
    <s:hidden name="token" value="%{#session.customToken}" />
    <script src="../js/merchant-undercrm.js"></script>
</body>
</html>