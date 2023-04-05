<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Modify Agent</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery.js"></script>
<script src="../js/commanValidate.js"></script>


<script type="text/javascript">	

$(document).ready( function() { 
  
  var getInputRadio = $(".checkbox-label input[type='checkbox']");
    for(var i = 0; i < getInputRadio.length; i++) {
        var getId = getInputRadio[i].id;
        if(getInputRadio[i].checked == true) {
            var getId = getInputRadio[i].id;
            $("#"+getId).attr("checked", true);
            $("#"+getId).closest("label").addClass("checkbox-checked");
        }

        $("#"+getId).closest("label").attr("for", getId);
    }

    $(".checkbox-label input[type='checkbox']").on("change", function(e) {
      var getInput = $(this);
      var getId = getInput[0].id;
      if(getInput[0].checked == true) {
            $("#"+getId).closest("label").addClass("checkbox-checked");
      }else{
        $("#"+getId).closest("label").removeClass("checkbox-checked");
      }
    })

  
	$('#btnEditUser').click(function() {	
		var answer = confirm("Are you sure you want to edit Agent details?");
		if (answer != true) {
					return false;
		} else {
      /*  $.ajax({
          url : 'editUserDetails',
          type : "POST",
          data : {
              firstName : document.getElementById('firstName').value ,						
              lastName:document.getElementById('lastName').value,
              mobile : document.getElementById('mobile').value,
              emailAddress : document.getElementById('emailAddress').value,
              isActive: document.getElementById('isActive').value,
            },									
              success:function(data){		       	    		       	    		
                    var res = data.response;		       	    	
                    },
                error:function(data){	
                          alert("Unable to edit user details");
                }
              });		  */
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

  <section class="edit-agent lapy_section white-bg box-shadow-box mt-70 p20">
      <div class="row">
          <div class="col-md-12">
              <div class="heading_with_icon mb-30">
                <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                  <h2 class="heading_text">Edit Agent</h2>
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
          <s:form action="editSubUserDetails" id="frmEditUser" >
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
                <label for="">Mobile Number<span style="color:red; margin-left:3px;">*</span></label>
                <s:textfield name="mobile" id="mobile" cssClass="lpay_input" onkeypress="javascript:return isNumber (event)"  maxlength="13" autocomplete="off" />
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
            <div class="col-md-12">
                <label for="isActive"
                  class="checkbox-label unchecked mb-10">is Active ?
                  <s:checkbox	name="isActive" id="isActive" />
						</label>
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12 text-center">
              <button id="btnEditUser" name="btnEditUser" class="lpay_button lpay_button-md lpay_button-secondary">Save Agent</button>
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