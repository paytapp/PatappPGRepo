<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<html dir="ltr" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>User Audit Trail</title>
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
	.lpay_table .dataTables_filter{
		display: block !important;
	}
	.lpay_table #datatable{
		white-space: nowrap;
	}
	td.payId{ text-decoration: underline;color: #00f;cursor: pointer; }
	.user_details{ display: flex;flex-wrap: wrap; }
	.user_details .detail_heading{ width: 100%;padding: 12px 10px;background-color: #eee;display: inline-block;border-radius: 5px;font-size: 14px;margin: 20px 5px 10px;text-transform: capitalize;font-weight: 500; }
	.user_details .detail_box{ width: 100%;max-width: 25%;background-color: #eee;border: 5px solid #fff;padding: 15px 10px;border-radius: 15px;  }
	.detail_box [data-view]{ display: block;margin-top: 10px;font-weight: 600; }
	.lp-user_trail_popup{ display: none;z-index: 999; }
	.lp-user_trail_flex{ position: fixed;width: 100%;height: 100%;left: 0;top:0;z-index: 99;display: flex;align-items: center;justify-content: center;background-color: rgba(0,0,0,.8);overflow-y: scroll; }
	.lp-user_trail_box{ background-color: #fff;width: 100%;max-width: 1200px;margin: auto;padding: 15px 0;border-radius: 5px;z-index: 99; }
	.lp-user_trail_box h3{ margin-left: 20px;margin-bottom: 10px;padding-left: 15px;border-left: 5px solid #0d34a2; }
	.lp-user_trail_box h3 .lpay_button{ float: right;margin-right: 20px !important;margin-top: -3px; }
</style>
</head>
<body>
	<div class="edit-permission" style="z-index: -1;position: absolute;"><s:property value="%{editPermission}"></s:property></div>
	<div class="merchant-list lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="heading_with_icon mb-30">
			<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
			<h2 class="heading_text">User Audit Trail Filter</h2>
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
		</div>
	</div>
	<!-- /.merchant-list -->
	<div class="merchant_data lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-10">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">User Audit Trail List</h2>
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
								<th>Mobile</th>
								<th>Email Id</th>
								<th>Status</th>
								<th>Updated Date</th>
								<th>Updated By</th>
								<th>Updated By Email</th>
								<th>Updated By UserType</th>
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

	<div class="lp-user_trail_popup">
		<div class="lp-user_trail_flex">
			<div class="lp-user_trail_box">
				<h3>User Details <span class="lpay_button lpay_button-md lpay_button-primary" onclick="closePopup()">Close</span></h3>
				<div class="lp-view_trail col-md-12">
	
				</div>
				<!-- /.lp-view_trail -->
			</div>
			<!-- /.lp-user_trail_flex -->
		</div>
		<!-- /.lp-user_trail_box -->
	</div>
	<!-- /.lp-user_trail_popup -->

	<script type="text/javascript">

	$(window).on("load", function(e){
		// td remove if empty
		var _perm = $(".edit-permission").text();
		if(_perm == "false"){

			var td = $("#datatable").DataTable();
			td.columns(13).visible(false);

		}
    });

	function closePopup(){
		$(".lp-user_trail_popup").fadeOut();
		$(".detail_box").addClass("d-none");
	}
	

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

	$('body').on('click','.payId',function(){
		$("body").removeClass("loader--inactive");
		var _table = new $.fn.dataTable.Api('#datatable');
		var _getClosestTr = $(this).closest("tr");
		var _data = _table.rows(_getClosestTr).data();
		$.ajax({
			type: "POST",
			url: "getUserAuditDataById",
			data: {
				"id" : _data[0]['id'],
					
			},
			success: function(data){
				for(key in data.userDataByPayId[0]){
					var _checkNull = document.querySelector("[data-view='"+key+"']");
					if(_checkNull != null){
						if(data.userDataByPayId[0][key] != "" && data.userDataByPayId[0][key] != null){
							_checkNull.closest(".detail_box").classList.remove("d-none");
							_checkNull.innerText = data.userDataByPayId[0][key];
						}
					}
				}
				setTimeout(function(e){
					$("body").addClass("loader--inactive");
				}, 500);
				$(".lp-user_trail_popup").fadeIn();
				
			}
		})
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
				"url" : "getUserAuditDataTrail",
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
			"order": [[ "5", "desc" ]],
			"aoColumns" : [ 
				{"mData" : "payId", "class": "payId"}, 
				{"mData" : "businessName"},
				{"mData" : "mobile"},	
				{"mData" : "emailId"},
				{"mData" : "userStatus"},
				{"mData" : "mpaDataUpdateDate"},
				{"mData" : "mpaDataUpdatedBy"},
				{"mData" : "mpaDataUpdatedByEmail"},
				{"mData" : "mpaDataUpdatedByUserType"}
				
            ]
		});		
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

	var _data = {

		merchant_Details : {
			"businessName" : "Business Name",
			"industryCategory" : "Industry Category",
			"typeOfEntity" : "Type Of Entity",
			"companyName" : "Legal Name",
			"cin" : "CIN",
			"dateOfIncorporation" : "Date of Incorporation",
			"companyEmailId" : "Registered Email",
			"companyRegisteredAddress" : "Registered Address",
			"tradingAddress1" : "Trading Address",
			"country" : "Country",
			"state" : "State",
			"tradingPin" : "PIN",
			"businessPan" : "Company PAN",
			"gstin" : "GST",
			"companyPhone" : "Business Phone",
			"companyWebsite" : "Company Website",
			"businessEmailForCommunication" : "Email for Communication",
		},
		principal_Information: {
			"contactName" : "Contact Person",
			"contactMobile" : "Mobile",
			"contactEmail" : "Email",
			"contactLandline" : "Landline",
			"director1FullName" : "Director-1 Name",
			"director1Pan"  : "Director-1 PAN",
			"director1Email" : "Director-1 Email",
			"director1Mobile" :"Director-1 Mobile",
			"director1Address" :"Director-1 Address",
			"director1Landline" : "Director-1 Landline",
			"director2FullName" : "Director-2 Name",
			"director2Pan"  : "Director-2 PAN",
			"director2Email" : "Director-2 Email",
			"director2Mobile" :"Director-2 Mobile",
			"director2Address" :"Director-2 Address",
			"director2Landline" : "Director-2 Landline",
		},
		bankDetails: {
			"accountNumber" : "Account Number",
			"virtualAccountNo" : "Virtual Account Number",
			"merchantVPA" : "Merchant VPA",
			"accountHolderName" : "Account Name",
			"accountIfsc" : "IFSC",
			"accountMobileNumber" : "Account Mobile Number"
		},
		business_Details: {
			"annualTurnover" : "Annual Turnover (Approx)",
			"annualTurnoverOnline": "Annual Turnover ( Online )",
			"percentageCC" : "Credit Card",
			"percentageDC" : "Debit Card",
			"percentageNB" : "Net Banking",
			"percentageUP" : "UPI",
			"percentageWL" : "Wallet",
			"percentageEM" : "EMI",
			"percentageCD" : "COD/Cash",
			"percentageNeftOrImpsOrRtgs" : "NEFT/IMPS/RTGS",
			"percentageDomestic" : "Total Cards Domestic",
			"percentageInternational" : "Total Cards International",
		},
		on_Board_Details : {
			"payId" : "Pay Id",
			"salt" : "Salt",
			"encKey":"Encryption Key",
			"resellerId" : "Reseller Id",
			"terminalId" : "Terminal Id",
			"superMerchantId" : "Super Merchant Id",
			"requestUrl" : "Request URL",
			"registrationDate" : "Registration Date",
			"activationDate" : "Activation Date",
			"paymentCycle" : "Payment Cycle (In days)",
		},
		system_settings: {
			"defaultCurrency" : "Default Reporting Currency",
			"paymentMessageSlab" : "Payment Message Slab",
			"merchantHostedFlag" : "Merchant Hosted Flag",
			"iframePaymentFlag" : "iframe Payment",
			"checkOutJsFlag" : "Checkout JS",
			"surchargeFlag" : "Surcharge",
			"discountingFlag" : "Discount",
			"loadWalletFlag" : "Load Wallet",
			"eposMerchant" : "ePOS Merchant",
			"bookingRecord" : "Booking Report",
			"eNachReportFlag" : "eNACH Report",
			"upiAutoPayReportFlag" : "UPI AutoPay Report",
			"acceptPostSettledInEnquiry" : "Custom Status Enquiry Flag",
			"customTransactionStatus" : "Custom Transaction Status",
			"capturedMerchantFlag" : "Custom Capture Report",
			"paymentAdviceFlag" : "Payment Advice Email",
			"retailMerchantFlag" : "Marketplace Settlement Report",
			"lyraPay" : "Lyra Pay",
			"non3dsTxn" : "Non 3DS Txn",
			"retryTransactionCustomeFlag" : "Retry Transaction",
			"attemptTrasacation" : "Number of Retry",
			"whiteListReturnUrlFlag" : "White List FLag",
			"whiteListReturnUrl" : "White List Return URL",
			"autoRefund" : "Auto Refund(Post Settled Txn.)",
			"saveVPAFlag" : "Save VPA",
			"vpaSaveParam" : "VPA Save Param",
			"saveNBFlag" : "Save NB Bank",
			"nbSaveParam" : "NB Save Param",
			"saveWLFlag" : "Save wallet",
			"wlSaveParam" : "WL Save Param",
			"expressPay" : "Express Pay",
			"expressPayParameter" : "Card Save Param",
			"merchantInitiatedDirectFlag" : "Payout",
			"nodalReportFlag" : "Nodal Report",
			"virtualAccountFlag" : "Virtual Account",
			"topupFlag" : "Topup Flag",
			"statementFlag" : "Settlement Flag",
			"accountVerificationFlag" : "Account Verification",
			"vpaVerificationFlag" : "VPA Verification",
			"allowCustomHostedUrl" : "Allow Custom Hosted URL",
			"customHostedUrl" : "Custom Hosted URL",
			"skipOrderIdForRefund" : "Skip OrderId for Refund",
			"allowDuplicateSaleOrderId" : "Allow Duplicate Sale",
			"allowDuplicateRefundOrderId" : "Allow Duplicate Refund",
			"allowDuplicateSaleOrderIdInRefund" : "Allow Sale In Refund",
			"allowDuplicateRefundOrderIdSale" : "Allow Refund In Sale",
			"allowDuplicateNotSaleOrderId" : "Unique Order Id",
			"smtMerchant" : "SMT Merchant",
			"logoFlag" : "Merchant Logo",
			"logoName" : "Self Logo",
			"allowLogoInPgPage" : "Allow Logo in PG Page",
			"codName" : "COD Text",
			"allowPartSettle" : "Allow Part Settle",
			"partAnnualTurnover" : "Annual Turnover",
			"deviation" : "Deviation",
			"mCC" : "Merchant Category Code",
			"allowQRScanFlag" : "Allow PG QR",
			"allowUpiQRFlag" : "Allow UPI QR",
			"callBackUrl" : "Callback URL",
			"callBackFlag" : "Status Enquiry Callback Flag",
			"allCallBackFlag" : "All Payment Type Callback Flag",
			

  		}
		
	}
	var _div = "<div class='user_details'>";
	for(key in _data){
		_div += "<span class='detail_heading'>"+key.replaceAll("_", " ")+"</span>";
			
		for(key2 in _data[key]){
			_div += "<div class='detail_box d-none'><span>"+_data[key][key2]+"</span><span data-view='"+key2+"'></span></div>";
		}
	}
	_div += "</div>";

	document.querySelector(".lp-view_trail").innerHTML = _div;
});
	</script>
 </body>
</html>