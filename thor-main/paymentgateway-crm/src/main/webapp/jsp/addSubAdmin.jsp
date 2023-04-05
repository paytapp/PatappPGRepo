<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Add SubAdmin</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script type="text/javascript" src="../js/jquery-latest.min.js"></script>
<!-- <link rel="stylesheet" href="../css/subAdmin.css"> -->
<link rel="stylesheet" href="../css/login.css">

<script language="JavaScript">	
$(document).ready( function () {
  // handleChange();
  var _checkbox = false;
	$('#btnEditUser').click(function() {	
    var _getAllCheckbox = document.querySelectorAll(".common");
    _getAllCheckbox.forEach(function(element, index, array){
      if(element.checked){
        _checkbox = true;
      }
    })
    if(_checkbox == true){
      var answer = confirm("Do you want to create Sub-admin ?");
			if (answer != true) {
				return false;
			} else {
				
				
				if ((document.getElementById("Create Merchant Mapping").checked) || (document.getElementById("Create Surcharge").checked) || (document.getElementById("Create TDR").checked) ||	(document.getElementById("Create Service Tax").checked) ||	(document.getElementById("Create Merchant Mapping").checked) ||
				(document.getElementById("Create Reseller Mapping").checked)){
					
					if (!document.getElementById("View Merchant Billing").checked){
	
						alert ("View Merchant Billing is a required permission for permissions Create Merchant Mapping , Create Surcharge , Create TDR , Create Service Tax , Create Merchant Mapping or Create Reseller Mapping . Please Select View Merchant Billing to continue");
						return false;
					}
				}
				
				if (document.getElementById("Edit Merchant Details").checked){
							
							if (!document.getElementById("View MerchantSetup").checked){
			
								alert ("View MerchantSetup is a required permission for permission Edit Merchant Details. Please Select View MerchantSetup to continue");
								return false;
							}
						}
				
				if (document.getElementById("Create Invoice").checked){
							
							if (!document.getElementById("View Invoice").checked){
			
								alert ("Create Invoice is a required permission for permission Create Invoice. Please Select View Invoice to continue");
								return false;
							}
						}
						
            $("body").removeClass("loader--inactive");
				
				document.getElementById("frmEditUser").submit();
    }
	  }else{
      alert("Please select any permission");
      return false;
    }
	      });
      });
	function handleChange(){

   	  var str = '<s:property value="permissionString"/>';
   	  var buttonFlag = '<s:property value="disableButtonFlag"/>';
   	  if(buttonFlag=='true'){
   		 document.getElementById('submit').style.display='none';
   	  }
      
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
<style type="text/css">


div#saveMessage li {
    text-align: center;
    background-color: #47dd4b73;
    border-radius: 5px;
    margin-bottom: 20px;
}

#saveMessage{
  background-color: transparent !important;
}


.permissionHeading {
    cursor: pointer;
    position: absolute;
    right: 20px;
    top: 20px;
    cursor: pointer;
}

div#saveMessage li span{
  text-align: center;
}

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

.checkbox-checked:before{
  background-image: url(../image/checked.png);
    background-size: 100%;
    opacity: 1;
    border: none;
}

.position-relative{
  position: relative;
}

.error-text{color:#a94442;font-weight:bold;background-color:#f2dede;list-style-type:none;text-align:center;list-style-type: none;margin-top:10px;
}.error-text li { list-style-type:none; }
#response{color:green;}

.error-subadmin {
    position: absolute;
    top: 0;
    right: 10px;
    color: #f00;
    display: none;
}

.show{
  display: block;
}

.error-subadmin p{
  margin: 0;
  color: #f00;
}

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
</head>
<body class="bodyColor">

  <section class="add-subadmin lapy_section white-bg box-shadow-box mt-70 p20">
    <s:form action="addSubAdmin" id="frmAddUser" >
      <div class="row">
          <div class="col-md-12">
              <div class="heading_with_icon mb-30">
                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                  <h2 class="heading_text">Add Sub-Admin</h2>
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
          <div class="col-md-6 mb-20">
            <div class="lpay_input_group">
              <label for="">First Name</label>
              <s:textfield name="firstName" onkeypress="onlyLetters(event)" id = "fname"  cssClass="lpay_input acquirer-input" autocomplete="off" />
              <div class="error-name error-subadmin">
                <p>Please Enter First Name</p>
              </div>
              <!-- /.error-name -->
            </div>
            <!-- /.lpay_input_group -->
          </div>
          <!-- /.col-md-6 -->
          <div class="col-md-6 mb-20">
            <div class="lpay_input_group">
              <label for="">Last Name <span>*</span></label>
              <s:textfield	name="lastName" onkeypress="onlyLetters(event)" id = "lname" cssClass="lpay_input acquirer-input" autocomplete="off" />
              <div class="error-last error-subadmin">
                <p>Please Enter Last Name</p>
              </div>
              <!-- /.error-name -->
            </div>
            <!-- /.lpay_input_group -->
          </div>
          <!-- /.col-md-3 -->
          <div class="col-md-6 common-validation mb-20">
            <div class="lpay_input_group">
              <label for="">Mobile Number <span>*</span></label>
              <div class="position-relative">
                <s:textfield name="mobile" id="phoneNumber" onkeypress="onlyDigit(event)" cssClass="lpay_input acquirer-input" autocomplete="off" maxlength="10"/>
                <img src="../image/right-tick.png" alt="/" class="right-tick status-img">
                <img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
              </div>
              <!-- /.position-relative -->
            </div>
            <!-- /.lpay_input_group -->
            <div class="error-mobile error-subadmin">
              <p>Please Enter Valid Mobile Number</p>
            </div>
            <!-- /.error-name -->
          </div>
          <!-- /.col-md-6 -->
          <div class="col-md-6 common-validation mb-20">
            <div class="lpay_input_group">
              <label for="">Email <span>*</span></label>
              <div class="position-relative">
                <s:textfield name="emailId" id="emailId" cssClass="lpay_input acquirer-input" autocomplete="off" />
                <img src="../image/right-tick.png" alt="/" class="right-tick status-img">
                          <img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
              </div>
              <!-- /.position-relative -->
              <div class="error-email error-subadmin">
                <p>Please Enter Valid Email Id</p>
              </div>
              <!-- /.error-name -->
            </div>
            <!-- /.lpay_input_group -->
          </div>
          <!-- /.col-md-3 -->
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
                <h3>Disbursements</h3>
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
                <h3>Customer Operations</h3>
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
                <s:iterator value="subscriptionPermission" status="itStatus">
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

        <div class="col-md-12 submit-btn-wrapper text-center">
          <input type="submit" class="lpay_button lpay_button-md lpay_button-secondary" id="btnEditUser" value="Save">
          
        </div>
        <!-- /.col-md-12 -->
      </div>
      <!-- /.row -->
      <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
    </s:form>
  </section>
  <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->


<script type="text/javascript">

function onlyDigit(event){
    var x = event.keyCode;
    if (x > 47 && x < 58 || x == 32) {
    } else {
        event.preventDefault();
    }
}

// only letters
function onlyLetters(event) {
    var x = event.keyCode;
    if (x > 64 && x < 91 || x > 96 && x < 123 || x == 32) {
        
    } else {
        event.preventDefault();
    }
}

  $(document).ready(function(){

    $("input[value='Pending Requests']").closest("label").addClass("d-none");

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

    var _button = document.querySelector("#btnEditUser");
    _button.onClick = function(){
      return false;
    }


    $(".acquirer-input").val("");
    var _checkClass = false;
    function checkBlankField(){
      $(".acquirer-input").each(function(e){
        
        var _thisVal = $(this).val();
        if(_thisVal == ""){
          _checkClass = false;
          $("#btnEditUser").attr("disabled", true);
          return false;
        }else{
          _checkClass = true;
          $("#btnEditUser").attr("disabled", true);
        }
      });
      if(_checkClass == true){
        if($(".error-subadmin.show").length == 0){
          $("#btnEditUser").attr("disabled", false);
        }else{
          $("#btnEditUser").attr("disabled", true);
        }
      }else{
        $("#btnEditUser").attr("disabled", true);
      }
    }
  
    $(".acquirer-input").on("change", checkBlankField);

    //error msg show

    $(".acquirer-input").on("blur", function(e){
      var _thisVal = $(this).val();
      if(_thisVal.length > 0){
      }else{
        $(this).closest(".col-md-6").find(".error-subadmin").addClass("show");
      }
    });
  
  
    $(".acquirer-input").on("keyup", function(e){
      $(this).closest(".col-md-6").find(".error-subadmin").removeClass("show");
    });

    function mailId(url, dataId, successMsg, param){
      $("body").removeClass("loader--inactive");
      var _parent = $(dataId);
      var emailId = $(dataId).val();
      var dataObj = {};
      dataObj[param] = emailId;
      $.ajax({
          type: "post",
          url: url,
          data: dataObj,
          success: function(data){
            
            if(data[successMsg] == "success"){
                $(_parent).attr("readonly", true);
                $(_parent).closest(".common-validation").removeClass("verify-denied");
                $(_parent).closest(".common-validation").addClass("verify-success");
                $(_parent).closest(".common-validation").find(".error-subadmin").removeClass("show");
                checkBlankField();
                $("body").addClass("loader--inactive");
            }else{
              $(_parent).closest(".common-validation").find(".error-subadmin p").text(data.response);
              $(_parent).closest(".common-validation").find(".error-subadmin").addClass("show");
              $(_parent).closest(".common-validation").addClass("verify-denied");
              checkBlankField();
              $("body").addClass("loader--inactive");
            }
          },
          error: function(data){
          }
      });
    }

    function isValidEmail() {
        var emailexp = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/;
        var emailValue = document.getElementById("emailId").value;
          if (emailValue.trim() && emailValue.match(emailexp)) {
              return true;
          } else{
            //   document.getElementById("emailError").style.display = "block";
        }
    }
  
    $("#phoneNumber").on("blur", function(e){
      var _getVal = $(this).val();
      if(_getVal.length < 9){
        $("#phoneNumber").closest(".common-validation").find(".fixHeight p").text("Please Enter Valid Mobile Number.");
        $("#phoneNumber").closest(".common-validation").find(".fixHeight").addClass("show");
      }
    });
  
    $("#phoneNumber").on("keyup", function(e){
      var _getVal = $(this).val();
      $(this).closest(".common-validation").removeClass("verify-denied");
      $("#phoneNumber").closest(".common-validation").find(".fixHeight").removeClass("show");
      if(_getVal.length > 9){
        mailId("mobileNumberValidate", "#phoneNumber", "phoneSuccessStatus", "phoneNumber");
      }
    });
  
    $("#emailId").on("blur", function(){
      if(isValidEmail(true)){
        mailId("emailAction", "#emailId", "emailSuccessStatus", "emailId");
      }else{
        $(this).closest(".common-validation").find(".error-subadmin").addClass("show");
      }
    });
  
    $("#emailId").on("keyup", function(e){
        $(this).closest(".common-validation").removeClass("verify-denied");
        $("#emailId").closest(".common-validation").find(".error-subadmin").removeClass("show");
    });

    $("input[value='Create Reseller Mapping']").closest(".col-md-4").addClass("d-none");
    $("input[value='View Merchant Details']").closest(".col-md-4").addClass("d-none");
    $("input[value='Create Bulk User']").closest(".col-md-4").addClass("d-none");
    $("input[value='Void/Refund']").closest(".col-md-4").addClass("d-none");
    $("input[value='View Remittance']").closest(".col-md-4").addClass("d-none");
    $("input[value='Create Surcharge']").closest(".col-md-4").addClass("d-none");
    $("input[value='Create Service Tax']").closest(".col-md-4").addClass("d-none");
    

    //set id to label
    if($("input[value='Approve MPA']").is(":checked")){
        $("input[value='Review MPA']").attr("disabled", true);
    }
    if($("input[value='Review MPA']").is(":checked")){
      $("input[value='Approve MPA']").attr("disabled", true);
    }

    if($("input[value='View Reseller Revenue']").is(":checked")){
      $("input[value='View Analytics']").attr("disabled", false);
    }else{
      $("input[value='View Analytics']").attr("disabled", true);
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
      var _getValue = $(this).val();
      if($("input[value='View Reseller Revenue']").is(":checked")){
        $("input[value='View Analytics']").attr("disabled", false);
      }else{
        $("input[value='View Analytics']").prop("checked", false);
        $("input[value='View Analytics']").closest("label").removeClass("checkbox-checked");
        $("input[value='View Analytics']").attr("disabled", true);
      }
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