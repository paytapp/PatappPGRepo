<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Edit Student details</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery.js"></script>
<script src="../js/commanValidate.js"></script>

<!--  loader scripts -->
<script src="../js/loader/modernizr-2.6.2.min.js"></script>
<script src="../js/loader/main.js"></script>
<link rel="stylesheet" href="../css/loader/normalize.css" />
<link rel="stylesheet" href="../css/loader/main.css" />
<link rel="stylesheet" href="../css/loader/customLoader.css" />
<link rel="stylesheet" href="../css/subAdmin.css">

<style>
	.permission-checkboxe label{
  position: relative;
  font-size: 15px;
  color: #888888;
  font-weight: 600;
  line-height: 20px;
  display: flex;
  align-items: center;
  padding-left: 30px;
  cursor: pointer;
}

.unchecked input{
  opacity: 0;
  position: absolute;
}

.unchecked:before {
    content: "";
    width: 20px;
    height: 20px;
    background-color: #fff;
    border-radius: 3px;
    border: 3px solid #888888;
    display: inline-block;
    opacity: .5;
    box-sizing: border-box;
    left: 0;
    position: absolute;
}

.checkbox-checked:before{
  background-image: url(../image/checked.png);
    background-size: 100%;
    opacity: 1;
    border: none;
}

#frmEditUser{
	margin: 0 -10px;
}

.error-text{color:#a94442;font-weight:bold;background-color:#f2dede;list-style-type:none;text-align:center;list-style-type: none;margin-top:10px;
}.error-text li { list-style-type:none; }
#response{color:green;}
.btn:focus{
		outline: 0 !important;
  }
  .permission-checkboxe td{
    vertical-align: bottom; 
  }
  .permission-checkboxe .labelfont{
    margin-bottom: 3px;
  }
  .subadmin-btn-wrapper > div{
    text-align: center;
  }
</style>

<script language="JavaScript">	

</script>
<script type="text/javascript">	

 $(document).ready( function() {
    
	$('#btnEditUser').click(function() {	
			var answer = confirm("Are you sure you want to edit Student details?");
				if (answer != true) {
					return false;
				} else {
					
					
					document.getElementById("frmEditUser").submit();	
					$("body").removeClass("loader--inactive");
		            }
		      });	        
	   });
</script>

<style>
.btn:focus{
		outline: 0 !important;
	}
</style>

</head>
<body>	
	<table width="100%" border="0" cellspacing="0" cellpadding="0"
		class="txnf">
		<tr>
			<td align="left">
			<div class="inner-heading">
				<h3>Edit Student Details</h3>
			</div>
		</td>
		</tr>
		<tr>
			<td align="left" valign="top"><div id="saveMessage">
					<s:actionmessage class="success success-text" />
				</div></td>
		</tr>
		<tr>
			<td align="left" valign="top"><div class="w-100">
					<s:form action="editStudentDetails" id="frmEditUser">
						<div class="col-md-6">
							Id<br>
							<div class="txtnew">
								<s:textfield name="id" id="id"
									cssClass="merchant__form_control" autocomplete="off" readonly="true" />
							</div>
						</div>
						<div class="col-md-6">
							Registration No.<br>
							<div class="txtnew">
								<s:textfield name="regNo" id="regNo"
									cssClass="merchant__form_control" autocomplete="off" readonly="true" />
							</div>
						</div>
						<div class="col-md-6">
							Student Name<br>
							<div class="txtnew">
								<s:textfield name="studentName" id="studentName"
									cssClass="merchant__form_control" autocomplete="off" />
							</div>
						</div>
						<div class="col-md-6">
							FATHER_NAME<br>
							<div class="txtnew">
								<s:textfield name="fatherName" id="fatherName"
									cssClass="merchant__form_control"
									autocomplete="off" />
							</div>
						</div>
						<div class="col-md-6">
							Mobile<br>
							<div class="txtnew">
								<s:textfield name="mobile" id="mobile"
									cssClass="merchant__form_control"
									autocomplete="off" />
							</div>
						</div>
						<div class="col-md-6">
							Class<br>
							<div class="txtnew">
								<s:textfield name="standard" id="standard" cssClass="merchant__form_control" autocomplete="off" />
							</div>
						</div>


						<div class="col-md-6">
							Current Status<br>
							<div class="txtnew">
								<s:textfield name="currentStatus" id="currentStatus"
									cssClass="merchant__form_control" autocomplete="off" readonly="true" />
							</div>
						</div>
						
						<div class="col-md-6">
							Change Status<br>
							<div class="txtnew">
										<s:select headerKey="" headerValue="Change Status" class="merchant__form_control"
											list="#{'ACTIVE':'ACTIVE','INACTIVE':'INACTIVE'}" name="status" id="status" value="name"
											lautocomplete="off" />
									</div>
						</div>
						
						

						<div class="adduT subadmin-btn-wrapper" style="padding-top: 10px">
							<s:submit id="btnEditUser" name="btnEditUser" value="Save"
								type="button" cssClass="primary-btn">
							</s:submit>
						</div>
						<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
					</s:form>
					<div class="clear"></div>
				
			</div>
			<!-- /.permission-div --></td>
		</tr>
		<tr>
			<td align="left" valign="top">&nbsp;</td>
		</tr>
	</table>


</body>
</html>