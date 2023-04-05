<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Nodal Settlement and Payout Date</title>
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
	$("body").remoClass("loader--inactive");
 
  // Initialize select2
  $("#merchant").select2();
});
</script>
	
<style>
.randomDisable{
  cursor: not-allowed;
  border: none;
  background-color: #c0d4f2;
  color: #666666;
}
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
	background-color:green;
	display: block;
    width: 100%;
    height: 30px;
    padding: 3px 4px;
    font-size: 14px;
    line-height: 1.42857143;
    color: #fff;
    border: 1px solid #ccc;
    border-radius: 4px;
	margin-top:25px;
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
.OtherList label {
    vertical-align: middle;
    display: block;
    color: #333;
    margin-bottom: 8px;
    margin-left: 3%;
	font-size:13px;
	font-weight:600;
}
.OtherList input {
    vertical-align: top;
    float: left;
    margin-left: 10px !important;
}
#wwctrl_nodalType {
	margin-top: 8% !important;
}
#loading {width: 100%;height: 100%;top: 0px;left: 0px;position: fixed;display: block; z-index: 99}
#loading-image {position: absolute;top: 50%;left: 52%;z-index: 100; width:10%;} 
.actionMessage li {
    color: black !important;
    background-color: #ccebda !important;
    border-color: #ccebda !important;
}
</style>


</head>
<body>

<s:form id="nodalPayoutsUpdateAction" action="nodalPayoutsUpdateAction"
		method="post">
                <tr>
					<s:actionmessage />
				</tr>
    
	
    <table width="100%" align="left" cellpadding="0" cellspacing="0" class="txnf">
		<tr>
			<td align="left"><h2>Nodal Settlement and Payout Date</h2>
				<div class="container">
				
					<div class="row padding10" style="margin-top:20px;">
						<div class="form-group col-md-2 txtnew col-sm-3 col-xs-6">
							<label for="merchant" style="margin-left: 2px;">Merchant:</label> <br />
							<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' ||    #session.USER_TYPE.name()=='SUPERADMIN'}">
								<s:select name="merchant" class="form-control" id="merchant" list="merchantList"
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
									 <s:select list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()" 
									  name="acquirer" id="acquirer" value="name" class="form-control"/>
						</div>
					
						<div class="form-group  col-md-3 col-sm-4 txtnew  col-xs-6">
							<label for="payment" >Payment Type:</label> <br />
							   <s:select class="form-control" list="@com.paymentgateway.commons.util.PaymentType@values()"
								listValue="name" listKey="code" name="paymentMethod" id="paymentMethod" autocomplete="off" value="" />
						</div>

				
						<div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
							<label for="dateFrom" >Settlement Date:</label> <br />
							<s:textfield type="text" readonly="true" id="settlementDate"
								name="settlementDate" class="form-control"  
								autocomplete="off"  />
						</div>
						
						
						<div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
							<label for="dateTo" >Nodal Settlement Date:</label> <br />
							<s:textfield type="text" readonly="true" id="nodalSettlementDate" name="nodalSettlementDate"
								class="form-control" autocomplete="off" />
						</div>
						
						
						<div class="OtherList" style="margin-left:10px; margin-top:5px;">
								<div id="nodalTypeDiv" >
									<s:radio list="#{'nodalSettlement':'Nodal Settlement Date','nodalPayoutDate':'Nodal Payout Date'}" name="nodalType" id ="nodalType" />
								</div>
						</div>
						
					
					</div>
					
					<div>
						<div class="form-group col-md-2 col-sm-3 txtnew col-xs-6 col-md-offset-5">
							   <s:submit id="btnEditUser" name="btnEditUser" value="Update"
								type="button" class="download-btn" onclick="showDiv()"></s:submit>
						</div>
					</div>
					
				</div>

			</td>
		</tr>
			<tr>
				<td align="left" style="border-bottom: 1px solid #eaeaea;">&nbsp;</td>
			</tr>
			
	</table>
	<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>

<script type="text/javascript">
	$(document).ready(function() {
		
		$(function() {
			$("#settlementDate").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			$("#nodalSettlementDate").datepicker({
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
			$('#settlementDate').val($.datepicker.formatDate('dd-mm-yy', today));
			$('#nodalSettlementDate').val($.datepicker.formatDate('dd-mm-yy', today));
           		

		});		
	});	
</script>


<script>
function openDialog(){
	if (confirm("Are you sure you want to udpate transactions?")) {
		
	}
	else{
		event.stopPropagation();
		return false;
	}
}
</script>

<script type="text/javascript">
     function showDiv() {
		//document.getElementById('Login').style.display = "none";
		$("body").removeClass("loader--inactive");
		  setTimeout(function() {
			$("body").addClass("loader--inactive");
	//document.getElementById('showme').style.display = "block";
		  },200000);
		   
		}
 </script>
<script>
$(document).ready(function(){
	$("#nodalTypenodalSettlement").attr('checked', 'checked');
});
</script>
</body>
</html>