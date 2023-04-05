

  $(document).ready(function(e){
    $('#btnEditUser').click(function() {	
	    $("body").removeClass("loader--inactive");
	    var fname = document.getElementById('fname').value;
	    var lname = document.getElementById('lname').value;
	    var bname = document.getElementById('bname').value;
	    var EmailIdInpt = document.getElementById('EmailIdInpt').value;
	    if(fname){
	      $('.frstNameError').hide();
	    }else{
	      $('.frstNameError').show();
	      return false;
	    }

	    if(lname){
	      $('.lstNameError').hide();
	    }else{
	      $('.lstNameError').show();
	      return false;
	    }

	    if(bname){
	      $('.businessNameError').hide();
	    }else{
	      $('.businessNameError').show();
	      return false;
	    }

	    if(isValidEmail()){
	      $('.emailIdError').hide();
	    }else{
	      $('.emailIdError').show();
	      return false;
      }
		  $("body").removeClass("loader--inactive");
    });

    function isValidEmail() {
        var emailexp = /^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/;
        var emailValue = document.getElementById("EmailIdInpt").value;
          if (emailValue.trim() && emailValue.match(emailexp)) {
          
                  return true;
          } else{
              document.getElementById("emailError").style.display = "block";
        }
    }
      
    $(".acquirer-input").val("");
    var _checkClass = false;
    function checkBlankField(){
      $(".acquirer-input").each(function(e){
        var _thisVal = $(this).val();
        if(_thisVal == ""){
          _checkClass = false;
          $("#btnEditUser").attr("disabled", true);
        }else{
          _checkClass = true;
          $("#btnEditUser").attr("disabled", true);
        }
      });
      if(_checkClass == true){
        if($(".fixHeight.show").length == 0){
          $("#btnEditUser").attr("disabled", false);
        }else{
          $("#btnEditUser").attr("disabled", true);
        }
      }else{
        $("#btnEditUser").attr("disabled", true);
      }
    }
  
    $(".acquirer-input").on("change", checkBlankField);
  
    $(".merchant__form_control").on("blur", function(e){
      var _thisVal = $(this).val();
      if(_thisVal.length > 0){
      }else{
        $(this).closest(".common-validation").find(".fixHeight").addClass("show");
      }
    });
  
  
    $(".merchant__form_control").on("keyup", function(e){
      $(this).closest(".common-validation").find(".fixHeight").removeClass("show");
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
                $(_parent).closest(".common-validation").find(".fixHeight").removeClass("show");
                checkBlankField();
                $("body").addClass("loader--inactive");
            }else{
              $(_parent).closest(".common-validation").find(".fixHeight p").text(data.response);
              $(_parent).closest(".common-validation").find(".fixHeight").addClass("show");
              $(_parent).closest(".common-validation").addClass("verify-denied");
              checkBlankField();
              $("body").addClass("loader--inactive");
            }
          },
          error: function(data){
          }
      });
    }
  
    $("#phoneIdInpt").on("blur", function(e){
      var _getVal = $(this).val();
      if(_getVal.length < 9){
        $("#phoneIdInpt").closest(".common-validation").find(".fixHeight p").text("Please Enter Valid Mobile Number.");
        $("#phoneIdInpt").closest(".common-validation").find(".fixHeight").addClass("show");
      }
    });
  
    $("#phoneIdInpt").on("keyup", function(e){
      var _getVal = $(this).val();
      $(this).closest(".common-validation").removeClass("verify-denied");
      $("#phoneIdInpt").closest(".common-validation").find(".fixHeight").removeClass("show");
      if(_getVal.length > 9){
        mailId("mobileNumberValidate", "#phoneIdInpt", "phoneSuccessStatus", "phoneNumber");
      }
    });
  
    $("#EmailIdInpt").on("blur", function(){
      if(isValidEmail(true)){
        mailId("emailAction", "#EmailIdInpt", "emailSuccessStatus", "emailId");
      }else{
        $(this).closest(".common-validation").find(".fixHeight").addClass("show");
      }
    });
  
    $("#EmailIdInpt").on("keyup", function(e){
        $(this).closest(".common-validation").removeClass("verify-denied");
        $("#EmailIdInpt").closest(".common-validation").find(".fixHeight").removeClass("show");
    });
})