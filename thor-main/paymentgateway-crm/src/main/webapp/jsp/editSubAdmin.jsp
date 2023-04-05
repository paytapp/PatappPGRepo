<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Modify SubAdmin</title>
<link rel="icon" href="../image/favicon-32x32.png">
<!-- <link href="../css/default.css" rel="stylesheet" type="text/css" /> -->
<script type="text/javascript" src="../js/jquery-latest.min.js"></script>

<link rel="stylesheet" href="../css/login.css">
<!-- <link rel="stylesheet" href="../css/subAdmin.css"> -->

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

  .permissionsDiv{
  flex-wrap: wrap;
}

.bg-grey{
  background-color: #f5f5f5;
}

.bg-dark-grey{
  background-color: #dfdfdf;
}

.permissionBox {
    padding: 20px;
    padding-bottom: 0px;
    border-radius: 5px;
    margin-bottom: 20px;
    position: relative;
}

.permissionBox h3{
    padding-bottom: 10px;
    border-bottom: 1px solid #ccc;
    margin-bottom: 15px !important;
    font-size: 16px !important;
}

.permissionsDiv label{
  margin-right: 20px;
}

.permissionHeading {
    cursor: pointer;
    position: absolute;
    right: 20px;
    top: 20px;
    cursor: pointer;
}

.button-wrapper{
	position: absolute;
	width: auto;
	display: inline-block;
	top: 9px;
	right: 0;
}

.button_wrapper a:hover{
	color: #fff;
}

</style>

<script language="JavaScript">	
$(document).ready( function () {
	handleChange();
});
	function handleChange(){

   	  var str = '<s:property value="permissionString"/>';
   	  var permissionArray = str.split("-");
   	  for(j=0;j<permissionArray.length;j++){
			selectPermissions(permissionArray[j]);
		}
    }
	
	function selectPermissions(permissionType){		
		var permissionCheckbox = document.getElementById(permissionType);
		if(permissionCheckbox==null){
			return;
		}
		permissionCheckbox.checked = true;
	}
</script>
<script type="text/javascript">	

 $(document).ready( function() {
    
	$('#btnEditUser').click(function() {	
		var answer = confirm("Are you sure you want to edit user details?");
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
	<section class="edit-subadmin lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row">
			<div class="col-md-12">

				<s:form action="editAgentDetails" id="frmEditUser">
	
				
					<div class="col-md-12">
						<div class="heading_with_icon mb-30">
							<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
							<h2 class="heading_text">Edit Sub-admin</h2>
						</div>
						<!-- /.heading_icon -->
					</div>
					<!-- /.col-md-12 -->
					<s:if test="%{responseObject.responseCode=='000'}">
					<div class="col-md-12" id="saveMessage">
					<s:actionmessage class="success success-text" />
					</div>
					<!-- /.col-md-12 -->
				</s:if>
				<s:else>
					<div class="col-md-12">
					<span class="empty-data mt-20 d-none">
						<s:actionmessage/>
					</span> <!-- /.noData -->
					</div>
					<!-- /.col-md-12 -->
					<!-- <div class="error-text"></div> -->
				</s:else>
				<div class="col-md-6 mb-20 track-scroll">
					<div class="lpay_input_group">
					<label for="">First Name</label>
					<s:textfield name="firstName" id = "fname" cssClass="lpay_input acquirer-input" autocomplete="off" />
					
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-6 -->
				<div class="col-md-6 mb-20">
					<div class="lpay_input_group">
					<label for="">Last Name <span>*</span></label>
					<s:textfield	name="lastName" id = "lname" cssClass="lpay_input acquirer-input" autocomplete="off" />
					
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 -->
				<div class="col-md-6 common-validation mb-20">
					<div class="lpay_input_group">
					<label for="">Mobile Number <span>*</span></label>
					<div class="position-relative">
						<s:textfield name="mobile" id="phoneNumber" readonly="true" onkeypress="onlyDigit(event)" cssClass="lpay_input acquirer-input" autocomplete="off" maxlength="10"/>
						<img src="../image/right-tick.png" alt="/" class="right-tick status-img">
						<img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
					</div>
					<!-- /.position-relative -->
					</div>
				</div>
				<!-- /.col-md-6 -->
				<div class="col-md-6 common-validation mb-20">
					<div class="lpay_input_group">
					<label for="">Email <span>*</span></label>
					<div class="position-relative">
						<s:textfield name="emailId" id="emailId" readonly="true" cssClass="lpay_input acquirer-input" autocomplete="off" />
						<img src="../image/right-tick.png" alt="/" class="right-tick status-img">
								<img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
					</div>
					<!-- /.position-relative -->
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 -->
				<div class="col-md-12">
					<div class="permissionBox bg-dark-grey" style="padding-bottom: 20px;">
						
						<div class="permissionDiv w-100 flex-wrap d-flex">
							<label class="checkbox-label unchecked" style="margin-bottom: 0;" for="1">Is Active ?
								<s:checkbox name="isActive" id="isActive" class="common" autocomplete="off"></s:checkbox>
							  </label>
						</div>
						<!-- /.permissionDiv w-100 flex-wrap d-flex -->
					</div>
					<!-- /.permissionBox bg-grey-dark -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12">
					<div class="permissionBox bg-grey">
					<div class="w-100 mb-10">
						<h3>Analytics</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="analyticsPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12">
					<div class="permissionBox bg-dark-grey">
					<div class="w-100 mb-10">
						<h3>Merchant Setup</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv flex-wrap w-100 d-flex">
						<s:iterator value="merchantSetupPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
				
				<div class="col-md-12">
					<div class="permissionBox bg-grey">
					<div class="w-100 mb-10">
						<h3>Merchant Configurations</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="merchantConfiguratiomsPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-dark-grey">
					<div class="w-100 mb-10">
						<h3>Reseller</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="resellerPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-grey">
					<div class="w-100 mb-10">
						<h3>View Configuration</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="viewConfigurationPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-dark-grey">
					<div class="w-100 mb-10">
						<h3>Quick Search</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="quickSearchPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-grey">
					<div class="w-100 mb-10">
						<h3>Reporting</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="reportingPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-dark-grey">
					<div class="w-100 mb-10">
						<h3>Vendor Payout</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="vendorPayoutPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-grey">
					<div class="w-100 mb-10">
						<h3>Quick Pay</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="quickPayPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-dark-grey">
					<div class="w-100 mb-10">
						<h3>School Fee Manager</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="schoolFeeManagerPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-grey">
					<div class="w-100 mb-10">
						<h3>Batch Operations</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="batchOperationsPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-dark-grey">
					<div class="w-100 mb-10">
						<h3>Banking</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="disbursementsPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->

				<div class="col-md-12">
					<div class="permissionBox bg-grey">
					<div class="w-100 mb-10">
						<h3>Beneficiary Verification</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="accountVerificationPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-dark-grey">
					<div class="w-100 mb-10">
						<h3>Fraud Prevention</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="fraudPreventionPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-grey">
					<div class="w-100 mb-10">
						<h3>Manage Users</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="manageUsersPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-dark-grey">
					<div class="w-100 mb-10">
						<h3>Manage Acquirers</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="manageAcquirersPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-grey">
					<div class="w-100 mb-10">
						<h3>Manage Issuers</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="manageIssuersPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-dark-grey">
					<div class="w-100 mb-10">
						<h3>Agent Access</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="agentAccessPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				<!-- /.col-md-12 -->
	
				<div class="col-md-12">
					<div class="permissionBox bg-grey">
					<div class="w-100 mb-10">
						<h3>Chargeback</h3>
						<span class="permissionHeading">Select All</span>
					</div>
					<!-- /.w-100 mb-20 -->
					<div class="permissionsDiv w-100 flex-wrap d-flex">
						<s:iterator value="chargebackPermission" status="itStatus">
						<label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
							<s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
						</label>
						</s:iterator>
					</div>
					<!-- /.permissionsDiv w-100 -->
					</div>
					<!-- /.permissionBox -->
				</div>
				 <div class="col-md-12">
            <div class="permissionBox bg-dark-grey">
              <div class="w-100 mb-10">
                <h3>MSEDCL</h3>
                <span class="permissionHeading">Select All</span>
              </div>
              <!-- /.w-100 mb-20 -->
              <div class="permissionsDiv w-100 flex-wrap d-flex">
                <s:iterator value="msedclPermission" status="itStatus">
                  <label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
                    <s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
                  </label>
                </s:iterator>
              </div>
              <!-- /.permissionsDiv w-100 -->
            </div>
            <!-- /.permissionBox -->
          </div>

		  <div class="col-md-12">
            <div class="permissionBox bg-grey">
              <div class="w-100 mb-10">
                <h3>Subscription</h3>
                <span class="permissionHeading">Select All</span>
              </div>
              <!-- /.w-100 mb-20 -->
              <div class="permissionsDiv w-100 flex-wrap d-flex">
                <s:iterator value="eNachPermission" status="itStatus">
                  <label class="checkbox-label unchecked mb-20" for="1"><s:property value="permission" />
                    <s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
                  </label>
                </s:iterator>
              </div>
              <!-- /.permissionsDiv w-100 -->
            </div>
            <!-- /.permissionBox -->
          </div>
				<!-- /.col-md-12 -->
				<div class="col-md-12 text-center button-wrapper button_wrapper">
					<s:a class="lpay_button lpay_button-md lpay_button-primary" action='searchSubAdmin'>Back</s:a>
					<button id="btnEditUser" class="lpay_button lpay_button-md lpay_button-secondary">Submit</button>
				</div>
				<!-- /.col-md-12 -->
				<s:hidden name="payId" id="payId" />
				<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
			</s:form>
			</div>
			<!-- /.col-md-12 -->
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
	

<script>
	 
	 
	 $(document).ready(function(){

		var _check = true;
    $(".permissionHeading").on("click", function(e){
      if(_check == true){
        $(this).closest(".col-md-12").find("label").addClass("checkbox-checked");
        $(this).closest(".col-md-12").find("input[type='checkbox']").prop("checked", true);
        $(this).text("Deselect All");
        _check = false;
      }else{
        $(this).closest(".col-md-12").find("label").removeClass("checkbox-checked");
        $(this).closest(".col-md-12").find("input[type='checkbox']").prop("checked", false);
        $(this).text("Select All");
        _check = true;
      }
    })

	var _getTab = document.querySelector(".track-scroll");
    var _getX = _getTab.getBoundingClientRect().y;
    window.addEventListener('scroll', function(e) {
        var _getWindow = window.scrollY;
        console.log(_getWindow);
        if(_getWindow > _getX){
            document.querySelector(".button_wrapper").classList.remove("button-wrapper");
        }else{
            document.querySelector(".button_wrapper").classList.add("button-wrapper");
        }
    });

		if($("input[value='Approve MPA']").is(":checked")){
			$("input[value='Review MPA']").attr("disabled", true);
			}
			if($("input[value='Review MPA']").is(":checked")){
			$("input[value='Approve MPA']").attr("disabled", true);
			}

		// set id to label for click label and checked
		$(".checkbox-label").each(function(e){
		var _getId = $(this).find("input[type='checkbox']").attr("id");
		$(this).attr("for", _getId);
		if($(this).find("input[type='checkbox']").is(":checked")){
			$(this).addClass("checkbox-checked");
		}
		});

		// add class to on changed 
		$(".checkbox-label input").on("change", function(e){
		if($(this).is(":checked")){
			$(this).closest("label").addClass("checkbox-checked");
		}else{
			$(this).closest("label").removeClass("checkbox-checked");
		}
		})

		// subadmin condition
		$("input[value='Approve MPA']").on("change", function(e){
		if($(this).is(":checked")){
			$("input[value='Review MPA']").attr("disabled", true);
		}else{
			$("input[value='Review MPA']").attr("disabled", false);
		}
		});
		$("input[value='Review MPA']").on("change", function(e){
		if($(this).is(":checked")){
			$("input[value='Approve MPA']").attr("disabled", true);
		}else{
			$("input[value='Approve MPA']").attr("disabled", false);
		}
		})
	})
</script>

</body>
</html>