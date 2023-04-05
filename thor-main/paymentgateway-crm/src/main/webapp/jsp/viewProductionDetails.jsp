<%@page import="com.paymentgateway.commons.util.SaltFactory"%>
<%@page import="com.paymentgateway.commons.user.User"%>


<%@page import="com.paymentgateway.commons.util.PropertiesManager"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>View Production Details</title>
<link rel="icon" href="../image/favicon-32x32.png">
<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
<!-- <link href="../css/custom.css" rel="stylesheet" type="text/css" /> -->
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/jquery.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<script src="../js/bootstrap-select.min.js"></script>

<style>
    #datatable{
        pointer-events: none;
        -webkit-user-select: none;
        -webkit-touch-callout: none;
        -moz-user-select: none;
        -ms-user-select: none;
        user-select: none;
    }
</style>

</head>
<body>

	<section class="reseller-setup lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">View Production Details</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12 mb-20">
				<ul class="lpay_tabs d-flex">
                    <li class="lpay-nav-item active">
                        <a href="#" class="lpay-nav-link" data-id="productionDetails">Production Details</a>
                    </li>
                    <li class="lpay-nav-item">
                        <a href="#" class="lpay-nav-link" data-id="mappingDetails">Mapping Details</a>
					</li>
                </ul>
                <!-- /.lpay_tabs -->
			</div>
			<!-- /.col-md-12 -->
            <div class="col-md-12">
                <div class="lpay_tab-content" data-target="productionDetails">
                    <div class="row">
                        <div class="col-md-4 mb-20">
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
                        <!-- col-md-4 -->
                        <%-- <s:if test="%{#session['USER'].superMerchant == true}">
                            <div class="col-md-4 mb-20" data-id="submerchant">
                                <div class="lpay_select_group">
                                <label for="">Sub Merchant</label>
                                <s:select data-id="subMerchant" name="subMerchant" class="selectpicker" id="subMerchant"
                                        list="subMerchantList" listKey="emailId" headerValue="ALL" headerKey=""
                                        listValue="businessName" autocomplete="off" />
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>
                            <!-- /.col-md-4 -->	
                        </s:if>
                        <s:else>
                            <div class="col-md-4 mb-20 d-none" data-id="submerchant"> 
                                <div class="lpay_select_group">
                                    <label for="">Sub Merchant</label>
                                    <select name="subMerchant" id="subMerchant" data-id="subMerchant" class=""></select>
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>
                            <!-- /.col-md-4 -->							
                        </s:else> --%>
                        
                        <div class="col-md-4 mb-20">
                            <div class="lpay_select_group">
                                <label for="acquirer">Acquirer</label>
                                <div class="position-relative">
                                    <s:select
                                        name="acquirer"
                                        class="selectpicker"
                                        id="acquirer"
                                        headerKey="ALL"
                                        headerValue="ALL"
                                        list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
                                        listKey="code"
                                        listValue="name"								
                                        autocomplete="off"
                                    />
                                </div>
                                <!-- /.position-relative -->
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-4 -->
                        <div class="col-md-4 mb-20">
                            <div class="lpay_select_group">
                               <label for="">Currency</label>
                               <s:select
                                name=""
                                id="currencyCode"
                                headerValue="ALL"
                                headerKey="ALL"
                                list="currencyMap"
                                listKey="key"
                                listValue="value"
                                class="textFL_merch selectpicker"
                                autocomplete="off"
                            />
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-4 MB-20 -->
                        <div class="col-md-12 text-center mb-20">
                            <button class="lpay_button lpay_button-md lpay_button-secondary" id="viewProButton">View</button>
                        </div>
                        <!-- /.col-md-12 -->
                        <div class="col-md-12">
                            <div class="lpay_table">
                                <table id="datatable" class="display" cellspacing="0" width="100%">
                                    <thead class="lpay_table_head">
                                        <tr>
                                            <th>Merchant Name</th>
                                            <th>Pay ID</th>
                                            <th>Merchant ID</th>
                                            <th>Txn Key</th>
                                            <th>Password</th>
                                            <th>Adf1</th>
                                            <th>Adf2</th>
                                            <th>Adf3</th>
                                            <th>Adf4</th>
                                            <th>Adf5</th>
                                            <th>Adf6</th>
                                            <th>Adf7</th>
                                            <th>Adf8</th>
                                            <th>Adf9</th>
                                            <th>Adf10</th>
                                            <th>Adf11</th>
                                        </tr>
                                    </thead>
                                </table>
                            </div>
                            <!-- /.lpay_table -->
                        </div>
                        <!-- /.col-md-12 -->
                    </div>
                    <!-- /.row -->
                </div>
                <!-- /.lpay_tab-content -->
                <div class="lpay_tab-content d-none" data-target="mappingDetails">
                    <form method="post" action="mappingDownload">
                        <div class="row">
                          <div class="col-md-4 mb-20">
                                <div class="lpay_select_group">
                                    <label for="">Merchant</label>
                                    <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                                    <s:select
                                    name="merchant"
                                    data-live-search="true"
                                    class="selectpicker lpay-input"
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
                                    <s:select name="merchant" data-id="reportMerchant" class="selectpicker lpay-input" headerKey="ALL" data-live-search="true" headerValue="ALL"
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
                            
                            <!-- col-md-4 -->
                            <%-- <s:if test="%{#session['USER'].superMerchant == true}">
                                <div class="col-md-4 mb-20" data-id="submerchant">
                                    <div class="lpay_select_group">
                                    <label for="">Sub Merchant</label>
                                    <s:select data-id="subMerchant" name="subMerchant" class="selectpicker" id="subMerchant"
                                            list="subMerchantList" listKey="emailId" headerValue="ALL" headerKey=""
                                            listValue="businessName" autocomplete="off" />
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                                <!-- /.col-md-4 -->	
                            </s:if>
                            <s:else>
                                <div class="col-md-4 mb-20 d-none" data-id="submerchant"> 
                                    <div class="lpay_select_group">
                                        <label for="">Sub Merchant</label>
                                        <select name="subMerchant" id="subMerchant" data-id="subMerchant" class=""></select>
                                    </div>
                                    <!-- /.lpay_select_group -->  
                                </div>
                                <!-- /.col-md-4 -->							
                            </s:else> --%>
                            
                            <div class="col-md-4 mb-20">
                                <div class="lpay_select_group">
                                    <label for="acquirer">Acquirer</label>
                                    <div class="position-relative">
                                        <s:select
                                            name="acquirer"
                                            class="selectpicker"
                                            headerKey="ALL"
                                            headerValue="ALL"
                                            list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
                                            listKey="code"
                                            listValue="name"								
                                            autocomplete="off"
                                        />
                                    </div>
                                    <!-- /.position-relative -->
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>
                            <!-- /.col-md-4 -->
                            <div class="col-md-4 mb-20">
                                <div class="lpay_select_group">
                                   <label for="">Currency</label>
                                   <s:select
                                    name="currency"
                                    headerValue="ALL"
                                    headerKey="ALL"
                                    list="currencyMap"
                                    listKey="key"
                                    listValue="value"
                                    class="textFL_merch selectpicker"
                                    autocomplete="off"
                                />
                                </div>
                                <!-- /.lpay_select_group -->  
                            </div>  
                             <div class="col-md-12 mb-20 text-center">
                                 <button class="lpay_button lpay_button-md lpay_button-primary downloadData">Download</button>
                             </div>
                        </div>
                        <!-- /.row -->
                    </form>
                </div>
                <!-- /.lpay_tab-content -->
            </div>
            <!-- /.col-md-12 -->
            <!-- <div class="col-md-12 text-center">
                <button class="lpay_button lpay_button-md lpay_button-secondary">Save</button>
            </div> -->
            <!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    <s:hidden id="userType" value="%{#session.USER.UserType}"></s:hidden>
    <script type="text/javascript">
    
		var _button = document.querySelectorAll(".lpay-nav-link");
		_button.forEach(function(e){
			e.addEventListener("click", function(f){
				var _getAttr = f.target.attributes["data-id"].nodeValue;
				var _getAllLink = document.querySelectorAll(".lpay-nav-link");
				var _getAll = document.querySelectorAll(".lpay_tab-content");
				_getAllLink.forEach(function(c){
					c.closest(".lpay-nav-item").classList.remove("active");
				})
				_getAll.forEach(function(d){
					d.classList.add("d-none");
				})
				this.closest(".lpay-nav-item").classList.add("active");
				document.querySelector("[data-target="+_getAttr+"]").classList.remove("d-none");
			})
		});

        $(document).ready(function(e){

            $("body").bind("keydown", function(e) {
                if (e.keyCode == 44) {
                    e.preventDefault();
                    return false;
                }
            });

            // user type 
            var _userType = $("#userType").val();
            if(_userType != "ADMIN"){
                $("[data-id=productionDetails]").closest(".lpay-nav-item").addClass("d-none");
                $("[data-id=mappingDetails]").closest(".lpay-nav-item").addClass("active");
                $("[data-target=productionDetails]").addClass("d-none");
                $("[data-target=productionDetails]").html("");
                $("[data-target=mappingDetails]").removeClass("d-none");
            }
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
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
							$("[data-id=submerchant]").addClass("d-none");
							$("#subMerchant").val("");
						}
					}
				});
		    });

            // variable sent to backend function
            function generatePostData(d) {
              
                var _merchant = $("#merchantReportPayId").val();
                var _acquirer = $("#acquirer").val();
                var _subMerchant = $("#subMerchant").val();
                var _currency = $("#currencyCode").val();
                if(_merchant == '') {
                    _merchant = 'ALL';
			        }
                console.log(_subMerchant);
               var obj = {
                   merchant : _merchant,
                   acquirer : _acquirer,
                   subMerchant : _subMerchant,
                   currency : _currency,
                   draw : d.draw,
                   length : d.length,
                   start : d.start,
               };
               return obj;
           }

           $("#viewProButton").on("click", function(e){
                renderTable();
                console.log("hi");
           });
            renderTable();
            // function for datatable
            function renderTable(){
                $("body").removeClass("loader--inactive");
                $("#datatable").dataTable({
                    dom : 'BTftlpi',
                    buttons: ['csv', 'print', 'pdf'],
                    language: {
                        search: "",
                        searchPlaceholder: "Search records"
                    },
                    "ajax": {
                        "type": "post",
                        "url": "productionData",
                        "data" : function (d){
                                return generatePostData(d);
                            }
                        },
                        "destroy":true,
                        "bSort": true,
                        "aoColumns": [
                        {
                            "mDataProp": "merchant",
                            "className": "my_class"
                        },
                        {"mData" : "payId"}, 
                        {"mData" : "merchantId"}, 
                        {"mData" : "txnKey","render": $.fn.dataTable.render.text()}, 
                        {"mData" : "password","render": $.fn.dataTable.render.text()}, 
                        {"mData" : "adf1","render": $.fn.dataTable.render.text()},
                        {"mData" : "adf2","render": $.fn.dataTable.render.text()}, 
                        {"mData" : "adf3","render": $.fn.dataTable.render.text()},
                        {"mData" : "adf4","render": $.fn.dataTable.render.text()},	
                        {"mData" : "adf5","render": $.fn.dataTable.render.text()},
                        {"mData" : "adf6","render": $.fn.dataTable.render.text()},
                        {"mData" : "adf7","render": $.fn.dataTable.render.text()},
                        {"mData" : "adf8","render": $.fn.dataTable.render.text()},
                        {"mData" : "adf9","render": $.fn.dataTable.render.text()},
                        {"mData" : "adf10","render": $.fn.dataTable.render.text()},
                        {"mData" : "adf11","render": $.fn.dataTable.render.text()}         
                    ]
                });
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 1000)
            }
        })
	</script>
</body>
</html>