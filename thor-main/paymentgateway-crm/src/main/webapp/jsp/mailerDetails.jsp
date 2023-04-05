<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Mailer</title>
    <link rel="stylesheet" href="../css/jquery-ui.css">
    <link rel="stylesheet" href="../css/bootstrap-datetimepicker.css">
    <script src="../js/jquery-latest.min.js"></script>
    <script src="../js/jquery-ui.js"></script>
    <script src="../js/daterangepicker.js" type="text/javascript"></script>
    <script src="../js/jquery.dataTables.js"></script>
    <script type="text/javascript" src="../js/dataTables.buttons.js"></script>
    <script type="text/javascript" src="../js/pdfmake.js"></script>
    <link rel="stylesheet" href="../css/bootstrap-select.min.css">
    <link rel="stylesheet" href="../css/bootstrap-flex.css">
    <script src="../js/bootstrap-select.min.js"></script>
    <!--<script type="text/javascript" src="../js/bootstrap-datetimepicker.js"></script>-->
	
	<script src="../js/city_state.js" type="text/javascript"></script>
	<script type="text/javascript" src="../js/moment-with-locales.js"></script>
	<!-- <script type="text/javascript" src="../js/bootstrap.min.js"></script> -->
    <script type="text/javascript" src="../js/bootstrap-datetimepicker.js"></script>

    <style>
        #actionMsg .action-msg {
            border-radius: 5px;
            -webkit-border-radius: 5px;
            -moz-border-radius: 5px;
            -ms-border-radius: 5px;
            -o-border-radius: 5px;
            pointer-events: none;
            text-align: center;
        }
        #actionMsg .success-text span {
            background-color: #b1deb1 !important;
            color: green !important;
        }
        #actionMsg .error-text span {
            background-color: #fbe9eb !important;
            color: #e34c5e !important;
        }
        @media (min-width: 768px) {
            .col-md-2_4 {
                width: 20% !important;
                float: left;
                position: relative;
                min-height: 1px;
                padding-right: 15px;
                padding-left: 15px;
            }
            .pd-field {
                transition: all ease .5s;
                min-width: 20% !important;
            }
        }
    </style>
</head>
<body class="bodyColor">
    <div class="edit-permission"><s:property value="%{editingpermission}"/></div>
    <!-- /.edit-permission -->

    <section class="imps-transferred lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Mailer Details</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->

            
            <div class="col-md-12">
                <s:if test="hasActionMessages()">
                    <div id="actionMsg" class="mb-20">
                        <s:actionmessage theme="simple" class="action-msg success-text" />
                    </div>
                </s:if>
                <s:if test="hasActionErrors()">
                    <div id="actionMsg" class="mb-20">
                        <s:actionerror theme="simple" class="action-msg error-text" />
                    </div>
                </s:if>
            </div>            
            <!-- /.col-md-12 -->

            <div class="col-md-12 mb-20">
                <ul class="lpay_tabs d-flex">
                    <li class="lpay-nav-item active"><a href="#" class="lpay-nav-link" data-id="maintenanceActivity">Maintenance Activity</a></li>
                    <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="settlementDelay">Settlement Delay </a></li>
                    <li class="lpay-nav-item"><a href="#" class="lpay-nav-link" data-id="paymentTypeDown">Payment Option Down</a></li>
                </ul>
                <!-- /.lpay_tabs -->
            </div>
            <!-- /.col-md-12 -->

            <div class="lpay_tabs_content w-100" data-target="maintenanceActivity">
                <form id="maintenanceMailerAction" name="maintenanceMailerAction" action="maintenanceMailerAction" method="post">
                    <s:hidden name="mailType" value="maintenanceActivity" id="maintenanceActivityMailType"></s:hidden>
                    
                    <div class="col-md-4 mb-20">
                        <div class="lpay_select_group">
                            <label for="">User Type <span class="text-danger">*</span></label>
                            <s:select
                                class="selectpicker"
                                id="maintenanceActivityUserType"
                                data-live-search="true"
                                headerKey="ALL"
                                list="#{'ADMIN':'Admin','SUBADMIN':'Sub-Admin','MERCHANT':'Merchant','SUBUSER':'Sub-User','SUPERMERCHANT':'Super-Merchant','SUBMERCHANT':'Sub-Merchant','RESELLER':'Reseller'}"
                                autocomplete="off"
                                multiple="true"
                                title="ALL"
                            />
                        </div>
                        <!-- /.lpay_select_group -->
                        <s:textfield type="hidden" name="userType" id="userType-MA" />
                    </div>
                    <!-- /.col-md-4 mb-20 -->
                
                    <div class="col-md-4 mb-20">
                        <div class="lpay_input_group">
                            <label for="dateForMaintenance">Date From <span class="text-danger">*</span></label>
                            <s:textfield
                                type="text"
                                class="textFL_merch lpay_input invoice-input"
                                id="dateForMaintenance"
                                name="dateFrom"
                                autocomplete="off"
                                readonly="true"
                                onkeyup="FieldValidator.valdExpDayAndHour(false)"
                                onkeypress="return isOnlyNumberKey(this,event)"
                            />
                            <span id="expiresDayErr" class="error"></span>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>

                    <div class="col-md-4 mb-20">
                        <div class="lpay_input_group">
                            <label for="dateToMaintenance">Date To <span class="text-danger">*</span></label>
                            <s:textfield
                                type="text"
                                class="textFL_merch lpay_input invoice-input"
                                id="dateToMaintenance"
                                name="dateTo"
                                autocomplete="off"
                                readonly="true"
                                onkeyup="FieldValidator.valdExpDayAndHour(false)"
                                onkeypress="return isOnlyNumberKey(this,event)"
                            />
                            <span id="expiresDayErr" class="error"></span>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>
                    <!-- /.col-md-4 -->

                    <div class="col-md-12 text-center">
                        <!--<button class="lpay_button lpay_button-md lpay_button-secondary" id="submit">Submit</button>-->
                        <button class="lpay_button lpay_button-md lpay_button-secondary" id="btn-submit-MA">Send</button>
                    </div>
                    <!-- /.col-md-12 -->
                </form>
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content w-100 d-none" data-target="settlementDelay">
                <form id="settlementDelay" name="settlementDelay" action="maintenanceMailerAction" method="post">
                    <s:hidden name="mailType" value="settlementDelay" id="settlementDelayMailType"></s:hidden>
                    <div class="col-md-3 mb-20">
                        <div class="lpay_select_group">
                            <label for="">User Type <span class="text-danger">*</span></label>
                            <s:select
                                class="selectpicker"
                                id="settlementDelayUserType"
                                data-live-search="true"
                                headerKey="ALL"
                                list="#{'ADMIN':'admin','SUBADMIN':'subAdmin','MERCHANT':'Merchant','SUBUSER':'subUser','SUPERMERCHANT':'superMerchant','SUBMERCHANT':'subMerchant','RESELLER':'reseller'}"
                                autocomplete="off"
                                multiple="true"
                                title="ALL"
                            />
                        </div>
                        <!-- /.lpay_select_group -->
                        <s:textfield type="hidden" name="userType" id="userType-SD" />
                    </div>
                    <!-- /.col-md-3 mb-20 -->
                
                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                            <label for="dateForSettlement">Date From <span class="text-danger">*</span></label>
                            <s:textfield
                                type="text"
                                class="textFL_merch lpay_input invoice-input"
                                id="dateForSettlement"
                                name="dateFrom"
                                autocomplete="off"
                                readonly="true"
                                onkeyup="FieldValidator.valdExpDayAndHour(false)"
                                onkeypress="return isOnlyNumberKey(this,event)"
                            />
                            <span id="expiresDayErr" class="error"></span>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>

                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                            <label for="dateToSettlement">Date To <span class="text-danger">*</span></label>
                            <s:textfield
                                type="text"
                                class="textFL_merch lpay_input invoice-input"
                                id="dateToSettlement"
                                name="dateTo"
                                autocomplete="off"
                                readonly="true"
                                onkeyup="FieldValidator.valdExpDayAndHour(false)"
                                onkeypress="return isOnlyNumberKey(this,event)"
                            />
                            <span id="expiresDayErr" class="error"></span>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>

                    <div class="col-md-3 mb-20">
                        <div class="lpay_input_group">
                            <label for="settlementDate">Settlement Date</label>
                            <s:textfield
                                type="text"
                                class="textFL_merch lpay_input invoice-input"
                                id="settlementDate"
                                name="settlementDate"
                                autocomplete="off"
                                placeholder="Please select date and time"
                                readonly="true"
                                onkeyup="FieldValidator.valdExpDayAndHour(false)"
                                onkeypress="return isOnlyNumberKey(this,event)"
                            />
                            <span id="expiresDayErr" class="error"></span>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>

                    <div class="col-md-12 mb-20 text-center">
                        <button class="lpay_button lpay_button-md lpay_button-secondary" id="btn-submit-SD">Send</button>
                        <!--<button class="lpay_button lpay_button-md lpay_button-primary downloadData">Download</button>-->
                    </div>
                    <!-- /.col-md-12 -->
                </form>
            </div>
            <!-- /.lpay_tabs_content -->

            <div class="lpay_tabs_content w-100 d-none" data-target="paymentTypeDown">
                <form id="paymentTypeDown" name="paymentTypeDown" action="maintenanceMailerAction" method="post">
                    <s:hidden name="mailType" value="paymentTypeDown" id="paymentTypeDownMailType"></s:hidden>
                    <div class="col-md-2_4 mb-20 pd-field">
                        <div class="lpay_select_group">
                            <label for="">User Type <span class="text-danger">*</span></label>
                            <s:select
                                headerKey="ALL"
                                class="selectpicker"
                                data-live-search="true"
                                list="#{'ADMIN':'admin','SUBADMIN':'subAdmin','MERCHANT':'Merchant','SUBUSER':'subUser','SUPERMERCHANT':'superMerchant','SUBMERCHANT':'subMerchant','RESELLER':'reseller'}"
                                id="paymentTypeDownUserType"
                                autocomplete="off"
                                multiple="true"
                                title="ALL"
                            />
                        </div>
                        <!-- /.lpay_select_group -->

                        <s:textfield type="hidden" name="userType" id="userType-PD" />
                    </div>
                    <!-- /.col-md-4 mb-20 -->

                    <div class="col-md-2_4 mb-20 pd-field">
                        <div class="lpay_select_group d-flex flex-column">
                            <label for="dateRequired" class="mb-10">Is date required?</label>
                            <label class="lpay_toggle lpay_toggle_on">
                                <input type="checkbox" id="dateRequired" data-toggle="toggle" checked="true" />
                                <input type="hidden" name="isDateRequiredFlag" id="input-dateRequired" value="true">
                            </label>
                        </div>
                    </div>
                
                    <div class="col-md-2_4 mb-20 date-field pd-field">
                        <div class="lpay_input_group">
                            <label for="dateFrom">Date From <span class="text-danger">*</span></label>
                            <s:textfield
                                type="text"
                                class="textFL_merch lpay_input invoice-input"
                                id="dateForPaymentType"
                                name="dateFrom"
                                autocomplete="off"
                                readonly="true"
                                onkeyup="FieldValidator.valdExpDayAndHour(false)"
                                onkeypress="return isOnlyNumberKey(this,event)"
                            />
                            <span id="expiresDayErr" class="error"></span>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>

                    <div class="col-md-2_4 mb-20 date-field pd-field">
                        <div class="lpay_input_group">
                            <label for="dateTo">Date To <span class="text-danger">*</span></label>
                            <s:textfield
                                type="text"
                                class="textFL_merch lpay_input invoice-input"
                                id="dateToPaymentType"
                                name="dateTo"
                                autocomplete="off"
                                readonly="true"
                                onkeyup="FieldValidator.valdExpDayAndHour(false)"
                                onkeypress="return isOnlyNumberKey(this,event)"
                            />
                            <span id="expiresDayErr" class="error"></span>
                        </div>
                        <!-- /.lpay_input_group -->
                    </div>

                    <div class="col-md-2_4 mb-20 pd-field">
                        <div class="lpay_select_group">
                            <label for="">Select Payment Type <span class="text-danger">*</span></label>
                            <s:select
                                headerKey="ALL"
                                data-live-search="true"
                                class="selectpicker"
                                list="#{'Credit Card':'Credit Card','Debit Card':'Debit Card','Net Banking':'Net Banking','Wallet':'Wallet','UPI':'UPI','EMI':'EMI','COD':'COD','EMI CC':'EMI CC','EMI DC':'EMI DC','UPI QR':'UPI QR'}"
                                id="paymentType"
                                autocomplete="off"
                                multiple="true"
                                title="ALL"
                            />
                        </div>
                        <!-- /.lpay_select_group -->
                        <s:textfield type="hidden" name="paymentType" id="paymentType-PD" />
                    </div>                 
                    <!-- /.col-md-4 -->

                    <div class="col-md-12 text-center">
                        <!--<button class="lpay_button lpay_button-md lpay_button-secondary" id="submit">Submit</button>-->
                        <button class="lpay_button lpay_button-md lpay_button-secondary" id="btn-submit-PD">Send</button>
                    </div>
                    <!-- /.col-md-12 -->
                </form>
            </div>
            <!-- /.lpay_tabs_content -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<s:hidden name="token" value="%{#session.customToken}" />

    <script type="text/javascript">
        var _today = new Date();

        var setCurrentDate = function(eleId) {
            var _getDate = _today.getDate();
            var _getMonth = _today.getMonth() + 1;
            var _getYear = _today.getFullYear();
        
            if(_getDate < 10) {
                _getDate = '0' + _getDate;
            }
        
            if(_getMonth < 10) {
                _getMonth = '0' + _getMonth;
            }
        
            var _fullDate = _getDate + '-' + _getMonth + '-' + _getYear + ' 23:59';
            
            $(eleId).val(_fullDate);
        }

        $('#dateForMaintenance').datetimepicker({
            format: 'DD-MM-YYYY HH:mm', // HH = 00 01 ... 22 23
            showClose: true,            
            ignoreReadonly: true
        });

        $('#dateToMaintenance').datetimepicker({
            format: 'DD-MM-YYYY HH:mm', // HH = 00 01 ... 22 23
            showClose: true,            
            ignoreReadonly: true
        });

        $('#dateForSettlement').datetimepicker({
            format: 'DD-MM-YYYY HH:mm', // HH = 00 01 ... 22 23
            showClose: true,            
            ignoreReadonly: true
        });

        $('#dateToSettlement').datetimepicker({
            format: 'DD-MM-YYYY HH:mm', // HH = 00 01 ... 22 23
            showClose: true,            
            ignoreReadonly: true
        });

        $('#settlementDate').datetimepicker({
            format: 'DD-MM-YYYY HH:mm', // HH = 00 01 ... 22 23
            showClose: true,
            minDate: _today,
            ignoreReadonly: true,
            showClear: true
        });

        $('#dateForPaymentType').datetimepicker({
            format: 'DD-MM-YYYY HH:mm', // HH = 00 01 ... 22 23
            showClose: true,
            ignoreReadonly: true
        });

        $('#dateToPaymentType').datetimepicker({
            format: 'DD-MM-YYYY HH:mm', // HH = 00 01 ... 22 23
            showClose: true,            
            ignoreReadonly: true
        });

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

        $(document).ready(function(e) {            
            // tab creation 
            $(".lpay-nav-link").on("click", function(e){
                var _this = $(this).attr("data-id");
                $(".lpay-nav-item").removeClass("active");
                $(this).closest(".lpay-nav-item").addClass("active");
                $(".lpay_tabs_content").addClass("d-none");
                $("[data-target="+_this+"]").removeClass("d-none");
            });

            var splitDate = function(_date) {
                var $date = _date.slice(0, _date.length - 6);
                var $time = _date.slice(_date.length - 5);
                
                $time = $time.split(":");

                $date = $date.split("-");
                $date = new Date(Number($date[2]), Number($date[1]) - 1, Number($date[0]), Number($time[0]), Number($time[1]));
                return $date.getTime();
            }

            var _dateValidate = function(obj) {
                if(splitDate(obj.dateFrom.val()) > splitDate(obj.dateTo.val())) {
                    alert("Date To must be after Date From.");
                    
                    return false;
                } else if(obj.validateMonth) {
                    var oneDay = 24 * 60 * 60 * 1000;
                    var timeInterval = splitDate(obj.dateTo.val()) - splitDate(obj.dateFrom.val());
                    var dayCount = timeInterval / oneDay + 1;

                    if(dayCount > 31) {
                        // error_snackbar.innerHTML = "Please limit the date range to 1 month.";
                        // showSnackbar("error-snackbar");

                        alert("Please limit the date range to 1 month.");
                        return false;
                    }
                }

                return true;
            }

            $("#btn-submit-MA").on("click", function(e) {
                e.preventDefault();

                var userType = $("#maintenanceActivityUserType").val();

                if(userType == "") {
                    userType = "ALL";
                } else {
                    userType = userType.join(",");
                }

                $("#userType-MA").val(userType);

                var dateFrom    = $("#dateForMaintenance"),
                    dateTo      = $("#dateToMaintenance");

                if(dateFrom.value !== "" && dateTo.value !== "") {
                    var _result = _dateValidate({
                        dateFrom : dateFrom,
                        dateTo : dateTo,
                        validateMonth : true
                    });

                    if(_result) {
                        $("body").removeClass("loader--inactive");
                        $("#maintenanceMailerAction").submit();
                    }
                }
            });

            $("#btn-submit-SD").on("click", function(e) {
                e.preventDefault();

                var userType = $("#settlementDelayUserType").val();

                if(userType == "") {
                    userType = "ALL";
                } else {
                    userType = userType.join(",");
                }

                $("#userType-SD").val(userType);

                var dateFrom    = $("#dateForSettlement"),
                    dateTo      = $("#dateToSettlement");

                if(dateFrom.value !== "" && dateTo.value !== "") {
                    var _result = _dateValidate({
                        dateFrom : dateFrom,
                        dateTo : dateTo,
                        validateMonth : true
                    });

                    if(_result) {
                        var _settlementDate = $("#settlementDate");
                        if(_settlementDate.val() !== "") {
                            if(splitDate(dateTo.val()) > splitDate(_settlementDate.val())) {
                                alert("Settlement date must be after Date to.");
                                
                                return false;
                            }
                        }
                        
                        $("body").removeClass("loader--inactive");
                        $("#settlementDelay").submit();
                    }
                }
            });

            $("#btn-submit-PD").on("click", function(e) {
                e.preventDefault();

                var userType = $("#paymentTypeDownUserType").val();

                if(userType == "") {
                    userType = "ALL";
                } else {
                    userType = userType.join(",");
                }

                $("#userType-PD").val(userType);

                var dateFrom    = $("#dateForPaymentType"),
                    dateTo      = $("#dateToPaymentType");

                if(dateFrom.value !== "" && dateTo.value !== "") {
                    var _result = _dateValidate({
                        dateFrom : dateFrom,
                        dateTo : dateTo,
                        validateMonth : true
                    });

                    var paymentType = $("#paymentType").val();

                    if(paymentType == "") {
                        var listArr = [], count = 0;
                        $("#paymentType").find("option").each(function() {
                            listArr[count] = $(this).val();
                            count++;
                        });
                        paymentType = listArr.join(",");
                    } else {
                        paymentType = paymentType.join(",");
                    }

                    $("#paymentType-PD").val(paymentType);

                    if(_result) {
                        $("body").removeClass("loader--inactive");
                        $("#paymentTypeDown").submit();
                    }
                }
            });

            var requiredDateHandler = function(status) {
                if(status) {
                    $(".date-field").removeClass("d-none");
                    $(".pd-field").removeClass("col-md-4");
                    $(".pd-field").addClass("col-md-2_4");
                    $("#input-dateRequired").val(status);
                } else {
                    $(".date-field").addClass("d-none");
                    $(".pd-field").addClass("col-md-4");
                    $(".pd-field").removeClass("col-md-2_4");
                    $("#input-dateRequired").val(status);
                }
            }

            $(".lpay_toggle").on("change", function(e) {
                var _getChecked = $(this).find("input[type='checkbox']").is(":checked"),
                    _label = $(this).closest("label");

                if(!_getChecked) {
                    _label.removeClass("lpay_toggle_on");
                    requiredDateHandler(false);
                } else {
                    _label.addClass("lpay_toggle_on");
                    requiredDateHandler(true);
                }
            });
        });

        // ACTION MESSAGE
        $(window).on("load", function() {
            setTimeout(function() {
                $("#actionMsg").addClass("d-none");
            }, 6000);

            if($("#dateForMaintenance").val() == "") {
                setCurrentDate("#dateForMaintenance");
            }

            if($("#dateToMaintenance").val() == "") {
                setCurrentDate("#dateToMaintenance");
            }

            if($("#dateForSettlement").val() == "") {
                setCurrentDate("#dateForSettlement");
            }

            if($("#dateToSettlement").val() == "") {
                setCurrentDate("#dateToSettlement");
            }

            if($("#dateForPaymentType").val() == "") {
                setCurrentDate("#dateForPaymentType");
            }

            if($("#dateToPaymentType").val() == "") {
                setCurrentDate("#dateToPaymentType");
            }
        });
    </script>

    <script src="../js/FieldValidator.js"></script>
</body>
</html>