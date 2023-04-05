<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Fraud Analytics</title>
<link rel="stylesheet" href="../css/jquery-ui.css">
<script src="../js/jquery-latest.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/user-script.js"></script>
<script src="../js/bootstrap-select.min.js"></script>

</head>
<!-- /.edit-permission -->
<body class="bodyColor">
    <s:hidden id="setSuperMerchant"></s:hidden>
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Fraud Analytics</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            
            <s:if
				   test="%{#session.USER.UserType.name()=='RESELLER'}">
				   <div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Select Merchant</label>
				   <s:select name="merchantEmailId" data-var="merchantEmailId" class="selectpicker"
					   id="merchant" data-submerchant="subMerchant" data-user="subUser" headerKey="ALL" data-live-search="true" headerValue="ALL"
					   list="merchantList" listKey="emailId" data-id="reportMerchant" 
					   listValue="businessName" autocomplete="off" />
					   </div>
					   </div>
				</s:if>
				<s:else>
				<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
				   <div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Select Merchant</label>
						<s:select name="merchantEmailId" class="selectpicker"
							id="merchant" headerKey="" data-var="merchantEmailId" data-submerchant="subMerchant" data-user="subUser"  data-live-search="true" headerValue="ALL"
							list="merchantList" listKey="emailId" data-id="reportMerchant" 
							listValue="businessName" autocomplete="off" />
					</div>
				   </div>
				</s:if>
					<s:else>
						<div class="col-md-3 mb-20 d-none">
							<div class="lpay_select_group ">
								<label for="">Select Merchant</label>
						<s:select name="merchantEmailId" data-var="merchantEmailId" data-live-search="true" class="selectpicker" id="merchant"
							list="merchantList" data-submerchant="subMerchant" data-user="subUser" listKey="emailId" data-id="reportMerchant"
							listValue="businessName" autocomplete="off" />
							</div>
							</div>
					</s:else>
				</s:else>
			<s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20" data-target="subMerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchantReport" data-var="subMerchantEmailId" headerKey="ALL" data-submerchant="subMerchant" data-user="subUser"  name="subMerchantEmailId" class="selectpicker" id="subMerchant" list="subMerchantList" listKey="emailId" headerValue="ALL"
							listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
                       <select name="subMerchantEmailId" data-var="subMerchantEmailId" data-id="subMerchantReport"
                        data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
            
            <div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Payment Region</label>
					<s:select
						headerKey=""
						headerValue="ALL"
						class="selectpicker"
						list="#{'DOMESTIC':'Domestic','INTERNATIONAL':'International'}"
						name="paymentRegion"
						id="paymentRegion"
						data-id="reportPaymentRegion"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Country of Origin</label>
				   <s:select headerKey="" headerValue="ALL" class="selectpicker"
				   list="@com.paymentgateway.commons.util.CountryCodes@values()"
                   listValue="name" listKey="code" name="countryCodes"
                   data-live-search="true"
				   id="countryCodes" data-id="reportCountryCodes" autocomplete="off" value="" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
            <!-- /.col-md-3 mb-20 -->	
            
            <div class="col-md-3 mb-20">
				<div class="lpay_select_group">
					<label for="">Status</label>
					<s:select
						class="selectpicker"
						list="lst"
						name="status"
						headerKey=""
						headerValue="ALL"
						id="status"
						value="name"
						listKey="name"
						listValue="name"
						 data-id="reportStatus"
						autocomplete="off"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 mb-20 -->
				
            <div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                    <label for="">Date From</label> 
                    <s:textfield type="text" id="dateFrom" data-id="reportDateFrom" name="dateFrom"
                    class="lpay_input datepick" autocomplete="off" readonly="true" />
                </div>
                <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                    <label for="">Date To</label>
                    <s:textfield type="text" id="dateTo" data-id="reportDateTo" name="dateTo"
                    class="lpay_input datepick" autocomplete="off" readonly="true" />
                </div>
                <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-12 mb-20 text-center">
                <button class="lpay_button lpay_button-md lpay_button-secondary viewData">View</button>
                <button class="lpay_button lpay_button-md lpay_button-primary downloadData">Download</button>
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12">
                <div class="lpay_table">
                    
                    <table id="datatable" class="display" cellspacing="0" width="100%">
                        <thead class="lpay_table_head">
                            <tr>
                                <th>Merchant Name</th>
                                <th>Sub Merchant</th>
                                <th>Order ID</th>
                                <th>Pg Ref Num</th>
                                <th>Date of Transaction</th>
                                <th>Country of Origin</th>
                                <th>Payment Region</th>
                                <th>Payment Type</th>
                                <th>Amount</th>
                                <th>Total Amount</th>
                                <th>Status</th>
                                <th>Payment Gateway Response Msg</th>
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
    <s:hidden name="isSuperMerchant" id="isSuperMerchant"></s:hidden>
    <s:hidden name="token" value="%{#session.customToken}" />
    <s:hidden id="userType" value="%{#session.USER.UserType}"></s:hidden>

    <s:form id="fraudDataDownload"  action="fraudDataDownload" >
        <s:hidden name="reportMerchant" id="reportMerchant"></s:hidden>
    <!-- <s:hidden name="subMerchant" id="subMerchant"></s:hidden> -->
        <s:hidden name="reportPaymentRegion" id="reportPaymentRegion"></s:hidden>
        <s:hidden name="reportCountryCodes" id="reportCountryCodes"></s:hidden>
        <s:hidden name="reportDateFrom" id="reportDateFrom"></s:hidden>
        <s:hidden name="reportDateTo" id="reportDateTo"></s:hidden>
        <s:hidden name="reportStatus" id="reportStatus"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
        <s:hidden name="subMerchant" id="reportSubMerchant" value=""></s:hidden>
    </s:form>
    

    <script type="text/javascript">

        function hideColumn(){
            var _userType = $("#userType").val();
            var _userLogin = $("#setSuperMerchant").val();
            var _isSuperMerchant = $("#isSuperMerchant").val();
            if(_userLogin == "true"){
                _isSuperMerchant = "Y"
            }
            console.log(_isSuperMerchant);
            var _table = $("#datatable").DataTable();
           
            if(_isSuperMerchant == "Y"){
                _table.columns(1).visible(true);
            }else{
                _table.columns(1).visible(false);
            }
            
        }



        $(document).ready(function(e){

            document.querySelector("#merchant").addEventListener("change", function(e){
		getSubMerchant(e, "getSubMerchantList", {
			isSuperMerchant : true,
			subUser : true,
			retailMerchantFlag: true,
			glocal : true
		});
	});

            // function for datatable hide columnd

            $(".downloadData").on("click", function(e){
            	if($("[data-id=reportMerchant]").val() == ""){
                    $("#reportMerchant").val("All");
                }else{
                    $("#reportMerchant").val($("[data-id=reportMerchant]").val());
                }
                $("#reportSubMerchant").val($("[data-id=subMerchantReport]").val());
                //$("#reportPaymentMode").val($("[data-id=reportPaymentMode]").val());
               // $("#subMerchant").val($("[data-id=subMerchant]").val());
               $("#reportPaymentRegion").val($("[data-id=reportPaymentRegion]").val());
               $("#reportCountryCodes").val($("[data-id=reportCountryCodes]").val());
                $("#reportDateFrom").val($("[data-id=reportDateFrom]").val());
                $("#reportDateTo").val($("[data-id=reportDateTo]").val());
                $("#reportStatus").val($("[data-id=reportStatus]").val());
                $("#fraudDataDownload").submit();
            })

            var today = new Date();
			$('.datepick').val($.datepicker.formatDate('dd-mm-yy', today));
            $(".datepick").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date(),
                changeMonth: true,
                changeYear: true
			});

            $(".viewData").on("click", function(e){
                $("body").removeClass("loader--inactive");
                handleChange();
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 1500);
            })
            
            handleChange();

            function handleChange(){
                var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		        var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
                if (transFrom > transTo) {
                    alert('From date must be before the to date');
                    $("body").addClass("loader--inactive");
                    $('#dateFrom').focus();
                    return false;
                }
                if (transTo - transFrom > 31 * 86400000) {
                    alert('No. of days can not be more than 31');
                    $("body").addClass("loader--inactive");
                    $('#dateFrom').focus();
                    return false;
                }

                $("#datatable").dataTable({
                    dom : 'BTftlpi',
                    buttons: ['csv', 'print', 'pdf'],
                    language: {
                        search: "",
                        searchPlaceholder: "Search records"
                    },
                    "destroy": true,
                    "ajax": {
                        "type": "post",
                        "url": "fraudAnalyticsReportData",
                        "data" : function (d){
                                return generatePostData(d);
                            }
                        },
                        "initComplete" : function(settings, json) {
                            $("#setSuperMerchant").val(json.flag);
                            hideColumn();
                        },

                        "destroy": true,
                        "order": [[ 3, 'desc' ]],
                        //"destroy": true,
                        "aoColumns": [
                        	{
                                "mDataProp": "merchants",
                                "className": "my_class"
                            },
                            {"mData" : "subMerchantId"},
                        {
                            "mData": "orderId",
                            
                        },
                        {"mData" : "pgRefNum"},
                        {"mData" : "tDate", "className": "text-center"}, 
                        {"mData" : "country"}, 
                        {"mData" : "paymentRegion"}, 
                        {"mData" : "paymentMethods"}, 
                        {"mData" : "amount"},
                        {"mData" : "totalAmount"},
                        {"mData" : "status"},
                        {"mData" : "pgTxnMessage"}
                                
                    ]
                });

            }

            


           // variable sent to backend function
           function generatePostData(d) {

var countryCodes = document.getElementById("countryCodes").value;
var paymentRegion = document.getElementById("paymentRegion").value;
var payId = $("#merchant").val();

if(paymentRegion == '') {
    paymentRegion = 'ALL';
}
if(countryCodes == '') {
    countryCodes = 'ALL';
}
var obj = {
    payId : payId,
    subMerchantPayId : $("#subMerchant").val(),
    status : $("#status").val(),
    paymentRegion : paymentRegion,
    countryCodes : countryCodes,
    dateFrom : $("#dateFrom").val(),
    dateTo : $("#dateTo").val(),
    draw : d.draw,
    length : d.length,
    start : d.start,
    token : $("[name=token]").val(),
    "struts.token.name" : "token",

   

};
return obj;
}

        })
    </script>
    
</body>
</html>