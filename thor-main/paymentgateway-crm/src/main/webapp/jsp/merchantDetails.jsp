<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html dir="ltr" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Merchant Accounts</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
<link rel="stylesheet" href="../css/subAdmin.css">
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>
<style>

	.lpay_table .dataTables_filter{ display: block !important; }
	.lpay_table #datatable{ white-space: nowrap; }
	.lpay_button.editMerchant{ padding: 8px 12px;font-weight: 600 !important;margin-left: 0 !important; }
	.lpay_button.editMerchant i{ margin-right: 5px; }
	.common-status{ padding: 5px 10px 4px;border-radius: 5px;font-weight: 600;background-color:#6fa6ff;color: #082552; }
	.active-status{ background-color: #c5f196;color: #3c6411; }
	.suspended-status{ background-color: #ccc;color: #464646; }
	.pending-status{ background-color: #ffda70;color: #8f6c08 }
	.terminated-status{ background-color: #f9a7a7;color: #6a1111; }
	.rejected-status{ background-color: #9f0c0c;color: #fff; }

</style>
</head>
<body>
	<div class="edit-permission" style="z-index: -1;position: absolute;"><s:property value="%{editPermission}"></s:property></div>
	<div class="merchant-list lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="heading_with_icon mb-30">
			<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
			<h2 class="heading_text">Merchant Account Filter</h2>
		</div>
		<!-- /.heading_icon -->
		<div class="row businessType">
			<div class="col-md-3 txtnew col-sm-4 col-xs-6">
				<div class="form-group lpay_select_group">
					<label for="merchant">Business Type:</label> <br />
					<s:select headerKey="ALL" headerValue="ALL" data-live-search="true" name="industryTypes" id="industryTypes"
					class="form-control selectpicker"  list="industryTypes" value="ALL"/>
				</div>
				<!-- /.form-group -->
			</div>
			<div class="col-md-3 txtnew col-sm-4 col-xs-6">
				<div class="form-group lpay_select_group">
					<label for="merchant">Status:</label> <br />
					<s:select class="form-control selectpicker" headerKey="ALL" headerValue="ALL"
					list="@com.paymentgateway.commons.util.UserStatusType@values()"
					id="merchantStatus" name="userStatus"
					value="ALL"/>
				</div>
				<!-- /.form-group -->
			</div>
			<div class="col-md-3 txtnew col-sm-4 col-xs-6">
				<div class="form-group d-none approver lpay_select_group">
					<label for="merchant">By Whom:</label> <br />
					<select name="byWhom" id="byWhom" class="form-control selectpicker">
						<option value="ALL">ALL</option>
						<option value="admin">Admin</option>
						<option value="maker">Maker</option>
						<option value="checker">Checker</option>
					</select>
				</div>
				<!-- /.form-group d-none approver -->
			</div>
		</div>
	</div>
	<!-- /.merchant-list -->
	<div class="merchant_data lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-10">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Merchant Account List</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12 lpay_table_style-2">
				<div class="lpay_table">
					<table id="datatable" class="display" cellspacing="0" width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th>Pay Id</th>
								<th>Business Name</th>
								<th>Status</th>
								<th>Mobile</th>
								<th>Updated Date</th>
								<th>Reg. Date</th>
								<th>Action</th>
							</tr>
						</thead>
					</table>
				</div>
				<!-- lpay_table	 -->
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</div>
	<s:form name="merchant" action="merchantSetup">
		<s:hidden name="payId" id="hidden" value="" />
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>
	<script type="text/javascript">

	$(window).on("load", function(e){
		// td remove if empty
		var _perm = $(".edit-permission").text();
		if(_perm == "false"){

			var td = $("#datatable").DataTable();
			td.columns(13).visible(false);

		}
    });
	

	$(document).ready(function() {

	function format ( d ) {
		_new = "<div class='main-div'>";
		var _obj = {
			"emailId": "Email ID",
			"userType": "User Type",
			"makerName": "Maker Name",
			"makerStatus": "Maker Status",
			"makerStatusUpDate": "Maker Status Update",
			"checkerName": "Checker Name",
			"checkerStatus": "Checker Status",
			"checkerStatusUpDate": "Checker Status Update"
		}
		for(key in _obj){
			if(_obj[key].hasOwnProperty("className")){
				var _getKey = Object.keys(_obj[key]);
				_new += '<div class="inner-div '+_obj[key]["className"]+'">'+
						'<span>'+_obj[key][_getKey[0]]+'</span>'+
						'<span>'+d[_getKey[0]]+'</span>'+
					'</div>'
			}else{
				_new += '<div class="inner-div">'+
					'<span>'+_obj[key]+'</span>'+
					'<span>'+d[key]+'</span>'+
				'</div>'
			}
		}
		_new += "</div>";
		return _new;
	}

	renderTable();

	$('body').on('click','.editMerchant',function(){
		$("body").removeClass("loader--inactive");
		var _payId = $(this).closest("tr").find(".payId").text();
		document.getElementById('hidden').value = _payId;
		document.merchant.submit();
	});

	function handleChange() {

		$("body").removeClass("loader--inactive");
		reloadTable();
		var _merchantVal = $("#merchantStatus").val();
		if(_merchantVal == "APPROVED" || _merchantVal == "REJECTED"){
			$(".approver").removeClass("d-none");
		}else{
			$("#byWhom").val("ALL");
			$(".approver").addClass("d-none");
		}

	}

	$(".form-control").on("change", handleChange);

	function renderTable() {

		$("body").removeClass("loader--inactive");
		$('#datatable').dataTable({
			language: {
				search: "",
				searchPlaceholder: "Search records"
			},		
			"ajax" : {
				"url" : "merchantDetailsAction",
				"type" : "POST",
				"data" : function (d){
					return generatePostData(d);
				}
			},
			"initComplete": function(settings, json) {
				$("body").addClass("loader--inactive");
  			},
			"bProcessing" : true,
			"bLengthChange" : true,
			"bAutoWidth" : false,
			"iDisplayLength" : 10,
			"order": [[ 1, "asc" ]],
			"aoColumns" : [ 
				{"mData" : "payId", "class": "payId"}, 
				{"mData" : "businessName", "width": "20%"},
				{
					"mData" : null,
					"mRender" : function(row){
						if(row.status == "ACTIVE"){
							return "<span class='active-status common-status'>"+row.status+"</span>";
						}else if(row.status == "PENDING"){
							return "<span class='pending-status common-status'>"+row.status+"</span>";
						}else if(row.status == "APPROVED"){
							return "<span class='approved-status common-status'>"+row.status+"</span>";
						}else if(row.status == "SUSPENDED"){
							return "<span class='suspended-status common-status'>"+row.status+"</span>";
						}else if(row.status=="TRANSACTION_BLOCKED"){
							return "<span class='blocked-status common-status'>"+row.status+"</span>";
						}else if(row.status=="TERMINATED"){
							return "<span class='terminated-status common-status'>"+row.status+"</span>";
						}else if(row.status == "REJECTED"){
							return "<span class='rejected-status common-status'>"+row.status+"</span>";
						}else{
							return "<span class='common-status'>"+row.status+"</span>";
						}
					}
				},	
				{"mData" : "mobile"},
				{"mData" : "updatedDate"},
				{"mData" : "registrationDate", "class": "registerDate"},
				{
                "mData" : null,
                "bSortable" : false,
                "mRender" : function() {
                    return '<button class="lpay_button lpay_button-sm lpay_button-secondary pointer editMerchant" id="editPermission"><i class="fa fa-pencil" aria-hidden="true"></i> Edit</button>';
                }
            }]
		});		
	}

	$("body").on("click", "#datatable tbody td", function(e){
		var table = new $.fn.dataTable.Api('#datatable');
		if(e.target.localName != "button"){
			var tr = $(this).closest('tr');
			var row = table.row(tr);
			if ( row.child.isShown() ) {
				tr.removeClass('shown');
				setTimeout(function(e){
					row.child()[0].children[0].classList.remove("active-row");
					row.child.hide();
				}, 600)
			}
			else {
				row.child( format(row.data()) ).show();
				row.child()[0].children[0].classList.add("active-row");
				getAllData();
				tr.addClass('shown');
			}
		}
		
	})

	function getAllData(){
		var _new = document.querySelectorAll(".inner-div");
		_new.forEach(function(index, array, element){
			var _getValue = _new[array].children[1].innerText;
			if(_getValue == 'null' || _getValue == '' || _getValue == 'undefined'){
				_new[array].classList.add("d-none");
			}
		})
	}

	function reloadTable() {
		var tableObj = $('#datatable');
		var table = tableObj.DataTable();
		table.ajax.reload();
		setTimeout(function(e){
			$("body").addClass("loader--inactive");
		}, 500)
	}

	function generatePostData(d) {
		var token = document.getElementsByName("token")[0].value;
		var businessType = null;
		var merhantStatus = null;
		var byWhom = null;
		// data: {"token": token,"merchantStatus":'ALL',"byWhom":null,"businessType":'ALL'},
		if(null != document.getElementById("merchantStatus")){
			merhantStatus = document.getElementById("merchantStatus").value;
		}else{
			merchantStatus = "ALL";
		}
		if(null != document.getElementById("industryTypes")){
			businessType = document.getElementById("industryTypes").value;
		}else{
			businessType = 'ALL';
		}
		if(null != document.getElementById("byWhom")){
			byWhom = document.getElementById("byWhom").value;
		}else{
			byWhom = "ALL"
		}

		var obj = {				
				token : token,
				businessType : businessType,
				merchantStatus: merhantStatus,
				byWhom: byWhom
		};
		return obj;
	}
});
	</script>
	<!-- <script type="text/javascript" src="../js/merchant-detail.js"></script> -->
 </body>
</html>