<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Nodal Settlement</title>
<link rel="stylesheet" href="../css/jquery-ui.css">
<script src="../js/jquery-latest.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<style>
    button#selectAll {
        position: absolute;
        padding: 5px 10px;
        left: 195px;
    }
    span#selectRow {
    padding: 5px 8px;
    background-color: #fff;
    border-radius: 5px;
    color: #000;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    }
    .text-center { text-align: center !important; }
    .no-wrap { white-space: nowrap !important; }
</style>
</head>
<body class="bodyColor">
    <div class="edit-permission"><s:property value="%{editingpermission}"/></div>
    <s:hidden id="isSuperMerchant"></s:hidden>
    <s:hidden id="setSuperMerchant"></s:hidden>
    <s:hidden id="userType" value="%{#session.USER.UserType.name()}"></s:hidden>
    <!-- /.edit-permission -->
    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Nodal Settlement</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->

            <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex" id="nodal-report-navigation">                    
                    <s:if test="%{#session.USER.nodalReportFlag == true}">
                        <li class="lpay-nav-item">
                            <a href="#" class="lpay-nav-link" data-id="search-transaction">CIB Report</a>
                        </li>
                    </s:if>
                </ul>
                <!-- /.lpay_tabs -->
            </div>
            <!-- /.col-md-12 -->

            <s:if test="%{#session.USER.nodalReportFlag== true}">
                <div class="lpay_tabs_content w-100" data-target="search-transaction">
                    <div class="col-md-4 mb-20">
                        <div class="lpay_input_group">
                            <label for="st-txnId">Txn ID</label>
                            <div class="position-relative">
                                <s:textfield
                                    type="text"                                
                                    id="st-txnId"
                                    name="txnId"
                                    class="lpay_input"
                                    autocomplete="off"
                                />
                                <span data-error="st-txnId" class="error font-size-12"></span>
                            </div>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 mb-20 -->
    
                    <div class="col-md-4 mb-20">
                        <div class="lpay_input_group">
                            <label for="st-utrNo">UTR</label>
                            <div class="position-relative">
                                <s:textfield
                                    type="text"                                
                                    id="st-utrNo"
                                    name="utrNo"
                                    class="lpay_input"
                                    autocomplete="off"
                                />
                                <span data-error="st-utrNo" class="error font-size-12"></span>
                            </div>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 mb-20 -->
    
                    
                            <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
                                <div class="col-md-4 mb-20">
                                    <div class="lpay_select_group">
                                        <label for="st-payId">Merchant</label>
                            <div class="position-relative">
                                    <s:select
                                        name="payId"
                                        class="selectpicker lpay-input merchant-selectbox"
                                        id="st-payId"
                                        headerKey="ALL"
                                        data-live-search="true"
                                        headerValue="ALL"
                                        list="merchantList"
                                        listKey="payId"
                                        listValue="businessName"
                                        autocomplete="off"
                                        data-name="st-subMerchant"
                                    />
                                </div>
                               </div> 
                                </div>
                            </s:if>
                            <s:else>
                                <div class="col-md-4 mb-20 d-none">
                                    <div class="lpay_select_group">
                                        <label for="st-payId">Merchant</label>
                                <div class="position-relative">
                                    <s:select
                                        name="payId"
                                        data-live-search="true"
                                        class="selectpicker lpay-input merchant-selectbox"
                                        id="st-payId"
                                        list="merchantList"
                                        listKey="payId"
                                        listValue="businessName"
                                        autocomplete="off"
                                        data-name="st-subMerchant"
                                    />
                                </div>
                            </div>
                        </div> 
                            </s:else>
                       
                       
    
                    <s:if test="%{#session['USER'].superMerchant == true}">
                        <div class="col-md-4 mb-20" data-id="st-subMerchant">
                            <div class="lpay_select_group">
                                <label for="st-subMerchant">Sub Merchant</label>
                                <div class="position-relative">
                                    <s:select
                                        name="subMerchant"
                                        class="selectpicker lpay-input"
                                        id="st-subMerchant"
                                        list="subMerchantList"
                                        listKey="payId"
                                        listValue="businessName"
                                        autocomplete="off"
                                        headerKey="" headerValue="ALL"
                                    />
                                </div>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-4 -->	
                    </s:if>
                    <s:else>
                        <div class="col-md-4 mb-20 d-none" data-id="st-subMerchant"> 
                            <div class="lpay_select_group">
                                <label for="st-subMerchant">Sub Merchant</label>
                                <div class="position-relative">
                                    <select name="subMerchant" id="st-subMerchant" class="lpay-input"></select>
                                    <span data-error="st-subMerchant" class="error font-size-12"></span>
                                </div>
                            </div>
                            <!-- /.lpay_select_group -->  
                        </div>
                        <!-- /.col-md-4 -->
                    </s:else>
                    
                    <div class="col-md-4 mb-20">
                        <div class="lpay_select_group">
                            <label for="st-status">Status</label>
                            <div class="position-relative">
                                <select class="selectpicker" name="status" id="st-status">
                                    <option value="ALL">All</option>
                                    <option value="Captured">Captured</option>
                                    <option value="Failed">Failed</option>
                                    <option value="Duplicate">Duplicate</option>
                                    <option value="Pending">Pending</option>
                                    <option value="Processing">Processing</option>
                                </select>
                                <span class="error font-size-12" data-error="st-status"></span>
                            </div>
                        </div>
                        <!-- /.lpay_select_group -->  
                    </div>
                    <!-- /.col-md-4 -->
                    
    
                    <div class="col-md-4 mb-20">
                        <div class="lpay_input_group">
                            <label for="st-dateFrom">Date From</label>
                            <div class="position-relative">
                                <s:textfield
                                    type="text"
                                    id="st-dateFrom"
                                    name="dateFrom"
                                    class="lpay_input datepick"
                                    autocomplete="off"
                                    readonly="true"
                                />
                                <span class="error font-size-12" data-error="st-dateFrom"></span>
                            </div>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 mb-20 -->
    
                    <div class="col-md-4 mb-20">
                        <div class="lpay_input_group">
                            <label for="st-dateTo">Date To</label>
                            <div class="position-relative">
                                <s:textfield
                                    type="text"
                                    id="st-dateTo"
                                    name="dateTo"
                                    class="lpay_input datepick"
                                    autocomplete="off"
                                    readonly="true"
                                />
                                <span class="error font-size-12" data-error="st-dateTo"></span>
                            </div>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->
    
                    <div class="col-md-12 mb-20 text-center">
                        <button class="lpay_button lpay_button-md lpay_button-secondary st-viewData">View</button>
                        <button class="lpay_button lpay_button-md lpay_button-primary downloadTranData">Download</button>
                    </div>
                    <!-- /.col-md-12 -->
    
                    <div class="col-md-12">
                        <div class="lpay_table">
                            <table id="st-datatable" class="display text-center" cellspacing="0" width="100%">
                                <thead class="lpay_table_head">
                                    <tr>
                                        <th class="text-center">Merchant Name</th>
                                        <th class="text-center">Sub Merchant Name</th>
                                        <th class="text-center">Payout Date</th>
                                        <th class="text-center">Txn ID</th>
                                        <th class="text-center">Captured Date From</th>
                                        <th class="text-center">Captured Date To</th>
                                        <th class="text-center">UTR Number</th>
                                        <th class="text-center">Pay Id</th>
                                        <th class="text-center">Payee Name</th>
                                        <th class="text-center">Payee Account Number</th>
                                        <th class="text-center">IFSC Code</th>
                                        <th class="text-center">Currency</th>
                                        <th class="text-center">TXN Mode</th>
                                        <th class="text-center">Amount</th>
                                        <th class="text-center">Status</th>
                                        <th class="text-center">Response Message</th>
                                        <th class="text-center">Remarks</th>
                                        <th class="text-center">Action</th>
                                    </tr>
                                </thead>
                            </table>
                        </div>
                        <!-- /.lpay_table -->
                    </div>
                    <!-- /.col-md-12 -->
                </div>
                <!-- /.lpay_tabs_content -->
            </s:if>
            

            

    </div>
    <!-- /.row -->
</section>
<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
     <s:hidden name="isSuperMerchant" id="isSuperMerchant"></s:hidden>
     <s:hidden name="token" value="%{#session.customToken}" />

     <div class="lpay_popup">
        <div class="lpay_popup-inner">
            <div class="lpay_popup-innerbox" data-status="default">
                <div class="lpay_popup-innerbox-success lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Amount has been transferred successfully.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->

                <div class="lpay_popup-innerbox-error lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/wrong-tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Nothing Found Try Again.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->
            </div>
            <!-- /.lpay_popup-innerbox -->
        </div>
        <!-- /.lpay_popup-inner -->
    </div>
    <!-- /.lpay_popup -->



     <s:form id="downloadReport"  action="downloadReport" >
        <s:hidden name="reportMerchant" id="reportMerchant"></s:hidden>
        <s:hidden name="reportStatus" id="reportStatus"></s:hidden>
        <s:hidden name="reportChannel" id="reportChannel"></s:hidden>
        <s:hidden name="reportDateFrom" id="reportDateFrom"></s:hidden>
        <s:hidden name="reportDateTo" id="reportDateTo"></s:hidden>
        <s:hidden name="reportSubMerchant" id="reportSubMerchant"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>

    <s:form id="downloadTranReport" action="downloadTranReport">
        <s:hidden name="payId" id="tranPayId"></s:hidden>
        <s:hidden name="subMerchant" id="transubPayId"></s:hidden>
        <s:hidden name="bankAccountNumber" id="tranAccountNumber"></s:hidden>
        <s:hidden name="payeeType" id="tranPayeeType"></s:hidden>
        <s:hidden name="txnId" id="tranTxnId"></s:hidden>
        <s:hidden name="utrNo" id="tranUtr"></s:hidden>
        <s:hidden name="status" id="tranStatus"></s:hidden>
        <s:hidden name="dateFrom" id="tranDateFrom"></s:hidden>
        <s:hidden name="dateTo" id="tranDateTo"></s:hidden>
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>

    <script type="text/javascript">

    function hideColumnPayout(){
        var _userType = $("#userType").val();
        var _userLogin = $("#setSuperMerchant").val();
        var _isSuperMerchant = $("#isSuperMerchant").val();
        
        if(_userLogin == "true"){

            _isSuperMerchant = "Y"

        }

        var _table = $("#datatable").DataTable();

        _table.columns(1).visible(true);

        if(_userType == "MERCHANT" && _userLogin == "true"){

            _table.columns(0).visible(true);
            _table.columns(1).visible(true);

        }

        if(_userType == "MERCHANT" && _userLogin == "false"){

            _table.columns(0).visible(true);
            _table.columns(1).visible(false);

        }
        
        
        if(_isSuperMerchant == "N"){

            _table.columns(1).visible(false);
        }
        
    }

    // only letters
    function onlyLetters(event) {
        var x = event.keyCode;
        if (x > 64 && x < 91 || x > 96 && x < 123 || x == 32) {
            
        } else {
            event.preventDefault();
        }
    }

    function lettersAndAlphabet(event) {
        var x = event.keyCode;
        if (x > 64 && x < 91 || x > 96 && x < 123 || x > 47 && x < 58 || x == 32) {
        } else {
            event.preventDefault();
        }
    }

        // only digit 

    function onlyDigit(event){
        var x = event.keyCode;
        if (x > 47 && x < 58 || x == 32) {
        } else {
            event.preventDefault();
        }
    }

    // only digit 



        function digitDot(event) {
            var x = event.keyCode;
            if (x > 47 && x < 58 || x == 32 || x == 08 || x == 46) {
            } else {
                event.preventDefault();
            }
        }

        $(document).ready(function (e) {
            $(".confirmButton").on("click", function(e) {
                var _currentId = $(".lpay-nav-item.active a").attr("data-id");
                
                $(".lpay_tabs_content[data-target='"+ _currentId +"'] .lpay-input").each(function() {
                    if($(this).prop("tagName") !== "DIV") {
                        var _currentId = $(this).attr("id");
                        if(_currentId == "ab-defaultbene") {
                            $(this).closest("[data-id='defaultbene']").addClass("d-none");
                            $(this).prop("checked", false);
                            $(this).closest("label").removeClass("lpay_toggle_on");
                        }
                        
                        $(this).val("");
                        if($(this).prop("tagName") == "SELECT") {
                            $(this).selectpicker("refresh");
                        }
                    }
                });

                var _merchant = $(".lpay_tabs_content[data-target='"+ _currentId +"'] .merchant-selectbox");

                if(_merchant.val() == "") {
                    var _dataName = _merchant.attr("data-name");
                    $("div[data-id='"+ _dataName +"']").addClass("d-none");
                }

                $(".lpay_popup").fadeOut();
            });


            var navigationHandler = function() {
                var _nodalTab = _id("nodal-report-navigation").querySelector("li");
                _nodalTab.classList.add("active");
                var _dataId = _nodalTab.querySelector("a").getAttribute("data-id");
                document.querySelector('[data-target="'+ _dataId +'"]').classList.remove("d-none");
            }

            navigationHandler();
            

            // var _channel = {
            //     vendorPayOutFlag : "Merchant Initiated - Indirect",
            //     merchantInitiatedDirectFlag : "Merchant Initiated - Direct",
            //     impsFlag : "PG Initiated"
            // };

            // var _channelOptions = "";

            // for(var _channelKey in _channel) {
            //     var isTrue = _id("hidden-" + _channelKey).value;

            //     if(isTrue == "true") {
            //         var _value = _channel[_channelKey].replace(" -", "");
            //         _channelOptions += '<option value="'+ _value +'">'+ _channel[_channelKey] +'</option>';                    
            //     }
            // }

            // $(_channelOptions).appendTo("#channel");
            // $("#channel").selectpicker("refresh");


                function getSubMerchant(_this, _url, _firstKey, _object) {
                    var _merchant = _this.target.value;
                    var _merchantId = _this.target.id;
                    var _key = _firstKey;
                    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
                    var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
                    if (_merchant != "") {
                        document.querySelector("body").classList.remove("loader--inactive");
                        var data = new FormData();
                        data.append('payId', _merchant);
                        var _xhr = new XMLHttpRequest();
                        _xhr.open('POST', _url, true);
                        _xhr.onload = function () {
                            if (_xhr.status === 200) {
                                var obj = JSON.parse(this.responseText);
                                var _option = "";
                                if (_object.isSuperMerchant == true) {
                                    if (obj.superMerchant == true) {
                                        $("#isSuperMerchant").val("Y");
                                        document.querySelector("#"+_subMerchantAttr).setAttribute("data-id", "lpay-input");
                                        document.querySelector("#" + _subMerchantAttr).innerHTML = "";
                                        if (_key !== null) {
                                            _option += document.querySelector("#" + _subMerchantAttr).innerHTML = "<option value=''>Select Sub Merchant</option>";
                                        } else {
                                            _option += document.querySelector("#" + _subMerchantAttr).innerHTML = "<option value='ALL'>ALL</option>";
                                        }
                                        for (var i = 0; i < obj.subMerchantList.length; i++) {
                                            _option += document.querySelector("#" + _subMerchantAttr).innerHTML += "<option value=" + obj.subMerchantList[i]["payId"] + ">" + obj.subMerchantList[i]["businessName"] + "</option>";
                                        }
                                        document.querySelector("[data-target=" + _subMerchantAttr + "]").classList.remove("d-none");
                                        if (_key !== null) {
                                            document.querySelector("#" + _subMerchantAttr + " option[value='']").selected = true;
                                        } else {
                                            document.querySelector("#" + _subMerchantAttr + " option[value='ALL']").selected = true;
                                        }
                                        $("#" + _subMerchantAttr).selectpicker();
                                        $("#" + _subMerchantAttr).selectpicker('refresh');
                                    } else {
                                        $("#isSuperMerchant").val("N");
                                        document.querySelector("#"+_subMerchantAttr).removeAttribute("data-id");
                                        document.querySelector("[data-target=" + _subMerchantAttr + "]").classList.add("d-none");
                                        document.querySelector("#" + _subMerchantAttr).value = "";
                                    }
                                }
                                if (_object.subUser == true) {
                                    if (obj.subUserList.length > 0) {
                                        _option += document.querySelector("#" + _subUserAttr).innerHTML = "<option value='ALL'>ALL</option>";
                                        for (var i = 0; i < obj.subUserList.length; i++) {
                                            _option += document.querySelector("#" + _subUserAttr).innerHTML += "<option value=" + obj.subUserList[i]["emailId"] + ">" + obj.subUserList[i]["businessName"] + "</option>";
                                        }
                                        document.querySelector("[data-target=" + _subUserAttr + "]").classList.remove("d-none");
                                        document.querySelector("#" + _subUserAttr + " option[value='ALL']").selected = true;
                                        $("#" + _subUserAttr).selectpicker();
                                        $("#" + _subUserAttr).selectpicker('refresh');
                                    } else {
                                        document.querySelector("[data-target=" + _subUserAttr + "]").classList.add("d-none");
                                        document.querySelector("#" + _subUserAttr).value = "";
                                    }
                                }
                                if (_object.glocal == true) {
                                    if (obj.glocalFlag == true) {
                                        document.querySelector("[data-id=deliveryStatus]").classList.remove("d-none");
                                        $("[data-id=deliveryStatus] select").selectpicker('val', 'All');
                                    } else {
                                        document.querySelector("[data-id=deliveryStatus]").classList.add("d-none");
                                    }
                                }

                                if (_object.retailMerchantFlag == true) {
                                    $("#retailMerchantFlag").val(data.retailMerchantFlag);
                                    document.querySelector("#retailMerchantFlag").value = data.retailMerchantFlag;
                                }
                            }
                        }
                        _xhr.send(data);
                        setTimeout(function (e) {
                            $("#isSuperMerchant").val("N");
                            document.querySelector("body").classList.add("loader--inactive");
                        }, 1000);
                    } else {
                        $("#isSuperMerchant").val("Y");
                        document.querySelector("[data-target=" + _subMerchantAttr + "]").classList.add("d-none");
                        document.querySelector("#" + _subMerchantAttr).value = "";

                    }
                }

                // document.querySelector("#merchant").addEventListener("change", function (e) {
                //     getSubMerchant(e, "getSubMerchantList", null, {
                //         isSuperMerchant: true,
                //     });
                // });

                // document.querySelector("#merchantReportPayId").addEventListener("change", function (e) {
                //     getSubMerchant(e, "getSubMerchantList", null, {
                //         isSuperMerchant: true,
                //     });
                // });

                // document.querySelector("#bulkAccountMerchant").addEventListener("change", function (e) {
                //     getSubMerchant(e, "getSubMerchantList", "select key", {
                //         isSuperMerchant: true,
                //     });
                // });
            })

            // reInitiate
            $("body").on("click", ".reInitiateIMPS", function(e){
                var _tr = $(this).closest("tr");
                $("body").removeClass("loader--inactive");
                var _data = {
                    "txnId" : _tr.find(".txnId").text(),
                    "orderId" : _tr.find(".orderid").text(),
                    "status" : _tr.find(".status").text()
                }
                $.ajax({
                    type :"post",
                    url : "impsReinitiateTransaction",
                    data: _data,
                    success: function(data){
                        if(data.response == "success"){
                            handleChangePayout();
                            $(".lpay_popup-innerbox").attr("data-status", "success");
                            $(".responseMsg").text(data.responseMsg);
                            $(".lpay_popup").fadeIn();
                            $("body").addClass("loader--inactive");
                        }else{
                            handleChangePayout();
                            $(".lpay_popup-innerbox").attr("data-status", "error");
                            $(".responseMsg").text(data.responseMsg);
                            $(".lpay_popup").fadeIn(); 
                            $("body").addClass("loader--inactive");
                        }
                    }
                })
            });

        //Merchant ReInitiate direct
        $("body").on("click", ".merchantReInitiateDirect", function(e){
            var _tr = $(this).closest("tr");
            $("body").removeClass("loader--inactive");
            var _data = {
                "txnId" : _tr.find(".txnId").text(),
                "orderId" : _tr.find(".orderid").text(),
                "status" : _tr.find(".status").text()
            }
            $.ajax({
                type :"post",
                url : "merchantReInitiateDirectTransaction",
                data: _data,
                success: function(data){
                    console.log(data.response);
                    console.log(data.responseMsg);
                    if(data.response == "success") {
                    	handleChangePayout();
                        $(".lpay_popup-innerbox").attr("data-status", "success");
                        $(".responseMsg").text(data.responseMsg);
                        $(".lpay_popup").fadeIn();
                        $("body").addClass("loader--inactive");
                    }else{
                    	handleChangePayout();
                        $(".lpay_popup-innerbox").attr("data-status", "error");
                        $(".responseMsg").text(data.responseMsg);
                        $(".lpay_popup").fadeIn(); 
                        $("body").addClass("loader--inactive");
                    }
                }
            })
        });

            // statusEnquiry
            $("body").on("click", ".statusEnquiryIMPS", function(e){
                var _tr = $(this).closest("tr");
                var _txnId = _tr.find(".txnId").text();
                $("body").removeClass("loader--inactive");
                $.ajax({
                    type: "POST",
                    url: "getImpsStatus",
                    data: {
                        "txnId": _txnId
                    },
                    success: function(data){
                        console.log(data);
                        if(data.response == "success"){
                            setTimeout(function(e) {
                                handleChangePayout();
                                $(".lpay_popup-innerbox").attr("data-status", "success");
                                $(".lpay_popup").fadeIn();
                                $(".responseMsg").text(data.responseMsg);
                                $("body").addClass("loader--inactive");
                            }, 2000);
                        } else {
                            handleChangePayout();
                            $(".lpay_popup-innerbox").attr("data-status", "error");
                            $(".responseMsg").text(data.responseMsg);
                            $(".lpay_popup").fadeIn();
                            $("body").addClass("loader--inactive");
                        }
                    }
                })
            })


    $(".downloadData").on("click", function(e){
                if($("[data-id=reportMerchant]").val() == ""){
                    $("#reportMerchant").val("All");
                }else{
                    $("#reportMerchant").val($("[data-id=reportMerchant]").val());
                }
                $("#reportStatus").val($("[data-id=reportStatus]").val());
                $("#reportChannel").val($("[data-id=reportChannel]").val());
                $("#reportDateFrom").val($("[data-id=reportDateFrom]").val());
                $("#reportDateTo").val($("[data-id=reportDateTo]").val());
                $("#reportSubMerchant").val($("#subMerchantReport").val());
                $("#downloadReport").submit();
            })

             // tab creation 
             $(".lpay-nav-link").on("click", function(e){
                var _this = $(this).attr("data-id");
                $(".lpay-nav-item").removeClass("active");
                $(this).closest(".lpay-nav-item").addClass("active");
                $(".lpay_tabs_content").addClass("d-none");
                $("[data-target="+_this+"]").removeClass("d-none");
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
                handleChangePayout();
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 1500);
            })
            
            handleChangePayout();

            function handleChangePayout(){
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
                        "url": "impsTransferReport",
                        "data" : function (d){
                                return generatePostDataPayout(d);
                            }
                        },
                        "initComplete" : function(settings, json) {
                            //console.log(json);
                            $("#setSuperMerchant").val(json.flag);
                            hideColumnPayout();
                        },

                        "destroy": true,
                        "order": [[ 3, 'desc' ]],
                        "aoColumns": [
                        {
                            "mDataProp": "merchant",
                            "className": "my_class"
                        },
                        {
                            "mData" : null,
                            "mRender" : function(row){
                                if(row.subMerchant != null){
                                    
                                    return row.subMerchant;
                                }else{
                                    return "<span>NA</span>"
                                }
                            }
                        },
                        {"mData" : "txnId", "className": "txnId"},
                        {"mData" : "merchantPayId"}, 
                        {"mData" : "orderId", "className": "orderId"},
                        {"mData" : "impsRefNum"}, 
                        {"mData" : "date"}, 
                        {"mData" : "systemSettlementDate", "className" : "settledDate"},
                        {"mData" : "userType"}, 
                        {"mData" : "phoneNo", "className" : "mobileNo"},
                        {"mData" : "bankAccountName", "className": "bankAccountName"}, 
                        {"mData" : "bankAccountNumber", "className" : "bankAccountNumber"},
                        {"mData" : "bankIFSC", "className" : "bankIFSC"},	
                        {"mData" : "amount" , "className": "amount"},

                        {"mData" : "responseMsg"},
                        {"mData" : "status", "className" : "status"},
                        {
                            "mData" : null,
                            "className" : "text-center no-wrap",
                            "mRender" : function(row) {
                                if(row.status == "Timeout" && row.userType == "Merchant Initiated Direct") {
                                    return "<button class='lpay_button lpay_button-md lpay_button-secondary statusEnquiryIMPS'>Get Status</button>"
                                } else if(row.status != "Captured" && row.userType == "Merchant Initiated Direct") {
                                    return "<button class='lpay_button lpay_button-md lpay_button-secondary merchantReInitiateDirect'>Re-Initiate</button>"
                                } else {
                                    return "NA";
                                }
                            }
                        }
                                
                    ]
                });

            } 

            // variable sent to backend function
            function generatePostDataPayout(d) {
                var obj = {
                    payId : $("#merchantReportPayId").val(),
                    status : $("#status").val(),
                    channel : $("#channel").val(),
                    subMerchant: $("#subMerchantReport").val(),
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



        </script>

        
    <script src="../js/cib-imps.js"></script>

</body>
</html>