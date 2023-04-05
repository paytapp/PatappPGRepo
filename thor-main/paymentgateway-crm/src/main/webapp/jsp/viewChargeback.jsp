<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>View Chargeback</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>  
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<script src="../js/bootstrap-select.min.js" type="text/javascript"></script>
	<script src="../js/common-scripts.js"></script>

	<style>
		#chargebackDataTable tr td { cursor: pointer; }

		.lpay_table .dataTables_filter{
			display: block !important;
		}

		td.case_id span {
			color: #0040ff !important;
			text-decoration: none !important;
			cursor: pointer;
		}
	</style>

	<script type="text/javascript">
		function hideColumn() {
			var _getUserType = $("#userType").val();
			
			var _table = new $.fn.dataTable.Api('#chargebackDataTable');

			if(_getUserType == "ADMIN" || _getUserType == "SUBADMIN") {				
				_table.column(10).visible(true);
			} else {				
				_table.column(10).visible(false);
			}
		}		

		$(document).ready(function() {
			function handleChange() {
				reloadTable();
			}

			document.querySelector("#viewChargeback").addEventListener("click", function(e){
				var _merchant = document.querySelector("#merchant").value;
				if(_merchant != "") {
					handleChange();
				} else {
					alert("Please Select Merchant");
				}
			});

			// $("#dateFrom").datepicker({
			// 	prevText : "click for previous months",
			// 	nextText : "click for next months",
			// 	showOtherMonths : true,
			// 	dateFormat : 'dd-mm-yy',
			// 	selectOtherMonths : false,
			// 	maxDate : new Date()
			// });

			// $("#dateTo").datepicker({
			// 	prevText : "click for previous months",
			// 	nextText : "click for next months",
			// 	showOtherMonths : true,
			// 	dateFormat : 'dd-mm-yy',
			// 	selectOtherMonths : false,
			// 	maxDate : new Date()
			// });
			
			// var today = new Date();
			// $('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
			// $('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
			renderTable();
			
			var datepick = $.datepicker;
			var table = $('#chargebackDataTable').DataTable();
			
			$('#chargebackDataTable tbody').on('click', '.my_class', function() {
				$("body").removeClass("loader--inactive");
				submitForm(table, this);
			});

			$("body").on("click", ".close-chargeback", function() {
				var isClose = confirm("Are you sure to close this chargeback?");

				if(isClose) {
					var caseId = $(this).closest("tr").find(".case_id").text();
					$("body").removeClass("loader--inactive");
	
					$.ajax({
						type : "post",
						url : "updateClosedStatus",
						data: {
							"caseId" : caseId,
							"dateFrom" : $("#dateFrom").val(),
							"dateTo" : $("#dateTo").val()
						},
						success: function(data) {
							setTimeout(function() {
								reloadTable();
								$("body").addClass("loader--inactive");
							}, 1000);
						},
						error: function() {
							alert("Something went wrong!");
						}
					});
				}
			});
		});
		
		function submitForm(table, index) {
			var rowIndex = table.cell(index).index().row;
			var columnIndex = table.cell(index).index().column;
			var rowData = table.row(rowIndex).data();
			var capturedAmount=rowData.capturedAmount;
			var pgRefNum = rowData.pgRefNum;
			var orderId = rowData.orderId;
			
			document.getElementById("pgRefNum").value = pgRefNum;
			document.getElementById("orderId").value = orderId;
			document.getElementById("capturedAmount").value=capturedAmount;

			document.getElementById('targetDate').value = rowData.targetDate;
			document.getElementById('caseId').value = rowData.caseId;
			
			document.viewChargebackDetails.submit();
		}
		
		function renderTable() {
			var table = new $.fn.dataTable.Api('#chargebackDataTable');
			$.ajaxSetup({
				global : false,
				beforeSend : function() {
					toggleAjaxLoader();
				},
				complete : function() {
					toggleAjaxLoader();
				}
			});

			var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
			var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
			if (transFrom == null || transTo == null) {
				alert('Enter date value');
				return false;
			}

			if (transFrom > transTo) {
				alert('From date must be before the to date');
				$('#dateFrom').focus();
				return false;
			}

			if (transTo - transFrom > 31 * 86400000) {
				alert('No. of days can not be more than 31');
				$('#dateFrom').focus();
				return false;
			}
			var token = document.getElementsByName("token")[0].value;

			$('#chargebackDataTable').DataTable({				
				language: {
					search: "",
					searchPlaceholder: "Search records"
				},
				dom : 'BTftlpi',
				buttons : [ {
					extend : 'copyHtml5',
					exportOptions : {
						columns : [ ':visible' ]
					}
				}, {
					extend : 'csvHtml5',
					title : 'Chargeback',
					exportOptions : {
						columns : [ ':visible' ]
					}
				}, {
					extend : 'pdfHtml5',
					title : 'Chargeback',
					exportOptions : {
						columns : [ ':visible' ]
					}
				}, {
					extend : 'print',
					title : 'Chargeback',
					exportOptions : {
						columns : [':visible']
					}
				},{
					extend : 'colvis',
					//           collectionLayout: 'fixed two-column',
					columns : [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
				} ],
				"ajax" : {
					type : "POST",
					url : "viewChargebackAction",
					data :function (d) {
						return generatePostData(d);
					}
				},
				"fnDrawCallback" : function(settings, json) {
					hideColumn();
					$("body").addClass("loader--inactive");
				},
				"processing": false,
				"paginationType": "full_numbers", 
				"lengthMenu": [[10, 25, 50, -1], [10, 25, 50, "All"]],
				"order" : [ [ 1, "desc" ] ], 
				"columns" : [
				{
					"data" : null,
					"className" : "my_class case_id",
					"mRender" : function(data, type, row) {
						return '<span>' + row.caseId + '</span>';
					}				
				}, {
					"data" : "orderId",
					"width" : '14%'
				},{
					"data" : "superMerchantName"
				},{
					"data" : "businessName",
					"width" : '13%'
				}, {
					"data" : "chargebackType",
					"width" : '13%'
				}, {
					"data" : "status",
					"width" : '10%'
				},{
					"data" : "capturedAmount",
				},
				// {
				// 	"data" : null,
				// 	"mRender" : function (data, type, row) {
				// 		var targetDate = row.targetDate;
				// 		targetDate = targetDate.split("-");
				// 		return targetDate[2] + "-" + targetDate[1] + "-" + targetDate[0] + " 23:59:59";
				// 	}
				// },
				{
					"data" : "targetDate"
				},
				{
					"data" : "createDateString"
				},
				// {
				// 	"data" : null,
				// 	"mRender" : function(data, type, row) {
				// 		var _createDate = row.createDate;
				// 		_createDate = _createDate.replace("T", " ");

				// 		return _createDate;
				// 	}
				// },
				{
					"data" : "closeDate"
				}, {
					"data" : null,
					"mRender": function(data, type, row, meta) {
						var isDisabled = "",
							btnTxt = "Close",
							isVisible = "d-none";
						if((row.status == "Accepted" || row.status == "Rejected" || row.status == "Refunded") && row.closeButtonFlag == false) {
							isVisible = "d-block";
						}

						return '<button class="'+ isVisible +' close-chargeback lpay_button lpay_button-md lpay_button-secondary">Close</button>'
					},
				}				
			]
			});
		}

		function generatePostData(d) {			
			var token = document.getElementsByName("token")[0].value;
			var obj = {
				payId : document.getElementById("merchant").value,
				chargebackType : document.getElementById("chargebackType").value,
				chargebackStatus : document.getElementById("chargebackStatus").value,
				dateTo : document.getElementById("dateTo").value,
				dateFrom : document.getElementById("dateFrom").value,
				subMerchantPayId : document.getElementById('subMerchant').value,
				orderId : document.querySelector("#orderId").value,
				token : token,
				"struts.token.name" : "token",
			};

			return obj;
		}
		
		function reloadTable() {
			var datepick = $.datepicker;
			var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
			var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
			if (transFrom == null || transTo == null) {
				alert('Enter date value');
				return false;
			}

			if (transFrom > transTo) {
				alert('From date must be before the to date');
				$('#dateFrom').focus();
				return false;
			}
			if (transTo - transFrom > 31 * 86400000) {
				alert('No. of days can not be more than 31');
				$('#dateFrom').focus();
				return false;
			}

			var tableObj = $('#chargebackDataTable');
			var table = tableObj.DataTable();
			
			table.ajax.reload();
		}

		

	</script>
</head>
<body>
	<s:hidden id="userType" value="%{#session.USER.UserType.name()}"></s:hidden>
	<section class="view-chargeback lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">View Chargeback Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
						<label for="">Select Merchant</label>
						<s:select
							name="merchant"
							class="selectpicker"
							id="merchant"
							headerKey="ALL"
							data-var="merchantPayId"
							data-id="subMerchant"
							data-live-search="true"
							headerValue="ALL"
							list="merchantList"
							listKey="payId"
							listValue="businessName"
							autocomplete="off"
						/>
					</s:if>
					<s:else>
						<label for="">Select Merchant</label>
						<s:select
							name="merchant"
							class="selectpicker"
							id="merchant"
							data-var="merchantPayId"
							data-id="subMerchant"
							list="merchantList"
							listKey="payId"
							listValue="businessName"
							autocomplete="off"
						/>
					</s:else>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->

			<!-- /.col-md-12 -->
			<s:if test="%{#session['USER'].superMerchant == true || superMerchant == true}">
				<div class="col-md-4 mb-20" data-target="subMerchant">
					<div class="lpay_select_group">
						<label for="">Sub Merchant</label>
						<s:select
							data-id="subMerchant"
							data-download="subMerchantPayId"
							data-var="subMerchantEmailId"
							headerKey="ALL"
							data-submerchant="subMerchant"
							data-user="subUser"
							name="subMerchantEmailId"
							class="selectpicker"
							id="subMerchant"
							list="subMerchantList"
							listKey="payId"
							headerValue="ALL"
							listValue="businessName"
							autocomplete="off"
						/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else>
				<div class="col-md-4 mb-20 d-none" data-target="subMerchant"> 
					<div class="lpay_select_group">
						<label for="">Sub Merchant</label>
						<select
							name="subMerchantEmailId"
							data-download="subMerchantPayId"
							headerKey="ALL"
							headerValue="ALL"
							data-var="subMerchantEmailId"
							data-submerchant="subMerchant"
							data-user="subUser"
							id="subMerchant"
							class="">
						</select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>

			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
					<label for="">Type</label>
					<s:select
						headerKey="ALL"
						headerValue="ALL"
						id="chargebackType"
						name="chargebackType"
						class="selectpicker"
						list="@com.paymentgateway.crm.chargeback.util.ChargebackType@values()"
						listKey="code"
						listValue="name"
						autocomplete="off"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-4 mb-20">
				<div class="lpay_select_group">
					<label for="">Status</label>
					<s:select
						headerKey="ALL"
						headerValue="ALL"
						id="chargebackStatus"
						name="chargebackStatus"
						class="selectpicker divalignment"
						list="@com.paymentgateway.crm.chargeback.util.ChargebackStatus@values()"
						lstkey="code"
						value="name"
						autocomplete="off"
					/>
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->

			<div class="col-md-4 mb-20 ">
				<div class="lpay_input_group">
				  <label for="">Order ID</label>
				  <s:textfield id="orderId" data-download="orderId" data-var="orderId" class="lpay_input" name="orderId"
				  type="text" value="" autocomplete="off"  ></s:textfield>
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-3 mb-20 -->

			<div class="col-md-4 mb-20">
				<div class="lpay_input_group">
					<label for="">Date From</label>
					<s:textfield
						type="text"
						readonly="true"
						id="dateFrom"
						name="dateFrom"
						class="lpay_input"
						autocomplete="off"
					/>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 mb-20 -->

			<div class="col-md-4 mb-20">
				<div class="lpay_input_group">
					<label for="">Date To</label>
					<s:textfield
						type="text"
						readonly="true"
						id="dateTo"
						name="dateTo"
						class="lpay_input"
						autocomplete="off"
					/>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-12 text-center">
				<button class="lpay_button lpay_button-md lpay_button-secondary" id="viewChargeback">Submit</button>
				<button class="lpay_button lpay_button-md lpay_button-primary" id="downloadChargeback">Download</button>
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<section class="view-chargeback lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">View Chargeback Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->

			<div class="col-md-12">
				<div class="lpay_table">
					<table id="chargebackDataTable" class="display" cellspacing="0" width="100%">
						<thead class="lpay_table_head">
							<tr>
								<th class="case-design">Case Id</th>
								<th>Order Id</th>
								<th>Super Merchant</th>
								<th>Merchant</th>
								<th>Type</th>
								<th>Status</th>
								<th>Amount</th>
								<th>Target Date</th>
								<th>Create Date</th>
								<th>Close Date</th>
								<th>Action</th>
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

	<s:form name="downloadChargebackReportAction" id="downloadChargebackReportAction" action="downloadChargebackReportAction">
		<s:hidden name="payId" id="download-payId" value="" />
		<s:hidden name="chargebackType" id="download-chargebackType" value="" />
		<s:hidden name="chargebackStatus" id="download-chargebackStatus" value="" />
		<s:hidden name="dateTo" id="download-dateTo" value=""/>
		<s:hidden name="dateFrom" id="download-dateFrom" value=""/>
		<s:hidden name="orderId" id="download-orderId"></s:hidden>
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="subMerchantPayId" id="download-subMerchant" />
	</s:form>

	<s:form name="viewChargebackDetails" action="viewChargebackDetailsAction">
		<s:hidden name="caseId" id="caseId" value="" />
		<s:hidden name="targetDate" id="targetDate" value="" />
		<s:hidden name="pgRefNum" id="pgRefNum" value="" />
		<s:hidden name="orderId" id="orderId" value=""/>
		<s:hidden name="capturedAmount" id="capturedAmount" value=""/>
		<s:hidden name="token" value="%{#session.customToken}" />
		<s:hidden name="actionStatus" value="ACTIVE" />
	</s:form>
	<script type="text/javascript">

	

	function downloadChargebackReportAction(){
		document.querySelector("#download-payId").value = document.getElementById("merchant").value;
		document.querySelector("#download-chargebackType").value = document.getElementById("chargebackType").value;
		document.querySelector("#download-chargebackStatus").value = document.getElementById("chargebackStatus").value;
		document.querySelector("#download-dateTo").value = document.getElementById("dateTo").value;
		document.querySelector("#download-dateFrom").value = document.getElementById("dateFrom").value;
		document.querySelector("#download-subMerchant").value = document.getElementById("download-subMerchant").value;
		document.querySelector("#download-orderId").value = document.querySelector("#orderId").value;
		// document.querySelector("#")
		document.querySelector("#downloadChargebackReportAction").submit();
	}

	document.getElementById("downloadChargeback").onclick = function(){
		downloadChargebackReportAction()
	}

		function getSubMerchant(_this){
		
        var _merchant = _this.target.value;
        var _subMerchantAttr = _this.target.attributes["data-id"].nodeValue;
        if(_merchant != ""){
            document.querySelector("body").classList.remove("loader--inactive");
            var data = new FormData();
            data.append('payId', _merchant);
            var _xhr = new XMLHttpRequest();
            _xhr.open('POST', "getSubMerchantListByPayId", true);
            _xhr.onload = function(){
                if(_xhr.status === 200){
					var obj = JSON.parse(this.responseText);
					console.log("obj "+obj);
                    document.querySelector("#"+_subMerchantAttr).innerHTML = "";
                    var  _option = "";
                    if(obj.superMerchant == true){
                        _option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value='ALL'>ALL</option>";
                        for(var i = 0; i < obj.subMerchantList.length; i++){
                            _option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
                        }
						document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
						document.querySelector("#"+_subMerchantAttr+" option[value='ALL']").selected = true;
						$("#"+_subMerchantAttr).selectpicker();
						$("#"+_subMerchantAttr).selectpicker("refresh");
                    }else{
						document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
						document.querySelector("#"+_subMerchantAttr).value = "";
					}
                }
            }
            _xhr.send(data);
            setTimeout(function(e){
                document.querySelector("body").classList.add("loader--inactive");
            }, 1000);
        }else{
            document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
            document.querySelector("#"+_subMerchantAttr).value = "";
        }
	}
	
	document.querySelector("#merchant").addEventListener("change", function(e){
        getSubMerchant(e);
    });	
	</script>
</body>

</html>