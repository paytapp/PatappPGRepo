<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>eCollection Report</title>
<link rel="stylesheet" href="../css/jquery-ui.css">
<script src="../js/jquery-latest.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<script src="../js/common-scripts.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<script src="../js/user-script.js"></script>

<style>
    .lp-success_generate, .lp-error_generate {
		background-color: #c0f4b4;
		font-size: 15px;
		padding: 10px;
		text-align: center;
		margin-top: 20px;
		border-radius: 5px;
		border: 1px solid #3b9f24;
	}

	.lp-error_generate{
		background-color: #f79999;
    	border: 1px solid #771313;
	}

	.lp-success_generate p{ 
		color: #326626;
	}

	.lp-error_generate p{
		color: #921919;
	}
</style>

</head>
<!-- /.edit-permission -->
<body class="bodyColor">
    <s:hidden id="setSuperMerchant"></s:hidden>
    <s:hidden id="permission-eCollection" value="%{#session['USER_PERMISSION'].contains('eCollection Transaction')}"></s:hidden>
    <s:hidden id="permission-virtualAccountList" value="%{#session['USER_PERMISSION'].contains('Virtual Account List')}"></s:hidden>
    <s:hidden id="userType" value="%{#session.USER.UserType.name()}" />
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">eCollection Report Filter</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->

        <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex">
                    <li class="lpay-nav-item active" onclick="tabShow(this, 'eCollectionTransaction')">
                        <a href="#" class="lpay-nav-link" data-id="eCollectionTransaction">eCollection Transaction</a>
                    </li>
                    <li class="lpay-nav-item" onclick="tabShow(this, 'vaList')">
                        <a href="#" class="lpay-nav-link" data-id="vaList">VA List</a>
                    </li>
                </ul>
                <!-- /.lpay_tabs -->
            </div>
            <!-- /.col-md-12 -->

            <div class="lpay_tabs_content w-100" data-target='eCollectionTransaction'>
        
                <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                            <label for="">Merchant</label>
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
                                autocomplete="off"
                                data-collection="reportMerchant"

                            />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                </s:if>
                <s:else>
                    <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Merchant</label>
                                <s:select name="merchant" data-id="reportMerchant" class="selectpicker lpay-input"
                                    id="merchantReportPayId" headerKey="" data-live-search="true" headerValue="ALL"
                                    list="merchantList" listKey="payId" data-collection="reportMerchant"
                                    listValue="businessName" autocomplete="off" />
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                    </s:if>
                    <s:elseif  test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId!=null }" >
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Merchant</label>
                                <s:select name="merchant" data-id="reportMerchant" data-live-search="true" class="selectpicker lpay-input" id="merchantReportPayId"
                                    list="subMerchantList" listKey="payId" data-collection="reportMerchant"
                                    listValue="businessName" autocomplete="off" />	
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                    </s:elseif>
                    <s:else>
                        <div class="col-md-3 mb-20 d-none">
                            <div class="lpay_select_group">
                                <label for="">Merchant</label>
                                <s:select name="merchant" data-id="reportMerchant" data-live-search="true" class="selectpicker lpay-input" id="merchantReportPayId"
                                    list="merchantList" listKey="payId" data-collection="reportMerchant"
                                    listValue="businessName" autocomplete="off" />
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>      
                    </s:else>
                </s:else>
                    
    
                <s:if test="%{#session['USER'].superMerchant == true}">
                    <div class="col-md-3 mb-20" data-id="submerchant">
                        <div class="lpay_select_group">
                           <label for="">Sub Merchant</label>
                           <s:select data-id="subMerchant" data-collection="reportSubMerchant" name="subMerchant" class="selectpicker" id="subMerchant"
                                list="subMerchantList" listKey="emailId" headerValue="ALL" headerKey="ALL"
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
                           <select name="subMerchant" data-collection="reportSubMerchant" id="subMerchant" data-id="subMerchant" class=""></select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->							
                </s:else>
                
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Select Payment Type</label>
                       <select name="paymentMode" data-collection="paymentMode" id="paymentMode" data-id="reportPaymentMode" class="selectpicker">
                        <option value="All">ALL</option>
                        <option value="IMPS">IMPS</option>
                        <option value="NEFT">NEFT</option>
                        <option value="RTGS">RTGS</option>
                        <option value="Topup">Topup</option>
                    </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Status</label>
                       <select name="status" data-collection="reportStatus" id="status" data-id="reportStatus" class="selectpicker">
                        <option value="All">ALL</option>
                        <option value="Captured">Captured</option>
                        <option value="Settled">Settled</option>
                    </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Transaction Type</label>
                       <select name="txnType" id="txnType" data-collection="reportTxnType" data-id="reportTxnType" class="selectpicker">
                        <option value="All">ALL</option>
                        <option value="COLLECTION">ECollection</option>
                        <option value="Topup">Topup</option>
                    </select>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Date From</label> 
                        <s:textfield type="text" data-collection="reportDateFrom" onchange="dateBaseDownload()" id="dateFrom" data-id="reportDateFrom" name="dateFrom"
                        class="lpay_input datepick" autocomplete="off" readonly="true" />
                    </div>
                    <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                    <label for="">Date To</label>
                    <s:textfield type="text" data-collection="reportDateTo" onchange="dateBaseDownload()" id="dateTo" data-id="reportDateTo" name="dateTo"
                    class="lpay_input datepick" autocomplete="off" readonly="true" />
                </div>
                <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 -->
                <div class="col-md-12 mb-20 text-center">
                <button class="lpay_button lpay_button-md lpay_button-secondary viewData">View</button>
                <button class="lpay_button lpay_button-md lpay_button-primary downloadData" id="downloadButton">Download</button>
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12 d-none">
                    <div class="lp-success_generate">
                        <p>Your file has been generate successfully please see after some time</p>
                    </div>
                    <!-- /.lp-success_generate -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12 d-none">
                    <div class="lp-error_generate">
                        <p>Please try again after some time</p>
                    </div>
                    <!-- /.lp-success_generate -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12">
                    <div class="lpay_table">
                    
                        <table id="datatable" class="display" cellspacing="0" width="100%">
                            <thead class="lpay_table_head">
                                <tr>
                                    <th>Merchant Name</th>
                                    <th>Sub Merchant</th>
                                    <th>Merchant Virtual Account Number</th>
                                    <th>Transaction Date </th>
                                    <th>Payment Gateway Code</th>
                                    <th>Payment Gateway Account Number</th>
                                    <th>Payment Mode </th>
                                    <th>Transaction Type</th>
                                    <th>Payee Name</th>
                                    <th>Payee Account number</th>
                                    <th>Payee Bank IFSC</th>
                                    <th>Bank TXN Number </th>
                                    <th>Total PA Commission</th>
                                    <th>Settled Amount</th>
                                    <th>Txn Amount</th>
                                    <th>Status</th>
                                    <th>Sender Remark</th>
                                </tr>
                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content w-100 -->
            <div class="lpay_tabs_content w-100 d-none" data-target='vaList'>

                <s:if test="%{#session.USER.UserType.name()=='RESELLER'}">
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                            <label for="">Merchant</label>
                            <s:select
                                name="merchant"
                                data-id="vaListMerchant"
                                data-live-search="true"
                                class="selectpicker lpay-input"
                                id="vaListMerchant"
                                data-submerchant="vaSubMerchant"
							    data-user="vaSubUser"
                                headerKey="ALL"
                                headerValue="ALL"
                                list="merchantList"
                                listKey="payId"
                                listValue="businessName"
                                autocomplete="off"
                            />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                </s:if>
                <s:else>
                    <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Merchant</label>
                                <s:select name="merchant" data-submerchant="vaSubMerchant"
							    data-user="vaSubUser" data-id="vaListMerchant" class="selectpicker lpay-input"
                                    id="vaListMerchant" headerKey="ALL" data-live-search="true" headerValue="ALL"
                                    list="merchantList" listKey="payId"
                                    listValue="businessName" autocomplete="off" />
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                    </s:if>
                    <s:elseif  test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId!=null }" >
                        <div class="col-md-3 mb-20">
                            <div class="lpay_select_group">
                                <label for="">Merchant</label>
                                <s:select name="merchant" data-id="vaListMerchant" data-live-search="true" class="selectpicker lpay-input" id="vaListMerchant"
                                    list="subMerchantList" listKey="payId"
                                    listValue="businessName" autocomplete="off" />	
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                    </s:elseif>
                    <s:else>
                        <div class="col-md-3 mb-20 d-none">
                            <div class="lpay_select_group">
                                <label for="">Merchant</label>
                                <s:select name="merchant" data-id="vaListMerchant" data-live-search="true" class="selectpicker lpay-input" id="vaListMerchant"
                                    list="merchantList" listKey="payId"
                                    listValue="businessName" autocomplete="off" />
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>      
                    </s:else>
                </s:else>
                <s:if test="%{#session['USER'].superMerchant == true}">
                    <div class="col-md-3 mb-20" data-target="vaSubMerchant">
                        <div class="lpay_select_group">
                           <label for="">Sub Merchant</label>
                           <s:select data-id="subMerchant" data-submerchant="vaSubMerchant" name="subMerchant" class="selectpicker" id="vaSubMerchant"
                                list="subMerchantList" listKey="emailId" headerValue="ALL" headerKey="ALL"
                                listValue="businessName" autocomplete="off" />
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->	
                </s:if>
                <s:else>
                    <div class="col-md-3 mb-20 d-none" data-target="vaSubMerchant"> 
                        <div class="lpay_select_group">
                           <label for="">Sub Merchant</label>
                           <select name="subMerchant" data-submerchant="vaSubMerchant" id="vaSubMerchant" data-id="subMerchant" class=""></select>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-3 -->							
                </s:else>
                <div class="col-md-3 mb-20">
                    <div class="lpay_input_group">
                        <label for="">Virtual Account Number</label>
                        <input type="text" class="lpay_input input-caps" onkeypress="lettersAndAlphabet(event);" data-var='virtualAccountNo'>
                    </div>
                    <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-4 -->

                <div class="col-md-3" style="padding-top: 17px">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="view-vlSubMerchant">
                        View
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-secondary -->
                    <button class="lpay_button lpay_button-md lpay_button-primary" onclick="downloadVlList()">
                        Download
                    </button>
                    <!-- /.lpay_button lpay_button-md lpay_button-primary -->
                </div>
                <!-- /.col-md-12 -->

                <div class="col-md-12">
                    <div class="lpay_table">
                        <table id="vaListTable" class="display" cellspacing="0" width="100%">
                            <thead class="lpay_table_head" data-id="vaListTable">

                            </thead>
                        </table>
                    </div>
                    <!-- /.lpay_table -->
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.lpay_tabs_content w-100 -->

        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    <s:hidden name="isSuperMerchant" id="isSuperMerchant"></s:hidden>
    <s:hidden name="token" value="%{#session.customToken}" />
    <s:hidden id="userType" value="%{#session.USER.UserType}"></s:hidden>

    <s:form id="eCollectionDownload"  action="eCollectionDownload" >
        <s:hidden name="reportMerchant" id="reportMerchant"></s:hidden>
    <!-- <s:hidden name="subMerchant" id="subMerchant"></s:hidden> -->
        <s:hidden name="reportPaymentMode" id="reportPaymentMode"></s:hidden>
        <s:hidden name="reportStatus" id="reportStatus"></s:hidden>
        <s:hidden name="reportTxnType" id="reportTxnType"></s:hidden>
        <s:hidden name="reportDateFrom" id="reportDateFrom"></s:hidden>
        <s:hidden name="reportDateTo" id="reportDateTo"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
        <s:hidden name="subMerchant" id="reportSubMerchant" value=""></s:hidden>
    </s:form>

    <s:form id="virtualAccountForm" action="downloadVirtaulAccountList">
        <s:hidden name="reportMerchant" id="reportMerchantVl"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
        <s:hidden name="subMerchant" id="reportSubMerchantVl" value=""></s:hidden>
        <s:hidden name='virtualAccountNo' id="virtualAccountNo"></s:hidden>
    </s:form>

    <script type="text/javascript">

        function hideColumn(){
            var _userType = $("#userType").val();
            var _userLogin = $("#setSuperMerchant").val();
            var _isSuperMerchant = $("#isSuperMerchant").val();
            var _merchant = $("#merchantReportPayId").val();
            if(_userLogin == "true"){
                _isSuperMerchant = "Y"
            }
            console.log(_isSuperMerchant);
            var _table = $("#datatable").DataTable();
            if(_userType == "ADMIN" || _userType == "SUBADMIN"){
                console.log(_table);
                
                _table.columns(4).visible(true);
                _table.columns(5).visible(true);
            }else{
                console.log(_table);
                //console.log("v");
                _table.columns(4).visible(false);
                _table.columns(5).visible(false); 
            }
            if(_isSuperMerchant == "Y" || _merchant == ""){
                _table.columns(1).visible(true);
            }else{
                _table.columns(1).visible(false);
            }
            
        }

        function downloadVlList(){
            document.querySelector("#reportMerchantVl").value = document.querySelector("#vaListMerchant").value;
            document.querySelector("#reportSubMerchantVl").value = document.querySelector("#vaSubMerchant").value;
            document.querySelector("#virtualAccountNo").value = document.querySelector("[data-var='virtualAccountNo']").value;
            document.querySelector("#virtualAccountForm").submit();
        }
        
        function tabShow(_this, _data, _runTim){
            var _allTab = document.querySelectorAll(".lpay_tabs_content");
            var _allLink = document.querySelectorAll(".lpay-nav-item");
            _allTab.forEach(function(index, element, array){
                index.classList.add("d-none");

            })
            _allLink.forEach(function(index, array, element){
                index.classList.remove("active");
                if(_runTim == "load"){
                    index.classList.add("d-none");
                }
            })
            _this.classList.add("active");
            if(_runTim == "load"){
                _this.classList.remove("d-none");
            
            }
            document.querySelector("[data-target='"+_data+"']").classList.remove("d-none");
            _runTime = "yo";
        }

        var _user = document.querySelector("#userType").value;
        if(_user == "RESELLER"){
            var _permissionEcollection = document.querySelector("#permission-eCollection").value;
            var _permissionVaList = document.querySelector("#permission-virtualAccountList").value;
            if(_permissionEcollection == "true" && _permissionVaList == "true"){
                // console.log("hello");
            }else if(_permissionVaList == "true"){
                tabShow(document.querySelector("[data-id='vaList']").closest("li"), "vaList", "load");
            }else if(_permissionEcollection == "true"){
                tabShow(document.querySelector("[data-id='eCollectionTransaction']").closest("li"), "eCollectionTransaction", "load");
            }

        }

        function lettersAndAlphabet(event) {
            var x = event.keyCode;
            if (x > 64 && x < 91 || x > 96 && x < 123 || x > 47 && x < 58 || x == 32) {
            } else {
                event.preventDefault();
            }
        }

        $(".input-caps").on("keyup", function(e){
            this.value = this.value.toUpperCase();
        });

        $(document).ready(function(e){

            document.querySelector("#vaListMerchant").addEventListener("change", function(_this){
                getSubMerchant(_this, "getSubMerchantList", {
                    isSuperMerchant : true
                });
            });
    
          
    
            function vlDataList(_selector){
                document.querySelector("body").classList.remove("loader--inactive");
                var _dataInput = {
                    "payId" : document.querySelector("#vaListMerchant").value,
                    "subMerchantPayId" : document.querySelector("#vaSubMerchant").value,
                    "virtualAccountNo" : document.querySelector("[data-var='virtualAccountNo']").value
                }
                _dataValue = [
                    {'title' : 'Reseller', 'mData' : 'reseller'},
                    {'title': 'Merchant','mData' : "merchant"},
                    {'title' : 'Sub-Merchant', 'mData' : "subMerchant"},
                    {'title' : 'Virtual Account Number','mData' : "merchantVirtualAccountNumber"},
                    {'title' : 'Virtual Account Flag', 'mData' : 'virtualAccountFlag'}
                ]
                $("#vaListTable").dataTable({
                    "ajax" : {
                        "url" : "virtaulAccountList",
                        "type" : "POST",
                        "data" : _dataInput
                    },
                    "initComplete" : function(settings, json) {
                        if(_selector != "outside"){
                            console.log(settings.json);
                            hideColumnVl(settings.json);
                        }else{
                            document.querySelector("body").classList.add("loader--inactive");
                        }
                    },
                    "destroy": true,
                    "columns" :_dataValue,
                });

            }
    
            vlDataList("outside");

            document.querySelector("#view-vlSubMerchant").onclick = function(e){
                vlDataList("click");
            }

            function hideColumnVl(_data){
                var _table = $("#vaListTable").DataTable();
                _table.columns(0).visible(true);
                _table.columns(2).visible(true);
                if(_data.aaData.length > 0){
                    if(_data.aaData[0].reseller == "NA"){
                        _table.columns(0).visible(false);
                    }
                }
                if(_data.aaData.length > 0){
                    if(_data.aaData[0].subMerchant == "NA"){
                        _table.columns(2).visible(false);
                    }
                }
                document.querySelector("body").classList.add("loader--inactive");
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
                var _text = $(this).text();
                var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
                var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
                if (transTo - transFrom > 61 * 86400000) {
                    alert('No. of days can not be more than 60 days');
                    $("body").addClass("loader--inactive");
                    $('#dateFrom').focus();
                    return false;
                }
                if(_text == "Download"){
                    if($("[data-id=reportMerchant]").val() == ""){
                    $("#reportMerchant").val("All");
                    }else{
                        $("#reportMerchant").val($("[data-id=reportMerchant]").val());
                    }
                    $("#reportSubMerchant").val($("[data-id=subMerchant]").val());
                // $("#subMerchant").val($("[data-id=subMerchant]").val());
                    $("#reportPaymentMode").val($("[data-id=reportPaymentMode]").val());
                    $("#reportStatus").val($("[data-id=reportStatus]").val());
                    $("#reportTxnType").val($("[data-id=reportTxnType]").val());
                    $("#reportDateFrom").val($("[data-id=reportDateFrom]").val());
                    $("#reportDateTo").val($("[data-id=reportDateTo]").val());
                    $("#eCollectionDownload").submit();
                }else{
                    var _input = document.querySelectorAll("input[data-id]");
                    var _select = document.querySelectorAll("select[data-id]");
                    var _obj = {};
                    _input.forEach(function(index, array, element){
                        _obj[index.getAttribute("data-id")] = index.value;
                    })
                    _select.forEach(function(index, array, element){
                        _obj[index.getAttribute("data-id")] = index.value;
                    })
            
                    if(_obj['reportMerchant'] == ""){
                        _obj['reportMerchant'] = "ALL";
                    }
                    _obj['reportType'] = "eCollection";
                    _obj['subMerchant'] = $("[data-collection='reportSubMerchant']").val();
                    document.querySelector("body").classList.remove("loader--inactive");
                    $.ajax({
                        type: "POST",
                        url: "generateEcollectionReportFileAction",
                        data: _obj,
                        success: function(data){
                            setTimeout(function(e){
                                document.querySelector("body").classList.add("loader--inactive");
                                if(data.generateReport == true){
                                    document.querySelector(".lp-success_generate").closest(".col-md-12").classList.remove("d-none");
                                }else{
                                    document.querySelector(".lp-error_generate").closest(".col-md-12").classList.remove("d-none");
                                }
                                document.querySelector("body").classList.add("loader--inactive");
                            }, 500)
                            setTimeout(function(e){
                                removeError()
                            }, 4000);
                        }
                    })
                }
            })

            function removeError(){
                document.querySelector(".lp-error_generate").closest(".col-md-12").classList.add("d-none");
                document.querySelector(".lp-success_generate").closest(".col-md-12").classList.add("d-none");
            }

            $(".viewData").on("click", function(e){
                $("body").removeClass("loader--inactive");
                handleChange();
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 1500);
            });
            
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
                    "ajax": {
                        "type": "post",
                        "url": "eCollectionData",
                        "data" : function (d){
                                return generatePostData(d);
                            }
                        },
                        "initComplete" : function(settings, json) {
                            $("#setSuperMerchant").val(json.flag);
                            hideColumn();
                        },
                        "searching" :false,
                        "ordering" :false,
                        "destroy":true,
                        "processing" :true,
                        "serverSide" :true,
                        //"destroy": true,
                        "aoColumns": [
                        {
                            "mDataProp": "merchant",
                            "className": "my_class"
                        },
                        {"mData" : "subMerchant"},
                        {"mData" : "merchantVirtualAccountNumber", "className": "text-center"}, 
                        {"mData" : "transactionDate"}, 
                        {"mData" : "paymentGatewayCode"}, 
                        {"mData" : "paymentGatewayAccountNumber"}, 
                        {"mData" : "paymentMode"},
                        {"mData" : "txnType"}, 
                        {"mData" : "payeeName"},
                        {"mData" : "payeeAccountNumber"},	
                        {"mData" : "payeeBankIFSC"},
                        {"mData" : "bankTxnNumber"},
                        {"mData" : "totalPaCommission" },
                        {"mData" : "amount"},
                        {"mData" : "totalAmount" },
                        {"mData" : "status"},
                        {"mData" : "senderRemark"}                                
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
                    subMerchantPayId : $("#subMerchant").val(),
                    paymentMode : $("#paymentMode").val(),
                    status : $("#status").val(),
                    txnType : $("#txnType").val(),
                    dateFrom : $("#dateFrom").val(),
                    dateTo : $("#dateTo").val(),
                    subMerchant: $("#subMerchant").val(),
                    draw : d.draw,
                    length : d.length,
                    start : d.start,
                    token : $("[name=token]").val(),
                    "struts.token.name" : "token",
                };
                return obj;
	        }
        });

        function dateBaseDownload(){
            var transFrom = $.datepicker
                    .parseDate('dd-mm-yy', $('#dateFrom').val());
            var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
            
            if (transTo - transFrom > 30 * 86400000) {
                document.querySelector("#downloadButton").innerText = "Generate";
            }else{
                document.querySelector("#downloadButton").innerText = "Download";
            }
        }

    </script>    
</body>
</html>