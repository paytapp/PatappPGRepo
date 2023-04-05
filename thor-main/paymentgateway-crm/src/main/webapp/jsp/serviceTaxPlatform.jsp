<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>GST Platform</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<link rel="stylesheet" type="text/css" href="../css/popup.css" />
<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />

<script type="text/javascript">

$(document).ready(function() {
	
	   $('#businessType').change(function(event){
		   $("body").removeClass("loader--inactive");
		   var businessType = document.getElementById("businessType").value;
		   

		   if( businessType==null||businessType=="" ){
			   document.getElementById("datatable").style.display="none";
			  return false;
			  
			 }
		   else{
			   document.getElementById("datatable").style.display="block";
			   
		   }
		   document.getElementById("serviceTaxDetailsForm").submit();
		   
	   });
	   
	   var cancelButton = document.getElementById("cancelBtn1");
	   cancelButton.disabled = true;

});

var editMode;

function editCurrentRow(divId,curr_row,ele){
	
	 var cancelButton = document.getElementById("cancelBtn1");
	   cancelButton.disabled = false;
	   
	var div = document.getElementById(divId);

	var table = div.getElementsByTagName("table")[0];

	var businessType = document.getElementById("businessType").value;
	var userType = "<s:property value='%{#session.USER.UserType.name()}'/>";
	var loginEmailId = "<s:property value='%{#session.USER.EmailId}'/>";
	
	var rows = table.rows;
	var currentRowNum = Number(curr_row);
	var currentRow = rows[currentRowNum];
	var cells = currentRow.cells;
	var cell0 = cells[0];
	var cell1 = cells[1];
	var cell2 = cells[2];
	var cell3 =  cells[3];
	
	var cell0Val = cell0.innerText.trim();
	var cell1Val = cell1.innerText.trim();
	var cell2Val = cell2.innerText.trim();
	var cell3Val = cell3.innerText.trim();

	if(ele.value=="Edit"){
		if(editMode) 
		{
				alert('Please edit the current row to proceed');
				return;
		}
		ele.value="save";
		ele.className ="btn btn-success btn-xs";
		cell2.innerHTML = "<input type='number' id='cell2Val'   class='serviceTaxPlatfrom' min='1' step='0.0' value="+cell2Val+"></input>";
		editMode = true;
	}
	else{
		var businessType = cell1Val;
		var serviceTax = document.getElementById('cell2Val').value;
		var status = cell3Val;

		cell1.innerHTML = businessType;
		cell2.innerHTML = serviceTax;
		cell3.innerHTML = status;
		editMode = false;
		$("body").removeClass("loader--inactive");

		ele.value="Edit";
		ele.className ="btn btn-info btn-xs";
		var stl=parseInt(serviceTax);
		
		
		if(serviceTax=="")
		{

			serviceTax="0.00";
			cell2.innerHTML = serviceTax;
			
		}
		
		if(serviceTax <= -1) {
		
				alert('Please enter valid value, negative value not accepted in service tax');
				window.location.reload();
		
			return;
		}
		
		if(serviceTax=="") {
			
			alert('Please enter valid value, Blank value not accepted in service tax');
            window.location.reload();
	
		  return;
	    }
		var token  = document.getElementsByName("token")[0].value;
		$.ajax({
			type: "POST",
			url:"editServiceTax",
			timeout: 0,
			data:{"businessType":cell1Val, "serviceTax":serviceTax, "status":status, "userType":userType, "loginEmailId":loginEmailId, "token":token,"struts.token.name": "token",},
			success:function(data){
				var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
				if(null!=response){
					alert(response);			
				}
				//TODO....clean values......using script to avoid page refresh
				window.location.reload();
		    },
			error:function(data){
				alert("Network error, GST may not be saved");
			}
		});
	}
}


function cancel(curr_row,ele){
	var parentEle = ele.parentNode;
	
	if(editMode){
		$("body").removeClass("loader--inactive");
	 	window.location.reload();
	}
}


</script>
<style>
.product-spec input[type=text] {
	width: 35px;
}
.boxtext{
	margin-bottom: 15px !important;
}
.btn:focus{
		outline: 0 !important;
}
</style>
</head>
<body>
<s:actionmessage class="error error-new-text" />

	<s:form id="serviceTaxDetailsForm" action="serviceTaxPlatformAction"
		method="post">
		<table width="100%" border="0" cellspacing="0" cellpadding="0"
			class="txnf" style="margin-top:1%;">
			<tr>
				<td width="21%"><h2>Goods and Services Tax</h2></td>
			</tr>
			<tr>
				<td align="center" valign="top"><table width="98%" border="0"
						cellspacing="0" cellpadding="0">
						<tr>
							<td align="left">
								<div class="container">
									<div class="form-group col-md-4 txtnew col-sm-4 col-xs-6">
									
									<s:select headerKey=""
											headerValue="Select Industry" name="businessType" 
								id="businessType" list="industryCategoryList" class="form-control" autocomplete="off" style="margin-left:-3% !important;" />
								</div>
								</div>
							</td>
						</tr>
						<tr>
							<td align="left"><div id="datatable" class="scrollD">
									<s:iterator value="serviceTaxData" status="pay">
										<br>
										<span class="text-primary" id = "test"><strong><s:property
												value="key" /></strong></span>
										<br>
										<div class="scrollD">
											<s:div id="%{key +'Div'}" value = "key">
												<table width="100%" border="0" align="center"
													class="product-spec">
													<tr class="boxheading">
														<th width="5%" height="25" valign="middle"
															style="display: none">Payment</th>
														<th width="4%" align="left" valign="middle">Business Type</th>	
														<th width="6%" align="left" valign="middle">GST</th>
														<th width="4%" align="left" valign="middle">Status</th>
															 <th width="5%" align="left" valign="middle">Update</th>
														<th width="2%" align="left" valign="middle"
															style="display: none">id</th>
														<th width="5%" align="left" valign="middle"><span
															id="cancelLabel">Cancel</span></th>
													</tr>
													<s:iterator value="value" status="itStatus">
														<tr class="boxtext">
															<td align="left" valign="middle" style="display: none"><s:property
																	value="businessType" /></td>
															<td align="left" valign="middle"><s:property
																	value="businessType" /></td>
															<td align="left" valign="middle"><s:property
																	value="serviceTax" /></td>
																<td align="left" valign="middle"><s:property
																	value="status" /></td>
																
																	<td align="center" valign="middle"><s:div>
																	<s:textfield id="edit%{#itStatus.count}" value="Edit"
																		type="button"
																		onclick="editCurrentRow('%{key +'Div'}','%{#itStatus.count}', this)"
																		class="btn btn-info btn-xs" autocomplete="off"></s:textfield>

																	<%-- <s:textfield id="cancelBtn%{#itStatus.count}"
																		value="Cancel" type="button"
																		onclick="cancel('%{#itStatus.count}',this)"
																		style="display:none" autocomplete="off"></s:textfield> --%>
																</s:div></td>
															<td align="center" valign="middle" style="display: none"><s:property
																	value="id" /></td>
															<td align="center" valign="middle"><s:textfield
																	id="cancelBtn%{#itStatus.count}" value="Cancel"
																	type="button"
																	onclick="cancel('%{#itStatus.count}',this)"
																	class="btn btn-danger btn-xs" autocomplete="off"></s:textfield></td>
														</tr>
													</s:iterator>
												</table>
											</s:div>
										</div>
									</s:iterator>
								</div></td>
						</tr>
					</table></td>
			</tr>
			
			
		</table>
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>
	
</body>
</html>