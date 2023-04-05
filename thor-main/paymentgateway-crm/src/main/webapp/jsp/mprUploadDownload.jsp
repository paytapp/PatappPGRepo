<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<style>

.nav {
	 margin-bottom: 18px;
	margin-left: 0;
	list-style: none;
}

.nav > li > a {
	display: block;
}

.nav-tabs{
	*zoom: 1;
}

.nav-tabs:before,
.nav-tabs:after {
	display: table;
	content: "";
}

.nav-tabs:after {
	clear: both;
}

.nav-tabs > li {
	float: left;
}

.nav-tabs > li > a {
	padding-right: 12px;
	padding-left: 12px;
	margin-right: 2px;
	line-height: 14px;
}

.nav-tabs {
	border-bottom: 1px solid #ddd;
}

.nav-tabs > li {
	margin-bottom: -1px;
}

.nav-tabs > li > a {
	padding-top: 8px;
	padding-bottom: 8px;
	line-height: 18px;
	border: 1px solid transparent;
   -webkit-border-radius: 4px 4px 0 0;
  -moz-border-radius: 4px 4px 0 0;
	border-radius: 4px 4px 0 0;
}

.nav-tabs > li > a:hover {
	border-color: #eeeeee #eeeeee #dddddd;
}

.nav-tabs > .active > a,
.nav-tabs > .active > a:hover {
	color: #555555;
	cursor: default;
	background-color: #ffffff;
	border: 1px solid #ddd;
	border-bottom-color: transparent;
}

li {
	 line-height: 18px;
}

.tab-content.active{
	display: block;
}

.tab-content.hide{
	display: none;
}
.nav-tabs>li>a:hover{border-top: 0px solid transparent;}

		
/* .uploadButton{
    border: none;
	background-color: #002163;
    border-radius: 5px;
    width: 25%;
    font-size: 18px;
	color:white;
} */

.uploadButton{background-color: #002163 !important;
			display: inline-block;
			padding: 8px 20px;
			font-size: 12px !important;
			line-height: 1.42857143;
			color: #fff;
			border: 1px solid #ccc;
			border-radius: 4px;
			}
			
.heading{
   text-align: center;
    color: black;
    font-weight: bold;
    font-size: 22px;
}
.txtnew label {
    /* display: inline-block; */
    /* max-width: 100%; */
    display: block;
    text-align: left;
}
.form-control{
	margin-left: 0 !important;
	width: 100% !important;
}
		

</style>

<title>Acquirer MPR Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
	<link href="../css/Jquerydatatable.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/loader.css">
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script type="text/javascript" src="../js/moment.js"></script>
	<script type="text/javascript" src="../js/daterangepicker.js"></script>
	<link href="../css/loader.css" rel="stylesheet" type="text/css" />
	<script src="../js/jquery.popupoverlay.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>

	<style>
		.mprTabs li a:hover{
			background-color: #fff;
			color: #000
		}
	</style>
	
	
<script>


	$(document).ready(function() {
		$('.nav-tabs > li > a').click(function(event){
		event.preventDefault();//stop browser to take action for clicked anchor

		//get displaying tab content jQuery selector
		var active_tab_selector = $('.nav-tabs > li.active > a').attr('href');

		//find actived navigation and remove 'active' css
		var actived_nav = $('.nav-tabs > li.active');
		actived_nav.removeClass('active');

		//add 'active' css into clicked navigation
		$(this).parents('li').addClass('active');

		//hide displaying tab content
		$(active_tab_selector).removeClass('active');
		$(active_tab_selector).addClass('hide');

		//show target tab content
		var target_tab_selector = $(this).attr('href');
		$(target_tab_selector).removeClass('hide');
		$(target_tab_selector).addClass('active');
	     });
	  });
	  
	  
	  
</script>
	
	
</head>
<body id="mainBody">
<div style="width: 100%; max-width: 1200px; margin: auto; margin-bottom: 30px;">
	<section class="lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Acquirer MPR Report</h2>
				</div>
				<!-- /.heading_icon -->
			</div>


	<!-- <h2 class="pageHeading">Acquirer MPR Report</h2> -->
	<br>
	<br>
	 <table class="table98 padding0">
        
        <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
        <ul class="nav nav-tabs mprTabs" style="border-bottom:none;">
        <li class="active"><a href="#MprUpload">MPR Upload</a></li>
        <li><a href="#MprDownload">MPR Download</a></li>  		
    </ul>
    </td>
        </tr>
 
  <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
    
	<!----------------------------FIRST TAB CONTENT------------------------->
<section id="MprUpload" class="tab-content active">
    <div> 
        <table class="table98 padding0 profilepage">
            <form id="mprForm" name="mprForm" method="post" action="mprUploadAction" enctype="multipart/form-data">
                <div style="margin-top:20px;">
		            <!-- <div class="form-group col-md-2 col-md-offset-2 txtnew col-sm-3 col-xs-6"> -->
		            <div class="col-md-3 mb-20">
				        <div class="lpay_select_group">
							<label for="merchant">Acquirer:</label> 
							<s:select name="acquirerName" class="selectpicker" id="acquirerName" headerKey=""
								headerValue="Select Acquirer" list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
								listKey="name" listValue="code" autocomplete="off"/>
						</div>	
					</div>			 
					 
					<!-- <div class="form-group  col-md-2 col-sm-4 txtnew  col-xs-6"> -->
					<div class="col-md-3 mb-20">
				     	<div class="lpay_select_group">
							<label for="email">Payment Type:</label> 
							<select class="selectpicker" name="paymentType"
							id="paymentType">
								<option>Select Payment Type</option>
								<option>Cards</option>
								<option>UPI</option>
							</select>
						</div>
					</div>
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
							<label for="dateFrom">Date:</label> 
							<s:textfield type="text" readonly="true" id="mprUploadDate"
								name="mprDate" class="lpay_input" 
								autocomplete="off"/>
						</div>
					</div>
					
					<br>
					<br>
					<br>
					<br>
					<br>
					<div>
						    
						<input name="file" id="my_file"  type="file" accept=".xlsx, .xls, .csv, .xlsb"  style="margin-left: -8%; margin-bottom: 3%;" onchange="ValidateSingleInput(this);">
						<button class="uploadButton lpay_button-md lpay_button-secondary" id="submit" style="margin-right:70px !important;" onClick='checkValidation(event)'>Upload</button>
							
					</div>
					<span id="selectAcq" style="color:red; font-size:15px; display:none; margin-left:-5%;">Please Select Acquirer</span>
					<span id="selectPay" style="color:red; font-size:15px; margin-left:-5%; display:none;">Please Select Payment Type</span>
					
					<tr><td align="center" valign="top"></td>
				</tr>
				</div>
					<div id="errors" style="color:red;"></div>
					<div id="success" style="color:green;"></div>
					<tr><td align="center" valign="top">
						</td></tr>
				    <s:actionmessage class="success success-text" />
           		</form>
            </table>
                
            </div>
        </section>
       
		
		<!----------------------------SECOND TAB CONTENT------------------------->
		
<section id="MprDownload" class="tab-content hide">  
    <div>
        <table class="table98 padding0 profilepage">
            <form id="mprDownload" name="mprDownload" method="post" action="mprDownloadAction">
                <div style="margin-top:20px;">
                    <div class="col-md-3 mb-20">
				    	<div class="lpay_select_group">
		            	<!-- <div class="form-group col-md-2 col-md-offset-2 txtnew col-sm-3 col-xs-6"> -->
						<label for="merchant">Acquirer:</label> 
						<s:select name="acquirerName" class="selectpicker" id="acquirerName" headerKey=""
							headerValue="Select Acquirer" list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
							listKey="name" listValue="code" autocomplete="off"/>
						</div>
					</div>	
					<div class="col-md-3 mb-20">
				    	<div class="lpay_select_group">
						<!-- <div class="form-group  col-md-2 col-sm-4 txtnew  col-xs-6"> -->
						<label for="email">Payment Type:</label> 
							<select class="selectpicker" name="paymentType" id="paymentType">
							    <option>Select Payment Type</option>
							    <option>Cards</option>
								<option>UPI</option>
							</select>
						</div>
					</div>
					<div class="col-md-3 mb-20">
						<div class="lpay_input_group">
						<label for="dateFrom">Date:</label> 
						<s:textfield type="text" readonly="true" id="mprDownlaoadDate"
							name="mprDate" class="lpay_input" 
							autocomplete="off"/>
						</div>
					</div>
				</div>
				<br>
					<br>
					<br>
					<br>
					<br>
				<div>
					<button class="uploadButton lpay_button-md lpay_button-secondary" style="margin-right:140px !important;">Download</button>
				</div>
            </form>
        </table> 
    </div>  
</section>  

</td>
</tr>
</table>
</div>
</section>
</div>
		
    
     

<script type="text/javascript">
	$(document).ready(function() {
		
		$(function() {
			$("#mprUploadDate").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date(),
				changeMonth : true,
        		changeYear : true,
			});
			
		});
		$(function() {
			var today = new Date();
			$('#mprUploadDate').val($.datepicker.formatDate('dd-mm-yy', today));
		});
	});
	
			
</script>

<script type="text/javascript">
	$(document).ready(function() {
		
		$(function() {
			$("#mprDownlaoadDate").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
		});
		$(function() {
			var today = new Date();
			$('#mprDownlaoadDate').val($.datepicker.formatDate('dd-mm-yy', today));
			

		});
	});		
</script>

<script>
var _validFileExtensions = [".xlsx" , ".xls" , ".csv" ,".xlsb"];    
function ValidateSingleInput(oInput) {
    if (oInput.type == "file") {
        var sFileName = oInput.value;
         if (sFileName.length > 0) {
            var blnValid = false;
            for (var j = 0; j < _validFileExtensions.length; j++) {
                var sCurExtension = _validFileExtensions[j];
                if (sFileName.substr(sFileName.length - sCurExtension.length, sCurExtension.length).toLowerCase() == sCurExtension.toLowerCase()) {
                    blnValid = true;
					//alert("file uploaded successfully")
                    break;
                }
            }
             
            if (!blnValid) {
                alert("Sorry, this is invalid, allowed extensions are: " + _validFileExtensions.join(", ")+ " " + "only");
                oInput.value = "";
                return false;
            }
        }
    }
    return true;
}
</script>

<script>
function checkValidation(event){
	var acquirerVal = document.getElementById("acquirerName").value;
	var paymentVal = document.getElementById("paymentType").value;
	    document.getElementById("selectAcq").style.display = "none";
		document.getElementById("selectPay").style.display = "none";
	if (acquirerVal == "Select Acquirer" || acquirerVal == ""){
		document.getElementById("selectAcq").style.display = "block";
		event.preventDefault();
	}else if(paymentVal == "Select Payment Type" || paymentVal == ""){
		document.getElementById("selectPay").style.display = "block";
		event.preventDefault();
	}else{
		document.getElementById("selectAcq").style.display = "none";
		document.getElementById("selectPay").style.display = "none";
	}
}
</script>

</body>
</html>