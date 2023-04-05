<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html dir="ltr" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Reseller Accounts</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
<link rel="stylesheet" href="../css/subAdmin.css">
<style type="text/css">
.businessType .form-control {
	margin: 0;
}

#datatable_wrapper {
	overflow-y: auto;
}

.formbox {
	overflow-y: auto;
	table-layout: fixed;
}

.d-none {
	display: none !important;
}

.edit-permission {
	position: absolute;
	z-index: -1;
}

.buttons-columnVisibility {
	display: none !important;
}

.lpay_table .dataTables_filter{
			display: block !important;
		}

.buttons-columnVisibility.active {
	display: block !important;
}

.boxheadingsmall th {
	white-space: nowrap;
}

.form-control {
	width: 110% !important;
}

table.dataTable.display tbody tr.odd {
	background-color: #e6e6ff !important;
}

table.dataTable.display tbody tr.odd>.sorting_1 {
	background-color: #e6e6ff !important;
}

.btn:focus {
	outline: 0 !important;
}
</style>
</head>
<body>
	<div class="edit-permission">
		<s:property value="%{editPermission}"></s:property>
	</div>

	<section class="reseller-details lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Reseller Account List</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="datatable" class="display" cellspacing="0" width="100%">
						<thead class="lpay_table_head">
							<tr class="boxheadingsmall">
								<th>Pay ID</th>
								<th>Reseller ID</th>
								<th>Business Name</th>
								<th>Email ID</th>
								<th>Mobile</th>
								<th>Reg. Date</th>
								<th>Status</th>
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
	
	<s:form name="reseller" action="resellerSetup">
		<s:hidden name="payId" id="hidden" value="" />
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>

	<script type="text/javascript">
		$(window).on(
				"load",
				function(e) {
					// td remove if empty
					var _perm = $(".edit-permission").text();
					if (_perm == "false") {

						var td = $("#datatable").DataTable();
						td.columns(13).visible(false);

					}
					function dateToolTip() {
						$("body").removeClass("loader--inactive");
						$("td.registerDate").each(
								function(e) {
									var _getDate = $(this).text();
									if (_getDate != "") {
										var _getSpace = _getDate.indexOf(" ");
										var _getTime = _getDate
												.substring(_getSpace);
										var _getOnlyDate = _getDate.substring(
												0, _getSpace);
										$(this).text(_getOnlyDate);
										$(this).append(
												"<div class='timeTip'>"
														+ _getTime + "</div>");
									}
								})
						setTimeout(function(e) {
							$("body").addClass("loader--inactive");
						}, 500);
					}
					setTimeout(function(e) {

						dateToolTip();
					}, 500);
				});

		$(document).ready(function() {
							// var td = $("#datatable").DataTable();
							$('#datatable').on('page.dt', function() {
								$("body").removeClass("loader--inactive");
								setInterval(function(e) {
									dateToolTip();
								}, 500);
							});

							renderTable();

							function dateToolTip() {
								$("body").removeClass("loader--inactive");
								$("td.registerDate").each(
												function(e) {
													var _getDate = $(this)
															.text();
													if (_getDate != "") {
														var _getSpace = _getDate
																.indexOf(" ");
														var _getTime = _getDate
																.substring(_getSpace);
														var _getOnlyDate = _getDate
																.substring(0,
																		_getSpace);
														$(this).text(
																_getOnlyDate);
														$(this)
																.append(
																		"<div class='timeTip'>"
																				+ _getTime
																				+ "</div>");
													}
												})
								setTimeout(function(e) {
									$("body").addClass("loader--inactive");
								}, 500);
							}

							$('body')
									.on(
											'click',
											'.editMerchant',
											function() {
												console.log("hi");
												var _payId = $(this).closest(
														"tr").find(".payId")
														.text();
												document
														.getElementById('hidden').value = _payId;
												document.reseller.submit();
											});

							function handleChange() {
								$("body").removeClass("loader--inactive");
								reloadTable();
								var _merchantVal = $("#merchantStatus").val();
								if (_merchantVal == "APPROVED"
										|| _merchantVal == "REJECTED") {
									$(".approver").removeClass("d-none");
								} else {
									$("#byWhom").val("ALL");
									$(".approver").addClass("d-none");
								}
								setTimeout(function(e) {
									dateToolTip();
								}, 1000);
							}

							$(".form-control").on("change", handleChange);

							function renderTable() {
								$("body").removeClass("loader--inactive");
								var token = document.getElementsByName("token")[0].value;
								var buttonCommon = {
									exportOptions : {
										format : {
											body : function(data, column, row,
													node) {
												// Strip $ from salary column to make it numeric
												if (column == 13) {

												}
												return column === 0 ? "'"
														+ data
														: column === 2 ? data
																.replace(
																		"&#x40;",
																		"@")
																: data;
											}
										}
									}
								};
								$('#datatable')
										.dataTable(
												{
													language: {
														search: "",
														searchPlaceholder: "Search records"
													},
													dom : 'BTftlpi',

													buttons : [
															$
																	.extend(
																			true,
																			{},
																			buttonCommon,
																			{
																				extend : 'copyHtml5',
																				exportOptions : {
																					columns : [ ':visible :not(:last-child)' ]
																				}
																			}),
															$
																	.extend(
																			true,
																			{},
																			buttonCommon,
																			{
																				extend : 'csvHtml5',
																				exportOptions : {
																					columns : [ ':visible :not(:last-child)' ]
																				}
																			}),
															{
																extend : 'pdfHtml5',
																title : 'Merchant List',
																orientation : 'landscape',
																exportOptions : {
																	columns : [ ':visible :not(:last-child)' ]
																},
																customize : function(
																		doc) {
																	doc.defaultStyle.alignment = 'center';
																	doc.styles.tableHeader.alignment = 'center';
																}
															},
															{
																extend : 'print',
																title : 'Merchant List',
																exportOptions : {
																	columns : [ ':visible :not(:last-child)' ]
																}
															},
															{
																extend : 'colvis',
																//collectionLayout: 'fixed two-column',
																columns : [ 0,
																		1, 2,
																		3, 4,
																		5, 6, 7 ]
															} ],
													"ajax" : {
														"url" : "resellerDetailsAction",
														"type" : "POST",
														"data" : function(d) {
															return generatePostData(d);
														}
													},
													"initComplete" : function(
															settings, json) {
														// console.log("hello");
														dateToolTip();
													},
													"bProcessing" : true,
													"bLengthChange" : true,
													"bAutoWidth" : false,
													"iDisplayLength" : 10,
													"order" : [ [ 1, "asc" ] ],
													"aoColumns" : [
															{
																"mData" : "payId",
																"class" : "payId"
															},
															{
																"mData" : "resellerId"
															},
															{
																"mData" : "businessName"
															},
															{
																"mData" : "emailId"
															},
															{
																"mData" : "mobile"
															},
															//	{"mData" : "userType"},	
															{
																"mData" : "registrationDate",
																"class" : "registerDate"
															},
															{
																"mData" : "status"
															},
															{
																"mData" : null,
																"sClass" : "center",
																"bSortable" : false,
																"mRender" : function() {
																	return '<button class="lpay_button lpay_button-md lpay_button-secondary editMerchant" id="">Edit</button>';
																}
															},
															{
																"data" : null,
																"visible" : false,
																"className" : "displayNone",

															} ]
												});
							}
							function reloadTable() {
								var tableObj = $('#datatable');
								var table = tableObj.DataTable();
								table.ajax.reload();
							}

							function generatePostData(d) {
								var token = document.getElementsByName("token")[0].value;
								var businessType = null;
								var merhantStatus = null;
								var byWhom = null;
								// data: {"token": token,"merchantStatus":'ALL',"byWhom":null,"businessType":'ALL'},
								if (null != document
										.getElementById("merchantStatus")) {
									merhantStatus = document
											.getElementById("merchantStatus").value;
									// $(".approver").removeClass("d-none");
								} else {
									merchantStatus = "ALL";
									// $(".approver").addClass("d-none");
								}
								if (null != document
										.getElementById("industryTypes")) {
									businessType = document
											.getElementById("industryTypes").value;
								} else {
									businessType = 'ALL';
								}
								if (null != document.getElementById("byWhom")) {
									byWhom = document.getElementById("byWhom").value;
								} else {
									byWhom = "ALL"
								}

								var obj = {
									token : token,
									businessType : businessType,
									merchantStatus : merhantStatus,
									byWhom : byWhom
								};
								return obj;
							}
						});
	</script>
</body>
</html>