<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Add Khadi Vendor</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" href="../css/login.css">
<link rel="stylesheet" href="../css/subAdmin.css">
<script src="../js/jquery.minshowpop.js"></script>
<script src="../js/jquery.formshowpop.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/bootstrap-select.min.js"></script>
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script language="JavaScript">	
$(document).ready( function () {
	handleChange();
});
	function handleChange(){

   	  var str = '<s:property value="permissionString"/>';
   	  var buttonFlag = '<s:property value="disableButtonFlag"/>';
   	 /*  if(buttonFlag=='true'){
   		 document.getElementById('submit').style.display='none';
   	  } */
   	
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

.position-relative{
  position: relative;
}

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

.mb-20{
  margin-bottom: 20px;
}


.error-text{color:#a94442;font-weight:bold;background-color:#f2dede;list-style-type:none;text-align:center;list-style-type: none;margin-top:10px;
}.error-text li { list-style-type:none; }
#response{color:green;}
</style>
</head>
<body>

  <section class="add-user lapy_section white-bg box-shadow-box mt-70 p20">
      <div class="row">
          <div class="col-md-12">
              <div class="heading_with_icon mb-30">
                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                  <h2 class="heading_text">Add User</h2>
              </div>
              <!-- /.heading_icon -->
          </div>
          <!-- /.col-md-12 -->
          <div class="col-md-12">
            <s:if test="%{responseObject.responseCode=='000'}">
              <div id="saveMessage">
                <s:actionmessage class="success success-text" />
              </div>
            </s:if>
            <s:else>
              <div class="error-text">
                <s:actionmessage/>
              </div>
            </s:else>
          </div>
          <!-- /.col-md-12 -->
          <s:form action="addUserAction" id="frmAddUser" >
          <s:token/>
          <div class="col-md-3 mb-20">
            <div class="lpay_select_group">
               <label for="">Subuser Type</label>
               <select name="subUserType" id="subUserType" class="selectpicker">
                 <option value="">Select SubUser Type</option>
                 <option value="vendorType">Vendor Type</option>
                 <option value="eposType">ePos Type</option>
                 <option value="normalType">Normal Type</option>
               </select>
            </div>
            <!-- /.lpay_select_group -->  
        </div>
        <!-- /.col-md-3 -->
        <div class="clearfix"></div>
          <div class="col-md-6 mb-20">
            <div class="lpay_input_group">
              <label for="">First Name</label>
              <s:textfield name="firstName" id = "fname" cssClass="lpay_input acquirer-input" autocomplete="off" onkeypress="noSpace(event,this);return isCharacterKey(event);" />
              <div class="error-last error-subadmin">
                <p>Please Enter First Name</p>
              </div>
              <!-- /.error-name -->
            </div>
            <!-- /.lpay_input_group -->
          </div>
          <!-- /.col-md-6 -->
          <div class="col-md-6 mb-20">
            <div class="lpay_input_group">
              <label for="">Last Name</label>
              <s:textfield	name="lastName" id = "lname" cssClass="lpay_input acquirer-input" autocomplete="off"  onkeypress="noSpace(event,this);return isCharacterKey(event);"/>
              <div class="error-last error-subadmin">
                <p>Please Enter Last Name</p>
              </div>
              <!-- /.error-name -->
            </div>
            <!-- /.lpay_input_group -->
          </div>
          
          <div class="col-md-6 mb-20">
            <div class="lpay_input_group">
              <label for="">Business Name</label>
              <s:textfield name="businessName" id = "businessName" cssClass="lpay_input acquirer-input" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);return isCharacterKey(event);" />
              <div class="error-last error-subadmin">
                <p>Please Enter Business Name</p>
              </div>
              <!-- /.error-name -->
            </div>
            <!-- /.lpay_input_group -->
          </div>
          
          <!-- /.col-md-6 -->
          <div class="col-md-6 common-validation mb-20">
            <div class="lpay_input_group">
              <label for="">Mobile Number</label>
              <div class="position-relative">
                <s:textfield name="mobile" id="phoneNumber" cssClass="lpay_input acquirer-input" autocomplete="off" onkeypress="javascript:return isNumber (event)"
                  maxlength="10"/>
                <img src="../image/right-tick.png" alt="/" class="right-tick status-img">
                <img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
              </div>
              <!-- /.position-relative -->
              <div class="error-mobile error-subadmin">
                <p>Please Enter Valid Mobile Number</p>
              </div>
              <!-- /.error-name -->
            </div>
            <!-- /.lpay_input_group -->
          </div>
          <!-- /.col-md-6 -->
          <div class="col-md-6 mb-20 common-validation">
            <div class="lpay_input_group">
              <label for="">Email</label>
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
          <!-- /.col-md-4 -->
          <!-- <div class="col-md-3 mb-20">
            <div class="lpay_select_group d-flex flex-column">
              <label for="getLatest" class="mb-10">Show All Data</label>
              <label class="lpay_toggle" style="margin-left: 5px; margin-top: 4px">
                <input type="checkbox" name="allDataPermissionFlag" id="allOrSelfPermissionFlag" />
              </label>
            </div>
          </div> -->
          <div class="col-md-12 mb-20" data-id="permission">
            <div class="lpay_heading">
              <h3>Privilege Type</h3>
            </div>
            <!-- /.lpay_heading -->
          </div>
          <!-- /.col-md-12 -->
          <div class="col-md-12" id="subUserPermission" data-id="permission">
            <div class="row">
             
              <s:iterator value="subUserPermissionType" status="itStatus">
                <div class="col-md-3 mb-20">
                  <label class="checkbox-label unchecked" for="1"><s:property value="permission" />
                    <s:checkbox name="lstPermissionType" id="%{permission}" class="common" fieldValue="%{permission}" value="false" autocomplete="off"></s:checkbox>
                  </label>
                </div>
                <!-- /.col-md-4 -->
              </s:iterator>
            </div>
            <!-- /.row -->
          </div>
          <!-- /.col-md-12 -->
          <div class="col-md-12 mb-20">
            <div class="lpay_heading">
              <h3>Account Privileges</h3>
            </div>
            <!-- /.lpay_heading -->
          </div>
          <!-- /.col-md-12 -->
          <s:iterator value="listPermissionType" status="itStatus">
            <s:if test="%{permission == 'Booking Report'}">
              <div class="col-md-3 mb-20 d-none" id="bookingPermission">
                  <label class="checkbox-label unchecked " for="1">
                    <s:property value="permission" />
                    <s:checkbox
                      name="lstPermissionType"
                      id="%{permission}"
                      class="common"
                      fieldValue="%{permission}"
                      value="false"
                      autocomplete="off">
                    </s:checkbox>
                  </label>
                  <!-- /.checkbox-subadmin -->
              </div>
            </s:if>
            <s:elseif test="%{permission == 'View Transaction Reports'}">
              <div class="col-md-3 mb-20" id="transactionPermission">
                  <label class="checkbox-label unchecked " for="1">
                    <s:property value="permission" />
                    <s:checkbox
                      name="lstPermissionType"
                      id="%{permission}"
                      class="common"
                      fieldValue="%{permission}"
                      value="false"
                      autocomplete="off">
                    </s:checkbox>
                  </label>
                  <!-- /.checkbox-subadmin -->
              </div>
            </s:elseif>
            <s:else>            
              <div class="col-md-3 mb-20">
                  <label class="checkbox-label unchecked " for="1">
                    <s:property value="permission" />
                    <s:checkbox
                      name="lstPermissionType"
                      id="%{permission}"
                      class="common"
                      fieldValue="%{permission}"
                      value="false"
                      autocomplete="off">
                    </s:checkbox>
                  </label>
                  <!-- /.checkbox-subadmin -->
              </div>
            </s:else>
          </s:iterator>
            <div class="col-md-3 mb-20">
              <label class="checkbox-label unchecked" for="allowEpos">
                Allow ePos
                <s:checkbox
                  name="allowEpos"
                  id="allowEpos"
                  class="common"
                  fieldValue="Allow ePos"
                  value="false"
                  autocomplete="off">
                </s:checkbox>
              </label>
              <!-- /.checkbox-subadmin -->
            </div>
          <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
          <div class="col-md-12 text-center">
            <button disabled="true" id="submit" class="lpay_button lpay_button-md lpay_button-primary">Save User</button>
          </div>
          <!-- /.col-md-12 -->
          </s:form>
      </div>
      <!-- /.row -->
  </section>
  <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->


<script>


    $("#subUserPermission input[type='checkbox']").on("change", function(e){
      $("#subUserPermission input[type='checkbox']").prop("checked", false);
      $("#subUserPermission label").removeClass("checkbox-checked");
      $(this).prop("checked", true);
      $(this).closest("label").addClass("checkbox-checked");
    })

    $("#subUserType").on("change", function(e){
      var _val = $(this).val();
      $(".common").attr("disabled", false);
      $("input[value='Vendor Report']").closest(".col-md-3").attr("disabled", false);
      $("input[value='Allow ePos']").closest(".col-md-3").attr("disabled", false);
      $(".checkbox-label").closest(".col-md-3").removeClass("d-none");
      $(".checkbox-label input").prop("checked", false);
        $(".checkbox-label input[type='checkbox']").closest("label").removeClass("checkbox-checked");
      if(_val != ""){
        $("input[value='Sub User All']").prop("checked", true);
        $("input[value='Sub User All']").closest("label").addClass("checkbox-checked");
        $(".acquirer-input").removeAttr("readonly", false);
      }
      if(_val == "eposType"){
        $("input[value='Vendor Report']").closest(".col-md-3").addClass("d-none");
        $("input[value='Create Invoice']").closest(".col-md-3").addClass("d-none");
        $("input[value='View Invoice']").closest(".col-md-3").addClass("d-none");
        $("input[value='Sub User All']").prop("checked", true);
        $("input[value='Sub User All']").closest("label").addClass("checkbox-checked");
        $("input[value='Allow ePos']").closest(".col-md-3").attr("disabled", true);
        $("input[value='Allow ePos']").prop("checked", true);
        $("input[value='Allow ePos']").closest("label").addClass("checkbox-checked");
      }
      if(_val == "vendorType"){
        $(".acquirer-input").removeAttr("readonly", false);
        $(".checkbox-label").closest(".col-md-3").addClass("d-none");
        // $(".common").attr("disabled", true);
        $("input[value='Vendor Report']").closest(".col-md-3").attr("disabled", true);
        $("input[value='Vendor Report']").closest(".col-md-3").removeClass("d-none");
        $("input[value='Vendor Report']").removeAttr("disabeld");
        $("input[value='Vendor Report']").prop("checked", true);
        $("input[value='Vendor Report']").closest("label").addClass("checkbox-checked");
      }else{
        
      }
    })

  $(".acquirer-input").val("");
    var _checkClass = false;
    function checkBlankField(){
      $(".lpay_input").each(function(e){
        console.log("hi");
        var _thisVal = $(this).val();
        if(_thisVal == ""){
          _checkClass = false;
          $("#submit").attr("disabled", true);
          return false;
        }else{
          _checkClass = true;
          $("#submit").attr("disabled", true);
        }
      });
      if(_checkClass == true){
        if($(".error-subadmin.show").length == 0){
          $("#submit").attr("disabled", false);
        }else{
          $("#submit").attr("disabled", true);
        }
      }else{
        $("#submit").attr("disabled", true);
      }
    }
  
    $(".acquirer-input").on("change", checkBlankField);

    //error msg show

    $(".merchant__form_control").on("blur", function(e){
      var _thisVal = $(this).val();
      if(_thisVal.length > 0){
      }else{
        $(this).closest(".col-md-6").find(".error-subadmin").addClass("show");
      }
    });
  
  
    $(".merchant__form_control").on("keyup", function(e){
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
            console.log(data);
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

    //set id to label
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
    });

    var _get = $("#allOrSelfPermissionFlag").is(":checked");
    if(_get == false){
      $("#allOrSelfPermissionFlag").val(false);
    }

    $(".lpay_toggle").on("change", function(e) {
				var _getChecked = $(this).find("input[type=checkbox]").is(":checked");
				var _label = $(this).closest("label");
				if(_getChecked) {
          _label.addClass("lpay_toggle_on");
          $("#allOrSelfPermissionFlag").val(true)
				} else {
          _label.removeClass("lpay_toggle_on");
          $("#allOrSelfPermissionFlag").val(false)
				}
			});

    // bookingPermission
    $("#transactionPermission input[type='checkbox']").on("change", function(e) {
			var isChecked = $(this).is(":checked");			
			if(isChecked) {
				$("#bookingPermission").removeClass("d-none");
			} else {
				$("#bookingPermission").addClass("d-none");
				$("#bookingPermission label").removeClass("checkbox-checked");
				document.getElementById("bookingPermission").querySelector("input[type='checkbox']").checked = false;
			}
		});
</script>
</body>
</html>