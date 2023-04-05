<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Merchant Details</title>
<link rel="stylesheet" href="../css/jquery-ui.css">
<script src="../js/jquery-latest.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
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
                    <h2 class="heading_text">Merchant Details Filter</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-3 mb-20">
                <div class="lpay_select_group">
                    <label for="">Merchant</label>

                    <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
					<s:select
                    name="merchant"
                    data-id="reportMerchant"
					data-live-search="true"
					class="selectpicker lpay-input"
					id="merchantReportPayId"
					headerKey=""
					headerValue="ALL"
					list="merchantList"
					listKey="payId"
					listValue="businessName"
					autocomplete="off" />
                    </s:if>
                    <s:else>
                    <s:if
                    test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                    <s:select name="merchant" data-id="reportMerchant" class="selectpicker lpay-input"
                        id="merchantReportPayId" headerKey="" data-live-search="true" headerValue="ALL"
                        list="merchantList" listKey="payId"
                        listValue="businessName" autocomplete="off" />
                    </s:if>
                    <s:else>
                        <s:select name="merchant" data-id="reportMerchant" data-live-search="true" class="selectpicker lpay-input" id="merchantReportPayId"
                            list="merchantList" listKey="payId"
                            listValue="businessName" autocomplete="off" />
                    </s:else>
                </s:else>
                </div>
                <!-- /.lpay_select_group -->  
            </div>

            <s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchant" name="subMerchant" class="selectpicker" id="subMerchant"
							list="subMerchantList" listKey="emailId" headerValue="ALL" headerKey=""
							listValue="businessName" autocomplete="off" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-3 mb-20 d-none" data-id="submerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchant" id="subMerchant" data-id="subMerchant" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
            
            
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
                                <th>Registration Date</th>
                                <th>Merchant Name</th>
                                <th>Sub Merchant</th>
                                <th>Pay ID</th>
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
    <s:hidden name="isSuperMerchant" id="isSuperMerchant"></s:hidden>
    <s:hidden name="token" value="%{#session.customToken}" />
    <s:hidden id="userType" value="%{#session.USER.UserType}"></s:hidden>

    <s:form id="merchantDownload"  action="merchantDownload" >
        <s:hidden name="reportMerchant" id="reportMerchant"></s:hidden>
    <!-- <s:hidden name="subMerchant" id="subMerchant"></s:hidden> -->
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
                _table.columns(2).visible(true);
            }else{
                _table.columns(2).visible(false);
            }
            
        }



        $(document).ready(function(e){

            // function for super merchant 
            $("#merchantReportPayId").on("change", function(e){
			var _merchant = $(this).val();
				$("body").removeClass("loader--inactive");
				$.ajax({
					type: "POST",
					url: "getSubMerchantListByPayId",
					data: {"payId": _merchant},
					success: function(data){
                        console.log(data);
						$("#subMerchant").html("");
						if(data.superMerchant == true){
                            $("#isSuperMerchant").val("Y");
							var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
							for(var i = 0; i < data.subMerchantList.length; i++){
								_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
							}
							$("[data-id=submerchant]").removeClass("d-none");
							$("#subMerchant option[value='']").attr("selected", "selected");
							$("#subMerchant").selectpicker();
                            $("#subMerchant").selectpicker("refresh");
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
						}else{
                            $("#isSuperMerchant").val("N");
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
							$("[data-id=submerchant]").addClass("d-none");
							$("[data-id=deliveryStatus]").addClass("d-none");
							$("[data-id=deliveryStatus]").val("");
							$("#subMerchant").val("");
						}
					}
				});
			
		})

            // function for datatable hide columnd

            $(".downloadData").on("click", function(e){
            	if($("[data-id=reportMerchant]").val() == ""){
                    $("#reportMerchant").val("All");
                }else{
                    $("#reportMerchant").val($("[data-id=reportMerchant]").val());
                }
                $("#reportSubMerchant").val($("[data-id=subMerchant]").val());
               // $("#subMerchant").val($("[data-id=subMerchant]").val());
                $("#merchantDownload").submit();
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
				
				$("#datatable").dataTable({
                    dom : 'BTftlpi',
                    buttons: ['csv', 'print', 'pdf'],
                    language: {
                        search: "",
                        searchPlaceholder: "Search records"
                    },
                    "ajax": {
                        "type": "post",
                        "url": "merchantData",
                        "data" : function (d){
                                return generatePostData(d);
                            }
                        },
                        "initComplete" : function(settings, json) {
                                $("#setSuperMerchant").val(json.flag);
                                hideColumn();
                            },

                          /*   
                          "searching" :false,
                          "ordering" :false,
                          "destroy":true,
                          "processing" :true,
                          "serverSide" :true, */

                          "destroy":true,
                          "bSort": true,

                        //"destroy": true,
                        "aoColumns": [
                        	{"mData" : "registrationDate"},
                        {
                            "mDataProp": "businessName",
                            "className": "my_class"
                        },
                        {"mData" : "subMerchant"},
                        {"mData" : "payId"}, 
                        {"mData" : "status"} 
                                
                    ]
                });

            }

            // variable sent to backend function
            function generatePostData(d) {
              
               var payId = $("#merchantReportPayId").val();
               var _subMerchant = $("#subMerchant").val();

                if(payId == '') {
                    payId = 'ALL';
			        }
                var obj = {
                    payId : payId,
                    //subMerchantPayId : $("#subMerchant").val(),
                   
                    subMerchant: $("#subMerchant").val(),
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