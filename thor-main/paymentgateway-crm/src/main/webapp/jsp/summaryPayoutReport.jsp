<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Summary Payout Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/jquery.popupoverlay.js"></script> 
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>  
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>
<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />

<script type="text/javascript">
$(document).ready(function(){
 
  // Initialize select2
  $("#merchant").select2();
});
</script>
	
<script>
 var expanded = false;
function showCheckboxes(e) {
  var checkboxes = document.getElementById("checkboxes");
  if (!expanded) {
    checkboxes.style.display = "block";
    expanded = true;
  } else {
    checkboxes.style.display = "none";
    expanded = false;
  }
   e.stopPropagation();

}

function getCheckBoxValue(){
	 var allInputCheckBox = document.getElementsByClassName("myCheckBox");
  		
  		var allSelectedAquirer = [];
  		for(var i=0; i<allInputCheckBox.length; i++){
  			
  			if(allInputCheckBox[i].checked){
  				allSelectedAquirer.push(allInputCheckBox[i].value);	
  			}
  		}

  		document.getElementById('selectBox').setAttribute('title', allSelectedAquirer.join());
  		if(allSelectedAquirer.join().length>28){
  			var res = allSelectedAquirer.join().substring(0,27);
  			document.querySelector("#selectBox option").innerHTML = res+'...............';
  		}else if(allSelectedAquirer.join().length==0){
  			document.querySelector("#selectBox option").innerHTML = 'Select Acqirer(s)';
			$('#Download').prop('disabled',true).addClass('disabled');
  		}else{
  			document.querySelector("#selectBox option").innerHTML = allSelectedAquirer.join();
			$('#Download').prop('disabled',false).removeClass('disabled');
  		}
}
</script>
<script type="text/javascript">
$(document).ready(function(){
	$(document).click(function(){
		expanded = false;
		$('#checkboxes').hide();
	});
	$('#checkboxes').click(function(e){
		e.stopPropagation();
	});
	$('#Download').prop('disabled',true).addClass('disabled');

});
</script>


<style>
  .divalignment{
	  margin-top: -30px !important;
  }
  
  .case-design{
	  text-decoration:none;
	  cursor: default !important;
  }
  .my_class:hover{
	  color: white !important;
  }
 .multiselect {
    width: 210px;
	display:block;
	margin-left:-20px;	
 }
  .selectBox {
  position: relative;
 }

#checkboxes {
  display: none;
  border-radius: 5px;
  border: 1px #dadada solid;
  height:300px;
  overflow-y: scroll;
  position:Absolute;
  background:#fff;
  z-index:1;
  margin-left:2px;
  margin-right:10px;
}

#checkboxes label {
  width: 74%;
}
#checkboxes input {
  width:18%;

}
.selectBox select {
  width: 95%;
  
}
.overSelect {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
}
.download-btn {
	background-color:#002163;
	display: block;
    width: 100%;
    height: 30px;
    padding: 3px 4px;
    font-size: 14px;
    line-height: 1.42857143;
    color: #fff;
    border: 1px solid #ccc;
    border-radius: 4px;
	margin-top:30px;
}
.form-control{
	margin-left: 0!important;
	width: 100% !important;
}
.padding10{
	padding: 10px;
}
.disabled {
    color:#fff;
	border-color: #a0a0a0;
	background-color: #a0a0a0;
}
</style>


</head>
<body>
<form id="downloadSummaryPayoutReportAction" name="downloadSummaryPayoutReportAction" action="downloadSummaryPayoutReportAction">
	
    <table width="100%" align="left" cellpadding="0" cellspacing="0" class="txnf">
		<tr>
			<td align="left"><h2>Summary Payout Report</h2>
				<div class="container">

					<div class="row padding10">
						<div class="form-group col-md-2 txtnew col-sm-3 col-xs-6">
							<label for="merchant" style="margin-left: 2px;">Merchant:</label> <br />
							<s:if
							test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
							<s:select name="merchant" class="form-control" id="merchant"
								headerKey="ALL" headerValue="ALL" list="merchantList"
								listKey="emailId" listValue="businessName" autocomplete="off" />
						    </s:if>
							<s:else>
								<s:select name="merchant" class="form-control" id="merchant"
									headerKey="" headerValue="ALL" list="merchantList"
									listKey="emailId" listValue="businessName" autocomplete="off" />
							</s:else>
							
						</div>


						<div class="form-group  col-md-3 col-sm-4 txtnew  col-xs-6">
								<label for="aquirer" >Acquirer</label> <br />
								<div>
									    <div class="selectBox" id="selectBox" onclick="showCheckboxes(event)">
											<select class="form-control">
												<option>Select Acquirer</option>
											</select>
										<div class="overSelect"></div>
										</div>
									<div id="checkboxes" onclick="getCheckBoxValue()">
											   <s:checkboxlist headerKey="Select Acqirer(s)" headerValue="Select Acqirer(s)" list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()" 
												name="acquirer" id="acquirer" value="name" class="myCheckBox" listKey="code" listValue="name"
												/>
									</div>
							    </div>
								
						</div>
					
						<div class="form-group  col-md-3 col-sm-4 txtnew  col-xs-6">
							<label for="email" >Payment Type:</label> <br />
							   <select class="form-control" style="margin-left:17px; width:75% !important;" id="paymentType" name="paymentType">
							    <option>ALL</option>
							    <option>CC,DC</option>
								<option>UPI</option>
							   </select>
						</div>

				
						<div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
							<label for="dateFrom" style="margin-left:-40% !important;">Date From:</label> <br />
							<s:textfield type="text" readonly="true" id="dateFrom"
								name="dateFrom" class="form-control"  
								autocomplete="off" style="margin-left: -40% !important; width:118% !important;" onchange="handleChange();"/>
						</div>
					
						<div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
							<label for="dateTo" style="margin-left: -25% !important;">Date To:</label> <br />
							<s:textfield type="text" readonly="true" id="dateTo" name="dateTo"
								class="form-control" autocomplete="off" style="margin-left:-24% !important; width: 118% !important;" onchange="handleChange();"/>
						</div>
					
					</div>
					
					<div>
						<div class="form-group col-md-2 col-sm-3 txtnew col-xs-6 col-md-offset-5">
							   <button class="download-btn" id="Download">Download</button>
						</div>
					</div>
					
				</div>

			</td>
		</tr>
			<tr>
				<td align="left" style="border-bottom: 1px solid #eaeaea;">&nbsp;</td>
			</tr>
			
	</table>
	</form>


<script type="text/javascript">
    function handleChange() {
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
     }
	$(document).ready(function() {
		
		$(function() {
			$("#dateFrom").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			$("#dateTo").datepicker({
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
			$('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
			$('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));	

		});		
	});	
</script>

<script>
var acquirer=[];
		var inputElements = document.getElementsByName('acquirer');
		for(var i=0; inputElements[i]; ++i){
		  if(inputElements[i].checked){	
			  acquirer.push( inputElements[i].value);
			  
		  }
		}
        var acquirerString = acquirer.join();	
</script>

</body>
</html>