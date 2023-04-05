<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
    <html>

    <head>

        <style>
            #refundReportDatatable_wrapper {
                overflow-y: auto;
                white-space: nowrap;
               
            }
            
            #mainTable {
                table-layout: fixed;
            }
            
            .heading {
                text-align: center;
                color: black;
                font-weight: bold;
                font-size: 22px;
            }
            
            .samefnew {
                width: 19.5% !important;
                float: left;
                font-weight: 600;
                font-size: 12px;
                color: #333;
                line-height: 22px;
                margin: 0 0 0 10px;
            }
            
            .cust {
                width: 20% !important;
                float: left;
                font: bold 13px arial !important;
                color: #333;
                line-height: 22px;
                margin: 0 0 0 0px !important;
            }
            
            .submit-button {
                width: 10% !important;
                height: 28px !important;
                margin-top: -4px !important;
            }
            
            .MerchBx {
                min-width: 99% !important;
                margin: 15px;
                margin-top: 25px !important;
                padding: 0;
            }
            
            table.dataTable thead .sorting {
                background: none !important;
            }
            
            .sorting_asc {
                background: none !important;
            }
            
            table.dataTable thead .sorting_desc {
                background: none !important;
            }
            
            table.dataTable thead .sorting {
                cursor: default !important;
            }
            
            table.dataTable thead .sorting_desc,
            table.dataTable thead .sorting {
                cursor: default !important;
            }
            
            table.dataTable.display tbody tr.odd {
                background-color: #e6e6ff !important;
            }
            
            table.dataTable.display tbody tr.odd>.sorting_1 {
                background-color: #e6e6ff !important;
            }
        </style>

        <title>Refund Report</title>
        <link rel="icon" href="../image/favicon-32x32.png">
        <link href="../css/jquery-ui.css" rel="stylesheet" />
        <link rel="stylesheet" href="../css/bootstrap-select.min.css">
        <script src="../js/jquery.min.js"></script>
        <script src="../js/jquery.dataTables.js"></script>
        <script src="../js/jquery-ui.js"></script>
        <script type="text/javascript" src="../js/daterangepicker.js"></script>
        <link href="../css/loader.css" rel="stylesheet" type="text/css" />
        <script type="text/javascript" src="../js/dataTables.buttons.js"></script>
        <script type="text/javascript" src="../js/pdfmake.js"></script>
        <link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
        <script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
        <script src="../js/common-scripts.js"></script>


        <script type="text/javascript">
        function hideColumn() {
			var _getMerchant = $("#setData").val();
			var td = $("#refundReportDatatable").DataTable();
			if(_getMerchant == "" || _getMerchant == "NA") {
				td.columns(5).visible(false);
			} else {
				td.columns(5).visible(true);
			}
        }
        
            $(document)
                .ready(
                    function() {
                        var _select = "<option value='ALL'>ALL</option>";
			$("[data-id='subMerchant']").find('option:eq(0)').before(_select);
			$("[data-id='subMerchant'] option[value='ALL']").attr("selected", "selected");

			$("#merchant").on("change", function(e) {
				var _merchant = $(this).val();
				if(_merchant != "") {
					$("body").removeClass("loader--inactive");
					$.ajax({
						type: "POST",
						url: "getSubMerchantList",
						data: {"payId": _merchant},
						success: function(data) {						
							$("#subMerchant").html("");
							if(data.superMerchant == true){
								var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
								for(var i = 0; i < data.subMerchantList.length; i++) {
									_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
								}

								$("[data-id='submerchant']").removeClass("d-none");
								$("#subMerchant option[value='']").attr("selected", "selected");
								$("#subMerchant").selectpicker();
                                $("#subMerchant").selectpicker("refresh");
								setTimeout(function(e){
									$("body").addClass("loader--inactive");
								},500);
							} else {
								setTimeout(function(e){
									$("body").addClass("loader--inactive");
								},500);

								$("[data-id='submerchant']").addClass("d-none");
								$("#subMerchant").val("");
							}
						}
					});
				}else{
					$("[data-id='submerchant']").addClass("d-none");
					$("#subMerchant").val("");	
				}
			});
                        
                        $(function() {
                            renderTable();
                        });


                        $("#submit").click(function(env) {
                            $("body").removeClass("loader--inactive");
                            $("#setData").val("");
                            reloadTable();
                        });
                        $(function() {
                            var datepick = $.datepicker;
                            var table = $('#refundReportDatatable').DataTable();
                            $('#refundReportDatatable tbody').on('click', 'td', function() {

                                popup(table, this);
                            });
                        });
                    });


            function renderTable() {
                var merchantEmailId = document.getElementById("merchant").value;
                var table = new $.fn.dataTable.Api('#refundReportDatatable');
                //to show new loader -Harpreet
                $("body").removeClass("loader--inactive");
                var transFrom = $.datepicker
                    .parseDate('dd-mm-yy', $('#dateFrom').val());
                var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
                if (transFrom == null || transTo == null) {
                    alert('Enter date value');
                    return false;
                }

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
                var token = document.getElementsByName("token")[0].value;
                $('#refundReportDatatable')
                    .dataTable({
                        //		dom : 'T<"clear">lfrtip',
                        "footerCallback": function(row, data, start, end,
                            display) {
                            var api = this.api(),
                                data;

                            // Remove the formatting to get integer data for summation
                            var intVal = function(i) {
                                return typeof i === 'string' ? i.replace(
                                        /[\,]/g, '') * 1 :
                                    typeof i === 'number' ? i : 0;
                            };

                            // Total over all pages
                            total = api.column(6).data().reduce(
                                function(a, b) {
                                    return intVal(a) + intVal(b);
                                }, 0);

                            // Total over this page
                            pageTotal = api.column(6, {
                                page: 'current'
                            }).data().reduce(function(a, b) {
                                return intVal(a) + intVal(b);
                            }, 0);

                            // Update footer
                            $(api.column(6).footer()).html(
                                '' + pageTotal.toFixed(2) + ' ' + ' ');

                            // Total over all pages
                            total = api.column(7).data().reduce(
                                function(a, b) {
                                    return intVal(a) + intVal(b);
                                }, 0);

                            // Total over this page refund
                            pageTotal = api.column(7, {
                                page: 'current'
                            }).data().reduce(function(a, b) {
                                return intVal(a) + intVal(b);
                            }, 0);

                            // Update footer
                            $(api.column(7).footer()).html(
                                '' + pageTotal.toFixed(2) + ' ' + ' ');

                            // Total over all pages
                            total = api.column(8).data().reduce(
                                function(a, b) {
                                    return intVal(a) + intVal(b);
                                }, 0);

                            // Total over this page
                            pageTotal = api.column(8, {
                                page: 'current'
                            }).data().reduce(function(a, b) {
                                return intVal(a) + intVal(b);
                            }, 0);

                            // Update footer
                            $(api.column(8).footer()).html(
                                '' + pageTotal.toFixed(2) + ' ' + ' ');

                            // Total over all pages
                            total = api.column(9).data().reduce(
                                function(a, b) {
                                    return intVal(a) + intVal(b);
                                }, 0);

                            // Total over this page
                            pageTotal = api.column(9, {
                                page: 'current'
                            }).data().reduce(function(a, b) {
                                return intVal(a) + intVal(b);
                            }, 0);

                            // Update footer
                            $(api.column(9).footer()).html(
                                '' + pageTotal.toFixed(2) + ' ' + ' ');
                        },

                        "columnDefs": [{
                            className: "dt-body-right",
                            "targets": [0, 1, 2, 3, 4, 5, 8, 9, 10, 11, 12,13]
                        }],
                        dom: 'BTftlpi',
                        buttons: [{
                            extend: 'copyHtml5',
                            //footer : true,
                            exportOptions: {
                                columns: [':visible']
                            }
                        }, {
                            extend: 'csvHtml5',
                            //footer : true,
                            title: 'Refund Report',
                            exportOptions: {
                                columns: [':visible']
                            }
                        }, {
                            extend: 'pdfHtml5',
                            //footer : true,
                            orientation: 'landscape',
                            title: 'Refund Report',
                            exportOptions: {
                                columns: [':visible']
                            }
                        }, {
                            extend: 'print',
                            //footer : true,
                            title: 'Refund Report',
                            exportOptions: {
                                columns: [':visible']
                            }
                        }, {
                            extend: 'colvis',
                            //           collectionLayout: 'fixed two-column',
                            columns: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,13]
                        }],
                        "ajax": {
                            "url": "refundReportAction",
                            "type": "POST",
                            "data": function(d) {
                                return generatePostData(d);
                            }
                        },
                        "fnDrawCallback": function() {
                            hideColumn();

                            $("#submit").removeAttr("disabled");
                            $("body").addClass("loader--inactive");
                            
                        },
                        "searching": false,
                        "ordering": false,
                        "processing": true,
                        "serverSide": true,
                        "paginationType": "full_numbers",
                        "lengthMenu": [
                            [10, 25, 50, -1],
                            [10, 25, 50, "All"]
                        ],
                        //"order" : [ [ 1, "desc" ] ],
                        "order": [],
                        "columnDefs": [{
                            "type": "html-num-fmt",
                            "targets": 4,
                            "orderable": false,
                            "targets": [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,13]
                        },
							{
						'targets': 0,
						'createdCell':  function (td, cellData, rowData, row, col) {
							console.log(rowData["subMerchatId"]);
							$("#setData").val(rowData["subMerchantId"]);
                        }
                            }
                        ],
                        "columns": [

                            {
                                "data": "origTxnId",
                                "width": '11%'
                            }, {
                                "data": "origTxnDate",
                                "width": '8%'
                            }, {
                                "data": "pgRefNum",
                            }, {
                                "data": "refundDate",
                                "width": '9%'
                            }, {
                                "data": "merchants",

                            },{
					            "data" : "subMerchantId",
						        "className" : "text-class"
					        }, {
                                "data": "orderId",

                            },

                            {
                                "data": "customerEmail",


                            },

                            {
                                "data": "paymentMethods"

                            }, {
                                "data": "currency"

                            }, {
                                "data": "origAmount",
                                "width": '7%',
                            },

                            {
                                "data": "refundAmount",
                                "width": '7%',
                            },

                            {
                                "data": "refundFlag",
                                "width": '7%',
                            }, {
                                "data": "refundStatus",
                                "width": '7%',
                            },

                        ]
                    });
                //TODO fix CSS
                /*$("#merchants").select2({
                	//data: payId
                	});*/
            }

            function reloadTable() {
                var datepick = $.datepicker;
                var transFrom = $.datepicker
                    .parseDate('dd-mm-yy', $('#dateFrom').val());
                var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
                if (transFrom == null || transTo == null) {
                    alert('Enter date value');
                    return false;
                }

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

                var tableObj = $('#refundReportDatatable');
                var table = tableObj.DataTable();
                table.ajax.reload();
            }

            function generatePostData(d) {


                var token = document.getElementsByName("token")[0].value;
                var merchantEmailId = document.getElementById("merchant").value;
                var aquirer = $("#aquirer").val();
                var _subMerchantEmailId = $("#subMerchant").val();
                var currency = document.getElementById("currency").value;
                var pgRefNum = document.getElementById("pgRefNum").value;
                if (merchant == '') {
                    merchant = 'ALL'
                }

                if (subMerchant == '') {
                    subMerchant = 'ALL'
                }

                if (currency == '') {
                    currency = 'ALL'
                }

                var obj = {
                    dateFrom: document.getElementById("dateFrom").value,
                    dateTo: document.getElementById("dateTo").value,
                    merchantEmailId: merchantEmailId,
                    subMerchantEmailId:  _subMerchantEmailId,
                    currency: currency,
                    aquirer: aquirer,
                    pgRefNum: pgRefNum,
                    draw: d.draw,
                    length: d.length,
                    start: d.start,
                    token: token,
                    "struts.token.name": "token",
                };

                return obj;
            }
        </script>

        <script>
            function checkRefNo() {
                var refValue = document.getElementById("pgRefNum").value;
                var refNoLength = refValue.length;
                if ((refNoLength < 16) && (refNoLength > 0)) {
                    document.getElementById("Submit").disabled = true;
                    document.getElementById("Submit").style.backgroundColor = "#b3e6b3";
                    document.getElementById("validRefNo").style.display = "block";
                } else if (refNoLength == 0) {
                    document.getElementById("Submit").disabled = false;
                    document.getElementById("Submit").style.backgroundColor = "#39ac39";
                    document.getElementById("validRefNo").style.display = "none";
                } else {
                    document.getElementById("Submit").disabled = false;
                    document.getElementById("Submit").style.backgroundColor = "#39ac39";
                    document.getElementById("validRefNo").style.display = "none";
                }
            }
        </script>

        <style type="text/css">
            .form-control {
                margin-left: 0 !important;
            }
            
            .samefnew {
                margin: 0 !important;
			}
			
			.mt-20{
				margin-top: 20px !important;
			}

			.MerchBx{
				margin-top: 0 !important;
			}

			#mainTable{
				margin-top: 0;
			}
            
          
        </style>

    </head>

    <body id="mainBody">
        <input type="hidden" id="setData">
        <section class="refund-report lapy_section white-bg box-shadow-box mt-70 p20">
            <div class="row">
                <div class="col-md-12">
                    <div class="heading_with_icon mb-30">
                        <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                        <h2 class="heading_text">Refund Report Filter</h2>
                    </div>
                    <!-- /.heading_icon -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-3 mb-20">
                  <div class="lpay_input_group">
                    <label for="">PF REF Num</label>
						<s:textfield id="pgRefNum" class="lpay_input" name="pgRefNum" type="text" value="" autocomplete="off" onkeypress="javascript:return isNumber (event)" maxlength="16" onblur="checkRefNo()"></s:textfield>
                  </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Select Merchant</label>
                       <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
                            <s:select name="merchant" class="selectpicker" id="merchant" headerKey="ALL" headerValue="ALL" list="merchantList" listKey="emailId" listValue="businessName" autocomplete="off" />
                        </s:if>
                        <s:else>
                            <s:select name="merchant" class="selectpicker" id="merchant" headerKey="ALL" headerValue="ALL" list="merchantList" listKey="emailId" listValue="businessName" autocomplete="off" />
                        </s:else>
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20" data-id="submerchant">
					<div class="lpay_select_group">
						<label for="">Sub Merchant</label>
						<s:select
							data-id="subMerchant"
							name="subMerchant"
							class="selectpicker"
							id="subMerchant"
							list="subMerchantList"
							listKey="emailId"
							data-live-search="true"
							listValue="businessName"
							autocomplete="off"
						/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->
			</s:if>
			<s:else>
				<div class="col-md-3 d-none mb-20" data-id="submerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchant" id="subMerchant"></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Acquirer</label>
						<s:select headerKey="ALL" data-live-search="true" headerValue="ALL" class="selectpicker" list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()" listValue="name" listKey="code" id="aquirer" name="aquirer" value="aquirer" />
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                    <div class="lpay_select_group">
                       <label for="">Currency</label>
					   <s:select name="currency" id="currency" headerValue="ALL" headerKey="ALL" list="currencyMap" class="selectpicker" />
                    </div>
                    <!-- /.lpay_select_group -->  
                </div>
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                  <div class="lpay_input_group">
                    <label for="">Date From</label>
					<s:textfield type="text" id="dateFrom" name="dateFrom" class="lpay_input" autocomplete="off" readonly="true" />
                  </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-3 mb-20">
                  <div class="lpay_input_group">
                    <label for="">Date To</label>
					<s:textfield type="text" id="dateTo" name="dateTo" class="lpay_input" autocomplete="off" readonly="true" />
                  </div>
                  <!-- /.lpay_input_group -->
                </div>
                <!-- /.col-md-3 mb-20 -->
                <div class="col-md-12 text-center">
                    <button class="lpay_button lpay_button-md lpay_button-secondary" id="submit">Submit</button>
                </div>
                <!-- /.col-md-12 -->
            </div>
            <!-- /.row -->
        </section>
        <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

        <section class="refund-report lapy_section white-bg box-shadow-box mt-70 p20">
            <div class="row">
                <div class="col-md-12">
                    <div class="heading_with_icon mb-30">
                        <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                        <h2 class="heading_text">Refund Report Data</h2>
                    </div>
                    <!-- /.heading_icon -->
                </div>
                <!-- /.col-md-12 -->
                <div class="col-md-12">
                    <div class="lpay_table">
                        <table id="refundReportDatatable" align="center" class="display" cellspacing="0" width="100%">
                            <thead class="lpay_table_head">
                                <tr>
                                    <th style="text-align:left;" data-orderable="false">Original Txn Id</th>
                                    <th style="text-align:left;" data-orderable="false">Original Txn Date</th>
                                    <th style="text-align:left;" data-orderable="false">PG Ref Num</th>
                                    <th style="text-align:left;" data-orderable="false">Refund Date</th>
                                    <th style="text-align:left;" data-orderable="false">Merchant Name</th>
                                    <th style="text-align:left;" data-orderable="false">Sub Merchant</th>
                                    <th style="text-align:left;" data-orderable="false">Order Id</th>
                                    <th style="text-align:left;" data-orderable="false">Customer Email</th>
                                    <th style="text-align:left;" data-orderable="false">Payment Method</th>
                                    <th style="text-align:left;" data-orderable="false">Currency</th>
                                    <th style="text-align:left;" data-orderable="false">Original Amount</th>
                                    <th style="text-align:left;" data-orderable="false">Refund Amount</th>
                                    <th style="text-align:left;" data-orderable="false">Refund Flag</th>
                                    <th style="text-align:left;" data-orderable="false">Refund Status</th>
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
    </body>

    </html>