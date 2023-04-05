<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html dir="ltr" lang="en-US">
<head>
<title>Merchant List</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" media="all"
	href="../css/daterangepicker-bs3.css" />
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script src="../js/bootstrap-select.min.js"></script>

<script type="text/javascript">
	
	function dateToolTip(){
    $("td.registerDate").each(function(e){
        var _getDate = $(this).text();
        if(_getDate != ""){
            var _getSpace = _getDate.indexOf(" ");
            var _getTime = _getDate.substring(_getSpace);
            var _getOnlyDate = _getDate.substring(0, _getSpace);
            $(this).text(_getOnlyDate);
            $(this).append("<div class='timeTip'>"+_getTime+"</div>");
        }
    })
}

	$(document).ready(function() {

		renderTable();
		
		$("#submit").click(function(env) {
			$("body").removeClass("loader--inactive");
			reloadTable();
			setTimeout(function(e){
				$("body").addClass("loader--inactive");
			}, 1000);
		});

	});

	function renderTable() {
		$('#txnResultDataTable').dataTable({
			dom : 'BTftlpi',
			buttons: ['csv', 'print', 'pdf'],
			language: {
				search: "",
				searchPlaceholder: "Search records"
			},	
			"ajax" : {
				"type" : "POST",
				"url" : "merchantSearchAction",
				"data" : function(d) {
					return generatePostData(d);
				}
			},

			"initComplete": function(settings, json) {
                // console.log("hello");
                dateToolTip();
            },

			"destroy" : true,
			"columns" : [
				{
					"data" : "registrationDate",
					"className" : "text-class registerDate"								
				},
				{
					"data" : "payId",
					"className" : "txnType text-class"
				},
				{
					"data" : "businessName",
					"className" : "txnType text-class"
				},
				{
					"data" : "emailId",
					"className" : "status text-class"
				},
				{
					"data" : "mobile",
					"className" : "text-class"

				},
				{
					"data" : "status",
					"className" : "text-class"
			
				},
			]
		});
	}

	function reloadTable() {
		var tableObj = $('#txnResultDataTable');
		var table = tableObj.DataTable();
		table.ajax.reload();
		setTimeout(function(e){
        dateToolTip();
        }, 1000);
	
	}

	function generatePostData(d) {
		var token = document.getElementsByName("token")[0].value;
		// var superMerchant = document.getElementById("merchant").value;
		var merchantEmail = document.getElementById("merchantEmail").value;
		var mobile = document.getElementById("mobile").value;
		var status = document.getElementById("status").value;
		if (merchantEmail == '') {
			merchantEmail = 'ALL'
		}
		if (mobile == '') {
			mobile = 'ALL'
		}
		if (status == '') {
			status = 'ALL'
		}
		

		var obj = {
			merchantEmail : merchantEmail,
			mobile : mobile,
			status : status,
			draw : d.draw,
			length : d.length,
			start : d.start,
			token : token,
			"struts.token.name" : "token",
		};

		return obj;
	}
	
</script>



</head>
<body id="mainBody">

	<input type="hidden" id="setGlobalData">

	<section class="sub-merchant-list lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Merchant List Filter</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_select_group">
				   <label for="">Status</label>
				   <s:select headerKey="" headerValue="All" class="selectpicker"
				   list="#{'ACTIVE':'ACTIVE','PENDING':'PENDING','TRANSACTION_BLOCKED':'TRANSACTION_BLOCKED','SUSPENDED':'SUSPENDED','TERMINATED':'TERMINATED'}" name="status" id="status" value="name"
					autocomplete="off" />
				</div>
				<!-- /.lpay_select_group -->  
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Merchant Email</label>
				<s:textfield id="merchantEmail" class="lpay_input"
				name="merchantEmail" type="text" value="" autocomplete="off"
				onblur="validateEmail(this);"></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-4 -->
			<div class="col-md-3 mb-20">
			  <div class="lpay_input_group">
				<label for="">Merchant Mobile</label>
				<s:textfield id="mobile" class="lpay_input"
				name="mobile" type="text" value="" autocomplete="off"
				></s:textfield>
			  </div>
			  <!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-12 text-center">
				<input type="button" id="submit" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary">
			</div>
			<!-- /.col-md-12 text-center -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<section class="sub-merchant lpay_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Merchant List</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="txnResultDataTable" class="" cellspacing="0"
							width="100%">
							<thead class="lpay_table_head">
								<tr>
									<th>Reg. Date</th>
									<th>Pay Id</th>
									<th>Merchant</th>
									<th>Email Id</th>
									<th>Mobile</th>
									<th>Status</th>
									<!-- <th style='text-align: center'>Action</th> -->
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

	<s:form name="subMerchEditFrm" id="subMerchEditFrm" action="editSubMerchCallAction">
		<s:hidden name="emailId" id="emailIdr" value="" />
		<s:hidden name="token" value="%{#session.customToken}" />
	</s:form>


</body>
</html>