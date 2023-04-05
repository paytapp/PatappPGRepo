<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>Hotel Inventory</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet" />
<script type="text/javascript" src="../js/moment.js"></script>
<script type="text/javascript" src="../js/daterangepicker.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
<script src="../js/commanValidate.js"></script>
<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>
<script type="text/javascript">
$(document).ready(function(){
 
  handleChange();
});
</script>

<script type="text/javascript">

function handleChange() {
	renderTable();
	reloadTable();
}
function decodeVal(text) {
	return $('<div/>').html(text).text();
}


function renderTable() {
	//to show new loader -Harpreet
	 $.ajaxSetup({
            global: false,
            beforeSend: function () {
            	toggleAjaxLoader();
            },
            complete: function () {
            	toggleAjaxLoader();
            }
        });
	var table = new $.fn.dataTable.Api('#HotelInv');
	 $.ajaxSetup({
            global: false,
            beforeSend: function () {
               $(".modal").show();
            },
            complete: function () {
                $(".modal").hide();
            }
        });
	var token = document.getElementsByName("token")[0].value;
	
	$('#HotelInv')
	.dataTable(
			{
				language: {
					search: "",
            searchPlaceholder: "Search Hotels"
        		},
				dom : 'BTftlpi',
				buttons : [ {
					extend : 'copyHtml5',
					exportOptions : {
						columns : [':visible']
					}
				}, {
					extend : 'csvHtml5',
					title : 'hotelInventory',
					exportOptions : {
						columns : [':visible']
					}
				}, {
					extend : 'pdfHtml5',
					orientation : 'landscape',
					title : 'hotelInventory',
					exportOptions : {
						columns : [':visible']
						},
					customize: function (doc) {
					    //doc.content[1].table.widths = Array(doc.content[1].table.body[0].length + 1).join('*').split('');
					    doc.defaultStyle.alignment = 'center';
     					doc.styles.tableHeader.alignment = 'center';
				  	}
				  	
				}, {
					extend : 'print',
					title : 'hotelInventory',
					exportOptions : {
						columns : [':visible']
					}
				}, {
					extend : 'colvis',
					//           collectionLayout: 'fixed two-column',
					columns : [0, 1, 2, 3, 4, 5, 6, 7, 8, 9,10,11,12]
				} ],			
				"ajax" : {
					"url" : "hotelInvAction",
					"type" : "POST",
					"data" : function (d){
								return generatePostData(d);
							}
				},
				"bProcessing" : true,
				"bDestroy" : true,
				"bLengthChange" : true,
				"iDisplayLength" : 10,
				"order" : [ [
						1,
						"desc" ] ],
				"aoColumns" : [ {
					"mData" : "id"
				},{
					"mData" : "hotelName"
				},{
					"mData" : "state"
				},{
					"mData" : "district"
				}, {
					"mData" : "city"
				}, {
					"mData" : "streetName"
				}, {
					"mData" : "contactName"
				}, {
					"mData" : "contactNumber"
				}, {
					"mData" : "contactEmail"
				}, {
					"mData" : "roomsCategory"
				}, {
					"mData" : "totalRooms"
				}, {
					"mData" : "availableRooms"
				}, {
					"mData" : "tariff"
				} ]
			});
}
function reloadTable() {		

	var tableObj = $('#HotelInv');
	var table = tableObj.DataTable();
	table.ajax.reload();
}

function generatePostData(d) {
	var token = document.getElementsByName("token")[0].value;
	var obj = {
		draw : d.draw,
		length : d.length,
		start : d.start,
		token : token,
		"struts.token.name" : "token",
	};

	return obj;
}
	
$(document).ready(function() {
	$('#example').DataTable({
		dom : 'B',
		buttons : [ {extend: 'csv', className: "lpay_button lpay_button-md lpay_button-primary", text: "Download CSV Format"} ]
	});
});

</script>

<style>
	#binCodeLow{
		width: 100% !important;
	}
.btn-fl{float: right;clear: right;margin-right: -70px;}
.btn-small{padding: 6px!important;}
.bws-tp{margin-bottom: -5px!important; margin-top: -10px;}
/*.inputfieldsmall {height: 28px!important;padding:4px 80px!important;} */
table #HotelInv tbody th, table #HotelInv tbody td{text-align:center;}
.inputfieldsmall1 {
    display: inline-block!important;
    padding: 4px 15px;
    height: 28px;
    font-size: 11px;
    font-family: 'Titillium Web', sans-serif;
    line-height: 1.428571429;
    color: black;
    margin-bottom: 5px;
    background-color: #fff;
    background-image: none;
    border: 1px solid #ccc;
    border-radius: 4px;
    -webkit-box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
    box-shadow: inset 0 1px 1px rgba(0, 0, 0, .075);
    -webkit-transition: border-color ease-in-out .15s, box-shadow ease-in-out .15s;
    transition: border-color ease-in-out .15s, box-shadow ease-in-out .15s;
	}
.form-control{
	margin-left: 0px !important;
}

#searchValue{
	margin-top: 21px;
	padding: 5px 17px !important;
}

table.dataTable thead .sorting {
    background: none !important;
}
.sorting {
    background: none !important;
}
.btn:focus{
		outline: 0 !important;
}


</style>
</head>
<body>

	<section class="manage-hotel lapy_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
					  <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Hotel Inventory Detail</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12">
					<div class="lpay_table">
						<table id="HotelInv" align="center" cellspacing="0" width="100%" style="text-align:center;">
							<thead class="lpay_table_head">
								<tr>
									<th style='text-align: center'>Id</th>
									<th style='text-align: center'>Hotel</th>
									<th style='text-align: center'>State</th>
									<th style='text-align: center'>District</th>
									<th style='text-align: center'>City</th>
									<th style='text-align: center'>Street</th>
									<th style='text-align: center'>Contact Name</th>
									<th style='text-align: center'>Mobile</th>
									<th style='text-align: center'>Email</th>
									<th style='text-align: center'>Category</th>
									<th style='text-align: center'>Total Rooms</th>
									<th style='text-align: center'>Available Rooms</th>
									<th style='text-align: center'>Tariff</th>								
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

		<section class="manage-hotel lapy_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Inventory Upload</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-4">
					<form action="hotelInvManager" name="contact-form" method="post" id="uploadInvForm"
					enctype="multipart/form-data">
					<label for="upload-input" class="lpay-upload">
						<input type="file" name="fileName" accept=".csv" id="upload-input" class="lpay_upload_input">
						<div class="default-upload">
							<h3>Upload Your CSV File</h3>
							<img src="../image/image_placeholder.png" class="img-responsive" id="placeholder_img" alt="">
						</div>
						<!-- /.default-upload -->
						<div class="upload-status">
							<div class="success-wrapper upload-status-inner d-none">
								<div class="success-icon-box status-icon-box">
									<img src="../image/tick.png" alt="">
								</div>
								<div class="success-text-box">
									<h3>Upload Successfully</h3>
									<div class="fileInfo">
										<span id="fileName"></span>
									</div>
									<!-- /.fileInfo -->
								</div>
								<!-- /.success-text-box -->
							</div>
							<!-- /.success-wraper -->
							<div class="error-wrapper upload-status-inner d-none">
								<div class="error-icon-box status-icon-box">
									<img src="../image/wrong-tick.png" alt="">
								</div>
								<div class="error-text-box">
									<h3>Upload Failed</h3>
									<div class="fileInfo">
										<div id="fileName">File size too Long.</div>
									</div>
									<!-- /.fileInfo -->
								</div>
								<!-- /.success-text-box -->
							</div>
							<!-- /.success-wraper -->
						</div>
						<!-- /.upload-success -->
					</label>
					<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
				
				<div class="button-wrapper lpay-center mt-20">
					<button value="Upload" name="fileName" id="btnUpload" class="lpay_button lpay_button-md lpay_button-secondary" >Submit</button>
					<table id="example" style="display: none;">
						<thead>
							<tr>
								<th>Hotel Name</th>
								<th>State</th>
								<th>District</th>
								<th>City</th>
								<th>Street Name</th>
								<th>Contact Name</th>
								<th>Contact Number</th>
								<th>Email</th>
								<th>Room category</th>
								<th>Total Rooms</th>
								<th>Available Rooms</th>
								<th>Tariff</th>
								<th>UDF1</th>
								<th>UDF2</th>
								<th>UDF3</th>
								<th>UDF4</th>
							</tr>
						</thead>
					</table> 
				</div>
				<!-- /.button-wrapper lpay_center mt-20 -->
			</form>
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row -->
		</section>
		<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
</body>
</html>