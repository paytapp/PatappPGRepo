<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>EMI BinRange Summary</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet" />

<script type="text/javascript">

function isNumber(evt) {
	var binCodeLow = document.getElementById("binCodeLow").value;
    evt = window.event;
    var charCode = (evt.which) ? evt.which : evt.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57)) {
        return false;
	}
	
	else if (binCodeLow.length > 5){
		if(charCode === 13) {
			// reloadTable();
			renderTable();
		}
		return false;
	}
    return true;
}

function checkLength(){
	var binCodeLow = document.getElementById("binCodeLow").value;
	var hiddenVal = document.getElementById("minValueHidden").value;
	var binLength = binCodeLow.length;
	if(binLength == 6){
		document.getElementById("binCodeLow").style.borderColor = "#ccc";
		document.getElementById("alertDigitMsd").innerHTML = "";
		// reloadTable();
		renderTable();
	}
	else{
		document.getElementById("binCodeLow").style.borderColor = "red";
		document.getElementById("alertDigitMsd").innerHTML = "It should be minimum 6 digit";
		return false;
	}
	

}
	function handleChange() {
		reloadTable();
	}
	function decodeVal(text) {
		return $('<div/>').html(text).text();
	}

	

	function renderTable() {
		var token = document.getElementsByName("token")[0].value;
		$('#BinRange').dataTable({
			language: {
				search: "",
				searchPlaceholder: "Search..."
			},
			dom : 'BTftlpi',
			buttons : [ {
				extend : 'copyHtml5',
				title : 'BinRange',
				exportOptions : {
					columns : [':visible' ]
				}
			}, {
				extend : 'csvHtml5',
				title : 'BinRange',
				exportOptions : {
					columns : [ ':visible' ]
				}
			} ],
			"ajax" : {
				"url" : "displayEmiBin",
				"type" : "POST",
				"data" : function(d) {
					return generatePostData(d);
				}
			},
			"destroy": true,
			"iDisplayLength" : "10",
			"paginationType" : "full_numbers",
			"lengthMenu" : [[10, 25, 50], [10, 25, 50]],
			"order" : [],
			"aoColumns" : [ {
				"mData" : "binCodeHigh"
			},{
				"mData" : "binCodeLow"
			},{
				"mData" : "binRangeHigh"
			},{
				"mData" : "binRangeLow"
			}, {
				"mData" : "mopType"
			}, {
				"mData" : "cardType"
			}, {
				"mData" : "cardHolder"
			}, {
				"mData" : "paymentRegion"
			},{
				"mData" : "issuerBankName"
			}, {
				"mData" : "issuerCountry"
			} ]
		});
	}

	$(document).ready(function() {
		$('#example').DataTable({
			
			dom : 'B',
			buttons : [ {extend: 'csv', className: "lpay_button lpay_button-md lpay_button-primary", text: "Download CSV Format"} ]
		});
		$("#BinRange").dataTable({
			language: {
				search: "",
				searchPlaceholder: "Search..."
			},
			dom : 'BTftlpi',
			buttons : [ {
				extend : 'copyHtml5',
				title : 'BinRange',
				exportOptions : {
					columns : [':visible' ]
				}
			}, {
				extend : 'csvHtml5',
				title : 'BinRange',
				exportOptions : {
					columns : [ ':visible' ]
				}
			} ]
		});
	});
	// search min and max value function ends here

	function reloadTable() {
		$("#BinRange").dataTable().fnDestroy();
	}
	function generatePostData(d) {
		var token = document.getElementsByName("token")[0].value;
		var obj = {
			binCode : document.getElementById("binCodeLow").value,
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
<body>


	<section class="manage-bin-range lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Bin Range Summary</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-3">
			  <div class="lpay_input_group">
				<label for="">Bin Code Low</label>
				<input type="text" value="" id="binCodeLow" onkeypress="return isNumber(event);" class="lpay_input">
				<span style="color: #f00" id="alertDigitMsd"></span>
			  </div>
			  <!-- /.lpay_input_group -->
			  <input type="hidden" id="minValueHidden">
			</div>
			<!-- /.col-md-3 -->
			<div class="col-md-3">
				<button id="searchValue" onclick="checkLength()" class="lpay_button lpay_button-md lpay_button-secondary lpay_button-with-input">Search</button>
			</div>
			<!-- /.col-md-3 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	<section class="manage-bin-range lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Bincode Data</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12">
				<div class="lpay_table">
					<table id="BinRange" align="center" class="display" cellspacing="0" width="100%">
						<thead>
							<tr class="lpay_table_head">
								<th>Bin Code High</th>
								<th>Bin Code Low</th>
								<th>Bin Range High</th>
								<th>Bin Range Low</th>
								<th>Mop Type</th>
								<th>Card Type</th>
								<th>Card Holder</th>
								<th>Payment Region</th>
								<th>Issuer Bank Name</th>
								<th>Issuer Country</th>								
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

	<section class="manage-bin-range lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Upload Bin Range</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-4">
				<form action="binRangeManeger" name="contact-form" method="post" id="uploadBinForm"
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
				</form>
				<div class="button-wrapper lpay-center mt-20">
					<s:submit value="Upload" name="fileName" id="btnUpload" class="lpay_button lpay_button-md lpay_button-secondary" />
					<!-- create table for download csv format -->
					<table id="example" style="display: none;">
						<thead>
							<tr>
								<th>Bin Code High</th>
								<th>Bin Code Low</th>
								<th>Bin Range High</th>
								<th>Bin Range Low</th>
								<th>Card Type</th>
								<th>Group Code</th>
								<th>Issuer Bank Name</th>
								<th>Issuer Country</th>
								<th>Mop Type</th>
								<th>Product Name</th>
								<th>Rfu1</th>
								<th>Rfu2</th>
							</tr>
						</thead>
					</table>
				</div>
				<!-- /.button-wrapper -->
			</div>
			<!-- /.col-md-4 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	
	<script type="text/javascript">
		$(".lpay_upload_input").on("change", function(e){
        var _val = $(this).val();
        var _fileSize = $(this)[0].files[0].size;
        var _tmpName = _val.replace("C:\\fakepath\\", "");
        if(_val != ""){
            $("body").removeClass("loader--inactive");
            $(".default-upload").addClass("d-none");
            $("#placeholder_img").css({"display":"none"});
            if(_fileSize < 2000000){
                $(this).closest("label").attr("data-status", "success-status");
                $("#fileName").text(_tmpName);
                $("#bulkUpdateSubmit").attr("disabled", false);
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500);
            }else{
                $(this).closest("label").attr("data-status", "error-status");
                $("#bulkUpdateSubmit").attr("disabled", true);
                setTimeout(function(e){
                    $("body").addClass("loader--inactive");
                }, 500);
            }
        }
    });
	</script>
</body>
</html>