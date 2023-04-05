<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Add Beneficiary</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery.minshowpop.js"></script>
<script src="../js/jquery.formshowpop.js"></script>
<script src="../js/commanValidate.js"></script>
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/moment.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>

<script src="../js/jszip.min.js" type="text/javascript"></script>
<script src="../js/vfs_fonts.js" type="text/javascript"></script>
<script src="../js/buttons.colVis.min.js" type="text/javascript"></script>
<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />

<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>

<script type="text/javascript">
	$(document).ready(function() {
		document.getElementById("btnEditUser").disabled=true;
	        $('#acquirer').change(function(event){
		   var acquirer = document.getElementById("acquirer").value;
		   if( acquirer=="ALL"|| acquirer == "" ){
			   document.getElementById("btnEditUser").disabled=true;
			  return false;
			 }
		   else{
			   document.getElementById("btnEditUser").disabled=false;	   
		   }		
	   });
	});

	function saveBeneficiary() {
			
			var custId = document.getElementById("custId").value;
			var beneficiaryCd = document.getElementById("beneficiaryCd").value;
			var srcAccountNo = document.getElementById("srcAccountNo").value;
			var beneName = document.getElementById("beneName").value;
			var beneAccountNo = document.getElementById("beneAccountNo").value;
			var ifscCode = document.getElementById("ifscCode").value;
			var bankName = document.getElementById("bankName").value;
			var paymentType = document.getElementById("paymentType").value;
			var beneType = document.getElementById("beneType").value;
			var currencyCode = document.getElementById("currencyCode").value;
			var acquirer = document.getElementById("acquirer").value;
			var status = "ACTIVE";
			
			if ( custId == null ||  custId.trim() == ""){
					alert ("Enter Cust Id !");
					return false;
			}
			if ( beneficiaryCd == null ||  beneficiaryCd.trim() == ""){
					alert ("Enter Beneficiary Cd !");
					return false;
			}
			if ( srcAccountNo == null ||  srcAccountNo.trim() == ""){
					alert ("Enter Nodal account number !");
					return false;
			}
			if ( beneName == null ||  beneName.trim() == ""){
					alert ("Enter Beneficiary Name !");
					return false;
			}
			if ( beneAccountNo == null ||  beneAccountNo.trim() == ""){
					alert ("Enter Beneficiary Account number !");
					return false;
			}
			if ( ifscCode == null ||  ifscCode.trim() == ""){
					alert ("Enter IFSC code !");
					return false;
			}
			if ( bankName == null ||  bankName.trim() == ""){
					alert ("Enter Bank Name !");
					return false;
			}if ( paymentType == null ||  paymentType.trim() == "ALL" ||  paymentType.trim() == "" ){
					alert ("Select Payment Type !");
					return false;
			}
			if ( beneType == null ||  beneType.trim() == "ALL" ||  beneType.trim() == "" ){
					alert ("Select Beneficiary Type !");
					return false;
			}
			if ( acquirer == null ||  acquirer.trim() == "ALL" ||  acquirer.trim() == "" ){
					alert ("Select Beneficiary Type !");
					return false;
			}
			if ( currencyCode == null ||  currencyCode.trim() == "ALL" ||  currencyCode.trim() == "" ){
					alert ("Select Currency Code !");
					return false;
			}
			
			
			var token  = document.getElementsByName("token")[0].value;
			$("body").removeClass("loader--inactive");
			document.getElementById("btnEditUser").disabled=true;
		$.ajax({
			type: "POST",
			url:"beneficiarySaveAction",
			timeout: 0,
			data:{"beneficiaryCd":beneficiaryCd,"custId":custId,"ifscCode":ifscCode,"paymentType":paymentType,"beneType":beneType,"currencyCode":currencyCode,"acquirer":acquirer, "srcAccountNo":srcAccountNo, "status":status, "beneName":beneName, "bankName":bankName, "beneAccountNo":beneAccountNo, "token":token,"struts.token.name": "token",},
			success:function(data){
				$("body").addClass("loader--inactive");
				var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
				alert(response);
				$("body").addClass("loader--inactive");
				//reloadTable();
				document.getElementById("btnEditUser").disabled=true;
				window.location.reload();
		    },
			error:function(data){
				alert("Network error, Beneficiary not be saved");
				$("body").addClass("loader--inactive");
				document.getElementById("btnEditUser").disabled=true;
				window.location.reload();
			}
		});
			
		};
		
	

</script>
<style type="text/css">.error-text{color:#a94442;font-weight:bold;background-color:#f2dede;list-style-type:none;text-align:center;list-style-type: none;margin-top:10px;
}.error-text li { list-style-type:none; }
#response{color:green;}
.errorMessage{
  display: none;
}
.errorInpt{
      font: 400 11px arial ;
      color: red;
      display: none;
      margin-left: 7px;
}
.fixHeight{
  height: 64px;
}
.adduT{
  margin-bottom: 0 !important;
}
.addu{
   height: 625px !important;
}
.btnSbmt{
  padding: 5px 10px !important;
    margin-right: 26px !important;
}
.actionMessage {
    border: 1px solid transparent;
    border-radius: 0 !important;
    width: 100% !important;
    margin: 0 !important;

}
</style>
<style type="text/css">
.cust {width: 24%!important; margin:0 5px !important; /*font: bold 10px arial !important;*/}
.samefnew{
	width: 24%!important;
    margin: 0 5px !important;
    /*font: bold 10px arial !important;*/
}
.btn {padding: 3px 7px!important; font-size: 12px!important; }
.samefnew-btn{
    width: 15%;
    float: left;
    font: bold 11px arial;
    color: #333;
    line-height: 22px;
    margin-left: 5px;
}
/*tr td.my_class{color:#000 !important; cursor: default !important; text-decoration: none;}*/
tr td.my_class{
	cursor: pointer;
}
tr td.my_class:hover{
	cursor: pointer !important;
}

tr th.my_class:hover{
	color: #fff !important;
}

.cust .form-control, .samefnew .form-control{
	margin:0px !important;
	width: 100%;
}
.select2-container{
	width: 100% !important;
}
.clearfix:after{
	display: block;
	visibility: hidden;
	line-height: 0;
	height: 0;
	clear: both;
	content: '.';
}
#popup{
	position: fixed;
	top:0px;
	left: 0px;
	background: rgba(0,0,0,0.7);
	width: 100%;
	height: 100%;
	z-index:999; 
	display: none;
}
.innerpopupDv{
	    width: 600px;
    margin: 80px auto;
    background: #fff;
    padding: 3px 10px;
    border-radius: 10px;
}
.btn-custom {
    margin-top: 5px;
    height: 27px;
    border: 1px solid #5e68ab;
    background: #5e68ab;
    padding: 5px;
    font: bold 12px Tahoma;
    color: #fff;
    cursor: pointer;
    border-radius: 5px;
}
#loader-wrapper .loader-section.section-left, #loader-wrapper .loader-section.section-right{
	background: rgba(225,225,225,0.6) !important;
	width: 50% !important;
}
.invoicetable{
	float: none;
}
.innerpopupDv h2{
	    font-size: 12px;
    padding: 5px;
}
.text-class{
	text-align: center !important;
}
.odd{
	background-color: #e6e6ff !important;
}
 
</style>
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0" class="txnf">
  <tr>
    <td align="left"><h2>Add New Beneficiary</h2></td>
  </tr>
  
  
  
  <s:if test="%{responseObject.responseCode=='000'}">
   <tr>
    <td align="left" valign="top"><div id="saveMessage">
        <s:actionmessage class="success success-text" />
      </div></td>
  </tr>
  
  </s:if>
<s:else><div class="error-text"><s:actionmessage/></div></s:else>

<tr>
							<td align="left">
							
								<div class="container">
									<div class="form-group col-md-3 txtnew col-sm-3 col-xs-4">
									
									    <s:select headerKey="ALL" headerValue="Select Nodal Acquirer" class="form-control"
										list="@com.paymentgateway.commons.util.AcquirerTypeNodal@values()"
										listValue="name" listKey="code" name="acquirer"
										id="acquirer" autocomplete="off" value=""/>
								</div>
								</div>
							</td>
						</tr>
						
  <tr>
    <td align="left" valign="top"><div class="addu">
        <div >
          <s:token/>
          <div class="adduT">Cust Id<span style="color:red; margin-left:3px;">*</span><br>
            <s:textfield name="custId" id = "custId" cssClass="signuptextfield" autocomplete="off" onkeypress="return checkInput(event);"/>
            </div>
           
			 <div class="adduT">Nodal Account No<span style="color:red; margin-left:3px;">*</span><br>
              <s:textfield name="srcAccountNo" id="srcAccountNo" cssClass="signuptextfield" autocomplete="off" onkeypress="return Validate(event);"/>
            </div>
			
            <div class="adduT">Beneficiary Cd<span style="color:red; margin-left:3px;">*</span><br>
              <s:textfield	name="beneficiaryCd" id = "beneficiaryCd" cssClass="signuptextfield" autocomplete="off" onkeypress="return checkInput(event);"/>
            </div>
           
    
			
			<div class="adduT">Beneficiary Name<span style="color:red; margin-left:3px;">*</span><br>
              <s:textfield name="beneName" id="beneName" cssClass="signuptextfield" autocomplete="off" onkeypress="return checkName(event);"/>
            </div>
          
			
			<div class="adduT">Beneficiary Account No<span style="color:red; margin-left:3px;">*</span><br>
              <s:textfield name="beneAccountNo" id="beneAccountNo" cssClass="signuptextfield" autocomplete="off" onkeypress="return Validate(event);"/>
            </div>
           
			<div class="adduT">IFSC Code<span style="color:red; margin-left:3px;">*</span><br>
              <s:textfield name="ifscCode" id="ifscCode" cssClass="signuptextfield" autocomplete="off" onkeypress="return checkInput(event);"/>
            </div>
          
			<div class="adduT">Bank Name<span style="color:red; margin-left:3px;">*</span><br>
              <s:textfield name="bankName" id="bankName" cssClass="signuptextfield" autocomplete="off" onkeypress="return checkName(event);"/>
            </div>
          
            <div class="adduT">Payment Type<span style="color:red; margin-left:3px;">*</span><br>
			  <s:select headerKey="" headerValue="ALL" class="form-control"
						list="@com.paymentgateway.commons.util.NodalPaymentTypes@values()"
						listValue="name" listKey="code" name="paymentType"
						id="paymentType" autocomplete="off" value="" style="margin-left:1px !important; margin-top:3px; margin-bottom:5px;"/>
            </div>
           

			 <div class="adduT">Beneficiary Type<span style="color:red; margin-left:3px;">*</span><br>
               <s:select headerKey="" headerValue="ALL" class="form-control"
						list="@com.paymentgateway.commons.util.BeneficiaryTypes@values()"
						listValue="name" listKey="code" name="beneType"
						id="beneType" autocomplete="off" value="" style="margin-left:1px !important; margin-top:3px; margin-bottom:5px;"/>
            </div>
			
			<div class="adduT">Currency Code<span style="color:red; margin-left:3px;">*</span><br>
               <s:select headerKey="" headerValue="ALL" class="form-control"
						list="@com.paymentgateway.commons.util.CurrencyTypes@values()"
						listValue="code" listKey="code" name="currencyCode"
						id="currencyCode" autocomplete="off" value="" style="margin-left:1px !important; margin-top:3px; margin-bottom:5px;"/>
            </div>

            <div class="adduT" style="padding-top:10px">
              <s:submit id="btnEditUser" name="btnEditUser" value="Save"
								type="button" cssClass="btn btn-success btn-md btnSbmt" onclick="saveBeneficiary()"> </s:submit>
            </div>
            
            <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
      

		  </div>
          </div>
          
          
          </td>
  </tr>
  <tr>
    <td align="left" valign="top">&nbsp;</td>
  </tr>
</table>

<script>
function Validate(event) {
	  var regex = /^\d+$/;
	    var key = String.fromCharCode(!event.charCode ? event.which : event.charCode);
	    if (!regex.test(key)) {
	       event.preventDefault();
	       return false;
	    }
} 
function checkInput(event) {
	  var regex = /^[0-9a-zA-Z\b]+$/;
	    var key = String.fromCharCode(!event.charCode ? event.which : event.charCode);
	    if (!regex.test(key)) {
	       event.preventDefault();
	       return false;
	    }
}  
function checkName(event) {
	  var regex = /^[a-zA-Z \b]+$/;
	    var key = String.fromCharCode(!event.charCode ? event.which : event.charCode);
	    if (!regex.test(key)) {
	       event.preventDefault();
	       return false;
	    }
}  
</script>

</body>
</html>