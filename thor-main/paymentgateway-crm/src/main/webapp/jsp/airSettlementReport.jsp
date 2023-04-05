<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>

<style>

.uploadButton{
    border: none;
	background-color: #002163;
    border-radius: 5px;
    width: 25%;
    font-size: 18px;
	color:white;
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

<title>Air Settlement Report</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
	<link href="../css/Jquerydatatable.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/loader.css">
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script type="text/javascript" src="../js/moment.js"></script>
	<script type="text/javascript" src="../js/daterangepicker.js"></script>
	<link href="../css/loader.css" rel="stylesheet" type="text/css" />
	<script src="../js/jquery.popupoverlay.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
	

</head>
<body id="mainBody">

	<h2 class="pageHeading">Air Settlement Report</h2>
	<br>
	<br>
	 <table class="table98 padding0">
        
        
 
  <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
    
		
		<div id="saleDownload" >  
        <div>
            <br />
            <form id="settlementDownload" name="settlementDownload" method="post" action="downloadAirSettlementReport">
             <table class="table98 padding0 profilepage">
                <div style="margin-top:0px;">
		            <div class="form-group col-md-3 col-md-offset-2 txtnew col-sm-3 col-xs-6">
						<label for="merchant" style="margin-left: -27% !important;" >Merchant:</label> 
						<s:if
							test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'  || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
							<s:select name="merchantPayId" class="form-control" id="merchantPayId"
								headerKey="ALL" headerValue="ALL" list="merchantList"
								listKey="payId" listValue="businessName" onchange="handleChange();"   autocomplete="off" 
								style="margin-left: -55% !important;"/>
						</s:if>
						<s:else>
							<s:select name="merchantPayId" class="form-control" id="merchantPayId"
								headerKey="ALL" headerValue="ALL" list="merchantList"
								listKey="payId" listValue="businessName" onchange="handleChange();" autocomplete="off" />
						</s:else>
					 </div>
					 
					 
					 <div class="form-group  col-md-3 col-sm-4 txtnew  col-xs-6">
						<label for="email" style="margin-left: -20% !important;">Currency:</label> 
						<s:select name="currency" id="currency" headerValue="ALL"
								headerKey="ALL" list="currencyMap" class="form-control" style="margin-left: -40% !important;" />
					</div>
					
					<div class="form-group  col-md-3 col-sm-3 txtnew col-xs-6">
						<label for="dateFrom" style="margin-left: -55px !important; width: 120% !important;">Sale Date:</label> 
						<s:textfield type="text" readonly="true" id="saleDate"
							name="saleDate" class="form-control" onchange="handleChange();" 
							autocomplete="off" style="margin-left: -28px !important; width: 120% !important;"/>
					</div>
				</div>
				<br>
					<br>
					<br>
					<br>
					<br>
					
					<div>
					  <button class="uploadButton">Download</button>
					</div>
					<br>
	         	</div>

              </table>
              </form>
        </div>  
        </div>  
		
		
		
    
      </td>
    </table>

<script type="text/javascript">
	    function handleChange() {
			
     }

	$(document).ready(function() {
		
		$(function() {
			$("#saleDate").datepicker({
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
			$('#saleDate').val($.datepicker.formatDate('dd-mm-yy', today));
			

		});
	});		
</script>


</body>
</html>