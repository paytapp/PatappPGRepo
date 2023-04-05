<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html dir="ltr" lang="en-US">
<head>
<title>Reseller Revenue Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/common-scripts.js"></script>
<script type="text/javascript">

$(document).ready(function(e){

    var _userType = $("#userType").val();

    // Reseller ID
    $("#resellerId").on("change", function(e){
        var _this = $(this).val();
        if(_this != ""){
            $("boby").removeClass("loader--inactive");
            $.ajax({
                type: "post",
                url: "getMerchantListByReseller",
                data: {
                    "resellerId": _this
                },
                success: function(data){
                    console.log(data);
                    $("#merchant").html("");
                    if(data.listMerchant.length > 0){
                        var _option = $("#merchant").append("<option value='ALL'>ALL</option>");
                        for(var i = 0; i < data.listMerchant.length; i++){
                            _option += $("#merchant").append("<option value="+data.listMerchant[i]["payId"]+">"+data.listMerchant[i]["businessName"]+"</option>")
                        }
                        $("#merchant").selectpicker("refresh");
                        $("#merchant").selectpicker();
                        $("boby").addClass("loader--inactive");
                    }else{
                        var _option = $("#merchant").append("<option value=''>No merchant exist</option>");
                        $("#merchant").selectpicker("refresh");
                        $("#merchant").selectpicker();
                        $("boby").addClass("loader--inactive");
                    }
                }
            })
        }
    })

    $("#resellerSubmit").on("click", function(e){
        var _token = $("[name=token]").val();
        var resellerId = $("#resellerId").val();
        var _merchant = $("#merchant").val();
        var _dateFrom = $("#dateFrom").val();
        var _dateTo = $("#dateTo").val();
		if(_userType == "RESELLER"){
			resellerId = "ALL";
		}
        var obj = {
            token : _token,
            resellerId : resellerId,
            merchantPayId : _merchant,
            dateFrom : _dateFrom,
            dateTo : _dateTo
        }
        if(_merchant == ""){
            alert("Please select merchant first");
            return false;
        }
        // var transFrom = $.datepicker.parseDate('yy-mm-dd', $('#dateFrom').val());
        // var transTo = $.datepicker.parseDate('yy-mm-dd', $('#dateTo').val());
        if(resellerId == "" || resellerId == null){
            alert("Reseller should not blank");
            return false;
        }
        // if (transFrom > transTo) {
        //     alert('From date must be before the to date');
        //     $("body").addClass("loader--inactive");
        //     $('#dateFrom').focus();
        //     return false;
        // }
        // if (transTo - transFrom > 31 * 86400000) {
        //     alert('No. of days can not be more than 31');
        //     $("body").addClass("loader--inactive");
        //     $('#dateFrom').focus();
        //     return false;
        // }
		$("body").removeClass("loader--inactive");
        $.ajax({
            type: "post",
            url: "resellerRevenueReport",
            data: obj,
            success: function(data){
                var _tableWrapper = $(".lpay_table_tbody");
                $(".lpay_table_tbody").html("");
                if(data.resellerRevenueLength > 0) {
                $(".no-data-table").addClass("d-none");    
                for(key in data.resellerRevenue){
                    var _tr = "<tr>";
                    _tr += "<td>"+key+"</td>";
                    _tr += "<td>"+data.resellerRevenue[key]["TOTAL_TRANSACTION"]+"</td>";
                    _tr += "<td>"+data.resellerRevenue[key]["TOTAL_REVENUE"]+"</td>";
                    _tr += "<td>"+data.resellerRevenue[key]["TOTAL_RESELLER_GST"]+"</td>";
                    if(_userType != "RESELLER"){
                        _tr += "<td>"+data.resellerRevenue[key]["TOTAL_PG_PROFIT"]+"</td>";  
                        _tr += "<td>"+data.resellerRevenue[key]["TOTAL_PG_GST"]+"</td>";
                    }
                    _tr += "</tr>";
                    _tableWrapper.append(_tr);
                }
            } else {
                $(".no-data-table").removeClass("d-none"); 
            }
				$("body").addClass("loader--inactive");
            } 
        })
    });
    
});


</script>
</head>
<body>

    <s:hidden id="userType" value="%{#session.USER.UserType}"></s:hidden>

    <section class="reseller-revenue lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Reseller Revenue Filter</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <s:if test="%{#session.USER.UserType.name()!='RESELLER'}">
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Reseller</label>
                       <s:select name="resellerId" class="selectpicker" title="Select Reseller" headerKey="" headerValue="Select Reseller" data-live-search="true" id="resellerId" list="listReseller"
                        listKey="resellerId" listValue="businessName"  autocomplete="off" />
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 -->
            </s:if>

            <s:if test="%{#session.USER.UserType.name()!='RESELLER'}">
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Merchant</label>
                       <select id="merchant" class="selectpicker">
                           <option value="">Select Merchant</option>
                       </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 mb-20 -->
            </s:if>
            <s:else>
                <div class="col-md-3 mb-20">
                <div class="lpay_select_group" >
                    <label for="">Merchant</label>
                    <s:select
                        headerKey="ALL"
                        headerValue="ALL"
                        name="merchantPayId"
                        class="selectpicker"
                        id="merchant"
                        list="listMerchant"
                        data-live-search="true"
                        listKey="payId"
                        listValue="businessName"
                        autocomplete="off"
                    />
                </div>
            </div>
            </s:else>
            
            <div class="col-md-3 mb-20">
              <div class="lpay_input_group">
                <label for="">Date From</label>
                <input type="text" id="dateFrom" class="lpay_input">
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                  <label for="">Date To</label>
                  <input type="text" id="dateTo" class="lpay_input">
                </div>
                <!-- /.lpay_input_group -->
              </div>
              <!-- /.col-md-4 -->
              <div class="col-md-12 text-center">
                  <button id="resellerSubmit" class="lpay_button lpay_button-md lpay_button-secondary">Submit</button>
              </div>
              <!-- /.col-md-12 text-center -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    <section class="reseller-revenue lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Reseller Revenue Report</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12">
                <div class="lpay_table_wrapper">
                    <table class="lpay_custom_table" cellspacing="0" width="100%">
                        <thead class="lpay_table_head">
                            <tr>
                                <th>Date</th>
                                <th>Total Transaction</th>
                                <th>Reseller Revenue</th>
                                <th>Reseller GST</th>
                                <s:if test="%{#session.USER.UserType.name()!='RESELLER'}">
                                    <th>PG Profit</th>
                                    <th>PG GST</th>
                                </s:if>
                            </tr>
                        </thead>
                        <tbody class="lpay_table_tbody">

                        </tbody>
                        <tfoot class="no-data-table">
                            <tr>
                                <td colspan="6">No Data Available</td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
                <!-- /.lpay_table -->
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

</body>
</html>