<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>

<style>

		
.heading{
   text-align: center;
    color: #002163;
    font-weight: bold;
    font-size: 22px;
}
.samefnew {
    width: 15.5% !important;
    float: left;
    font-size:12px;
	font-weight:600;
    color: #333;
    line-height: 22px;
    margin: 0 0 0 10px;
}
.cust {
	width: auto;
    font-size:12px;
    font-weight:600;
    color: #333;
    line-height: 22px;
    margin: 0 0 0 0px !important;
}
.submit-button{
	width:10% !important;
	height:28px !important;
	margin-top:-4px !important;
}
.MerchBx {
    min-width: 92%;
    margin: 15px;
    margin-top: 25px !important;
    padding: 0;
}

table.dataTable thead .sorting {
    background: none !important;
}
.sorting_asc {
    background:none !important;
}
table.dataTable thead .sorting_desc {
    background: none !important;
}
table.dataTable thead .sorting {
     cursor: default !important;
}
table.dataTable thead .sorting_desc, table.dataTable thead .sorting {
    cursor: default !important;
}
table.dataTable.display tbody tr.odd {
    background-color: #e6e6ff !important;
}
table.dataTable.display tbody tr.odd > .sorting_1{
	 background-color: #e6e6ff !important;
}
#loading {width: 100%;height: 100%;top: 0px;left: 0px;position: fixed;display: block; z-index: 99}
#loading-image {position: absolute;top: 40%;left: 55%;z-index: 100; width:10%;}	

#mainTable{
	table-layout: fixed;
}

.MerchBx{
	margin: 0 !important;
	margin-top: 20px !important;
}

.primary-btn{
	height: 43px;
	margin-top: 0px !important;
	padding: 4px 31px 6px !important;
}

.agent-search_inputs_div{ display: flex;align-items: center;justify-content: space-between;width: 100%;margin-bottom: 20px;padding: 0 15px }
.user-pin_row{ display: flex;align-items: end;flex-wrap: wrap; }
</style>

<title>Reset User PIN</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<!-- <script src="../js/jquery-latest.min.js" type="text/javascript"></script> -->
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js" type="text/javascript"></script>

	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<script src="../js/commanValidate.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">

<script type="text/javascript">

function renderTable() {
	let _mobileNumber = document.querySelector("#mobile").value;
	if(_mobileNumber.length === 10){

		$("body").removeClass("loader--inactive");
		$('#datatable').dataTable({
			language: {
				search: "",
				searchPlaceholder: "Search records"
			},		
			"ajax" : {
				"url" : "fetchUser",
				"type" : "POST",
				"data" : { 'mobile': document.querySelector("#mobile").value }
			},
			"initComplete": function(settings, json) {
				$("body").addClass("loader--inactive");
			  },
			"bProcessing" : true,
			"bLengthChange" : true,
			"destroy": true,
			"bAutoWidth" : false,
			"iDisplayLength" : 10,
			"order": [[ 1, "asc" ]],
			"aoColumns" : [ 
				{"mData" : "businessName", "class": "payId"}, 
				{"mData" : "emailId"},
					
				{"mData" : "mobile"},
				{
				"mData" : null,
				"bSortable" : false,
				"mRender" : function() {
					return '<button class="lpay_button lpay_button-sm lpay_button-secondary pointer resetPin" id="editPermission"> Reset PIN</button>';
				}
			}]
		});	
	}else{
		alert("Please enter valid mobile number.");
	}

}

$(document).ready(function(e){

	$("#datatable").dataTable();

	$('body').on('click','#datatable .resetPin',function() {
		var table = new $.fn.dataTable.Api('#datatable');
		var _getClosestTr = $(this).closest("tr");
		var _data = table.rows(_getClosestTr).data();
		console.log(_data);
		$.ajax({
			type: 'POST',
			url: 'resetUserPIN',
			data: {
				mobile: _data[0].mobile,
				payId: _data[0].payId
			},
			success: function(responseJson){
				if(responseJson.response === "success"){
					$(".lpay_popup-innerbox").attr("data-status", "success");
				}else{
					$(".lpay_popup-innerbox").attr("data-status", "error");
				}
				$(".lpay_popup .responseMsg").text(responseJson.responseMessage);
				$(".lpay_popup").fadeIn();
			}
		})
	})

	$(".confirmButton").on("click", function(e){
		$(".lpay_popup").fadeOut();
	})

})

</script>

</head>
<body id="mainBody">
	<section class="agent-search lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row user-pin_row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Reset User PIN</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3 mb-20">
				<div class="lpay_input_group">
					<label for="">User Mobile Number</label>
					<input type="text" data-var='mobile' onkeypress="javascript:return isNumber (event)" maxlength="10" id="mobile" value="" class="lpay_input"></input>
				</div>
				<!-- /.lpay_input_group -->
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-3 mb-20">
				<input type="button" id="agentSearchAction" onclick="renderTable()" value="Submit" class="lpay_button lpay_button-md lpay_button-secondary"></input>				
			</div>
			<!-- /.col-md-3 mb-20 -->
			<div class="col-md-12 lpay_table_style-2">
				<div class="lpay_table">
					<table id="datatable" class="display" cellspacing="0" width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th>Name</th>
								<th>Email</th>
								<th>Mobile Number</th>
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
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
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

</body>
</html>