<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Modify Acquirer</title>
<link rel="icon" href="../image/favicon-32x32.png">
<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
<script src="../js/jquery.js"></script>
<script src="../js/commanValidate.js"></script>
<!--  loader scripts -->

<script type="text/javascript">	
	$(document).ready( function() {    
		$('#btnEditUser').click(function() {	
			var answer = confirm("Are you sure you want to edit acquirer details?");
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

	div#saveMessage{
		pointer-events: none;
	}

	div#saveMessage li {
		text-align: center;
		background-color: #47dd4b73;
		border-radius: 5px;
		margin-bottom: 20px;
	}

	div#saveMessage li span{
		text-align: center;
	}
</style>

</head>
<body>	

	<section class="edit-agent lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
				  <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Edit Acquirer</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<div class="col-md-12 mb-20">
			  <div id="saveMessage">
				<s:actionmessage class="success success-text" />
			  </div>
			</div>
			<!-- /.col-md-12 -->
			<s:form action="editAcquirerDetails" id="frmEditUser" >
			  <div class="col-md-6 mb-20">
				<div class="lpay_input_group">
				  <label for="">First Name <span style="color:red; margin-left:3px;">*</span></label>
				  <s:textfield	name="firstName" id="firstName" cssClass="lpay_input" autocomplete="off" />
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-4 -->
			  <div class="col-md-6 mb-20">
				<div class="lpay_input_group">
				  <label for="">Last Name<span style="color:red; margin-left:3px;">*</span></label>
				  <s:textfield	name="lastName" id="lastName" cssClass="lpay_input" autocomplete="off"/>
	
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-4 -->
			  <div class="col-md-6 mb-20">
				<div class="lpay_input_group">
				  <label for="">Business Name</label>
				  <s:textfield name="businessName" id="businessName"
				  cssClass="lpay_input" autocomplete="off" />
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-4 -->
			  <div class="col-md-6 mb-20">
				<div class="lpay_input_group">
				  <label for="">Email</label>
				  <s:textfield name="emailId" id="emailAddress" cssClass="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-4 -->
			  <div class="col-md-6 mb-20">
				<div class="lpay_input_group">
				  <label for="">Account No</label>
				  <s:textfield name="accountNo" id="accountNo"
					cssClass="lpay_input" autocomplete="off" readonly="true" />
				</div>
				<!-- /.lpay_input_group -->
			  </div>
			  <!-- /.col-md-4 -->
			  <div class="col-md-12 text-center">
				<button id="btnEditUser" name="btnEditUser" class="lpay_button lpay_button-md lpay_button-secondary">Save Acquirer</button>
			  <!-- <s:submit id="btnEditUser" name ="btnEditUser" value="Save" type="button" cssClass="btn btn-success btn-md"> </s:submit> -->
			  </div>
			  <!-- /.col-md-12 -->
			  <s:hidden name="payId" id="payId"  /> 
		   	  <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
			</s:form>
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

</body>
</html>