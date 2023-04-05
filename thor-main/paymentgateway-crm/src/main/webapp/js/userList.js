$(document).ready(function() {
  // get sub merchant
function getSubMerchant(_this, _url, _object) {
  var _merchant = _this.target.value;
  var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
  var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
  if (_merchant != "ALL") {
      document.querySelector("body").classList.remove("loader--inactive");
      var data = new FormData();
      data.append('payId', _merchant);
      var _xhr = new XMLHttpRequest();
      _xhr.open('POST', _url, true);
      _xhr.onload = function() {
          if (_xhr.status === 200) {
              var obj = JSON.parse(this.responseText);
              var _option = "";
              document.querySelector("#eNACH-status").value = obj.eNachReport;
              document.querySelector("#merchantInitiatedDirectFlag").value = obj.merchantInitiatedDirectFlag;
              document.querySelector("#virtualAccountFlag").value = obj.virtualAccountFlag;
              document.querySelector("#flag-customerQrFlag").value = obj.customerQrFlag;
              document.querySelector("#flag-capturedMerchantFlag").value = obj.capturedMerchantFlag;
              document.querySelector("#flag-bookingReport").value = obj.bookingReportFlag;
              document.querySelector("#upiAutoPay-status").value = obj.upiAutoPayReport;
              
              if (_object.isSuperMerchant == true) {
                  if (obj.superMerchant == true) {
                      document.querySelector("#" + _subMerchantAttr).innerHTML = "";
                      _option += document.querySelector("#" + _subMerchantAttr).innerHTML = "<option value='ALL'>Self</option>";
                      for (var i = 0; i < obj.subMerchantList.length; i++) {
                          _option += document.querySelector("#" + _subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["emailId"]+">" + obj.subMerchantList[i]["businessName"] + "</option>";
                      }
                      document.querySelector("[data-target=" + _subMerchantAttr + "]").classList.remove("d-none");
                      document.querySelector("#" + _subMerchantAttr + " option[value='ALL']").selected = true;
                      $("#" + _subMerchantAttr).selectpicker();
                      $("#" + _subMerchantAttr).selectpicker('refresh');
                  } else {
                      document.querySelector("[data-target=" + _subMerchantAttr + "]").classList.add("d-none");
                      document.querySelector("#" + _subMerchantAttr).value = "";
                  }
              }
              if (_object.subUser == true) {
                  if (obj.subUserList.length > 0) {
                      _option += document.querySelector("#" + _subUserAttr).innerHTML = "<option value='ALL'>ALL</option>";
                      for (var i = 0; i < obj.subUserList.length; i++) {
                          _option += document.querySelector("#"
                                  + _subUserAttr).innerHTML += "<option value="+obj.subUserList[i]["emailId"]+">"
                                  + obj.subUserList[i]["businessName"]
                                  + "</option>";
                      }
                      document.querySelector("[data-target=" + _subUserAttr + "]").classList.remove("d-none");
                      document.querySelector("#" + _subUserAttr + " option[value='self']").selected = true;
                      $("#" + _subUserAttr).selectpicker();
                      $("#" + _subUserAttr).selectpicker('refresh');
                  } else {
                      document.querySelector("[data-target=" + _subUserAttr + "]").classList.add("d-none");
                      document.querySelector("#" + _subUserAttr).value = "";
                  }
              }
              if (_object.glocal == true) {
                  if (obj.glocalFlag == true) {
                      document.querySelector("[data-id=deliveryStatus]").classList.remove("d-none");
                      $("[data-id=deliveryStatus] select").selectpicker('val', 'All');
                  } else {
                      document.querySelector("[data-id=deliveryStatus]").classList.add("d-none");
                  }
              }

              if (_object.retailMerchantFlag == true) {
                  $("#retailMerchantFlag").val(data.retailMerchantFlag);
                  document.querySelector("#retailMerchantFlag").value = data.retailMerchantFlag;
              }

              if(_this.target.getAttribute("id") == "merchantPayId") {
                checkPermissionHandler();
              }

          }
      }
      _xhr.send(data);
      setTimeout(function(e) {
          document.querySelector("body").classList.add("loader--inactive");
      }, 1000);
  } else {
      document.querySelector("[data-target=" + _subMerchantAttr + "]").classList.add("d-none");
      document.querySelector("#" + _subMerchantAttr).value = "";

  }
}

// populate datatable
function populateDataTable() {
  var token = document.getElementsByName("token")[0].value;
  $('#searchUserDataTable').DataTable({
      language : {	
          search : "",
          searchPlaceholder : "Search"
      },
      dom : 'BTftlpi',
      buttons : [ {
          extend : 'copyHtml5',
          exportOptions : {
              columns : [ ':visible' ]
          }
      }, {
          extend : 'csvHtml5',
          title : 'Search User',
          exportOptions : {
              columns : [ ':visible' ]
          }
      }, {
          extend : 'pdfHtml5',
          title : 'Search User',
          exportOptions : {
              columns : [ ':visible' ]
          }
      }, {
          extend : 'print',
          title : 'Search User',
          exportOptions : {
              columns : [ 0, 1, 2, 3, 4, 5, 6 ]
          }
      }, {
          extend : 'colvis',
          //           collectionLayout: 'fixed two-column',
          columns : [ 1, 2, 3, 4, 5, 6 ]
      } ],
      "ajax" : {
          "url" : "searchUserListAction",
          "type" : "POST",
          "data" : function(d) {
              return generatePostData(d);
          }
      },
      "bProcessing" : true,
      "bLengthChange" : true,
      "bDestroy" : true,
      "iDisplayLength" : 10,
      "order" : [ [ 1, "desc" ] ],
      "aoColumns" : [
          {
              "mData" : "payId",
              "sWidth" : '25%',
              "className" : "payId"
          },
          {
              "mData" : "emailId",
              "sWidth" : '25%',
              "className" : "emailId"
          },
          {
              "mData" : "firstName",
              "sWidth" : '20%',
              "className" : "firstName"
          },
          {
              "mData" : "lastName",
              "sWidth" : '20%',
              "className" : "lastName"
          },
          {
              "mData" : "businessName",
              "sWidth" : '20%'
          },
          {
              "mData" : "mobile",
              "sWidth" : '20%',
              "className" : "mobile"
          },
          {
              "mData" : "registrationDate",
              "sWidth" : '20%',
              "className" : "createdDate"
          },
          {
              "mData" : "isActive",
              "sWidth" : '10%',
              "className" : "isActive"
          },
          {
              "mData" : "subUserType",
              "sWidth" : '10%'
          },
          {
              "mData" : null,
              "sClass" : "center",
              "bSortable" : false,
              "mRender" : function() {
                  return '<button id="editButton" class="lpay_button lpay_button-sm lpay_button-secondary">Edit</button>';
              }
          }, {
              "mData" : "payId",
              "sWidth" : '25%',
              "visible" : false,
          } 
      ]
  });
}

// get variable
function generatePostData(d) {
  var status = document.getElementById("status").value;

  if (status == '') {
      status = 'ALL'
  }
  var obj = {};

  var _getAllInput = document.querySelectorAll("[data-var]");
  _getAllInput.forEach(function(index, element, array) {
      var _new = _getAllInput[element].closest(".col-md-3").classList;
      var _newVal = _new.toString().indexOf("d-none");
      if (_newVal == -1) {
          obj[_getAllInput[element].name] = _getAllInput[element].value
      }
  })
  
  obj.status=status;
  obj.token = document.getElementsByName("token")[0].value;
  obj.draw = d.draw;
  obj.length = d.length;
  obj.start = d.start;
  obj["struts.token.name"] = "token";

  return obj;
}

  $(".acquirer-input").val("");
  var _checkClass = false;
  function checkBlankField() {
    $(".lpay_input").each(function(e){
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

    // bookingPermission
    $("#transactionPermission input[type='checkbox']").on("change", function(e) {    
      checkBookingReportPermission();
    });

    $("body").on("click", "#editButton", function(e) {
      var _parent = $(this).closest("tr");
      var _payId = _parent.find(".payId").text();
      var _emailId = _parent.find(".emailId").text();
      var _firstName = _parent.find(".firstName").text();
      var _lastName = _parent.find(".lastName").text();
      var _mobile = _parent.find(".mobile").text();
      var _createdDate = _parent.find(".createdDate").text();
      var _isActive = _parent.find(".isActive").text();
      $("#emailAddress").val(_emailId);
      $("#firstName").val(_firstName);
      $("#lastName").val(_lastName);
      $("#mobile").val(_mobile);
      $("#isActive").val(_isActive);
      $("#createdDate").val(_createdDate);
      $("#adminUserEdit").submit();
    });

    // tab creation 
    $(".lpay-nav-link").on("click", function(e){
      var _this = $(this).attr("data-id");
      $(".lpay-nav-item").removeClass("active");
      $(this).closest(".lpay-nav-item").addClass("active");
      $(".lpay_tabs_content").addClass("d-none");
      $("[data-target="+_this+"]").removeClass("d-none");
    });

    // var _select = "<option value='ALL'>ALL</option>";
    // $("[data-id='subMerchant']").find('option:eq(0)').before(_select);
    // $("[data-id='subMerchant'] option[value='ALL']").attr("selected", "selected");

    $('[data-id="subMerchant"]').each(function() {
      if(this.tagName !== "BUTTON") {
        $(this).prepend('<option value="ALL" selected="selected">Self</option>');
        $(this).selectpicker("refresh");
      }
    });

    // $('[data-id="subMerchant"]').prepend('<option value="ALL" selected="selected">Self</option>');
    // $('[data-id="subMerchant"]').selectpicker("refresh");

    $("#subUserPermission input[type='checkbox']").on("change", function(e){
      
      $("#subUserPermission input[type='checkbox']").prop("checked", false);
      $("#subUserPermission label").removeClass("checkbox-checked");
      $(this).prop("checked", true);
      $(this).closest("label").addClass("checkbox-checked");
    });

    $("#subUserType").on("change", function(e) {
      var _val = $(this).val();
      $(".common").attr("disabled", false);

      $("input[value='Vendor Report']").closest(".col-md-3").attr("disabled", false);
      $("input[value='Allow ePOS']").closest(".col-md-3").attr("disabled", false);
      $(".checkbox-label").closest(".col-md-3").removeClass("d-none");
      $(".checkbox-label input").prop("checked", false);
      $(".checkbox-label input[type='checkbox']").closest("label").removeClass("checkbox-checked");

      if(_val != "") {
        $("input[value='Sub User All']").prop("checked", true);
        $("input[value='Sub User All']").closest("label").addClass("checkbox-checked");
        $(".acquirer-input").removeAttr("readonly", false);
      }

      if(_val == "normalType") {
        checkBookingReportPermission();
      } else if(_val == "eposType") {
        $("input[value='Vendor Report']").closest(".col-md-3").addClass("d-none");
        $("input[value='VPA Verification']").closest(".col-md-3").addClass("d-none");
        $("input[value='Account Verification']").closest(".col-md-3").addClass("d-none");
        $("input[value='Create Invoice']").closest(".col-md-3").addClass("d-none");
        $("input[value='View Invoice']").closest(".col-md-3").addClass("d-none");
        $("input[value='Sub User All']").prop("checked", true);
        $("input[value='Sub User All']").closest("label").addClass("checkbox-checked");
        $("input[value='Allow ePOS']").closest(".col-md-3").attr("disabled", true);
        $("input[value='Allow ePOS']").prop("checked", true);
        $("input[value='Allow ePOS']").closest("label").addClass("checkbox-checked");
        $("input[value='Customer QR Report']").closest(".col-md-3").addClass("d-none");
        $("input[value='Payout']").closest(".col-md-3").addClass("d-none");
        $("input[value='Payment Advice']").closest(".col-md-3").addClass("d-none");
        $("input[value='Net Settled Report']").closest(".col-md-3").addClass("d-none");

        checkBookingReportPermission();
      } else if(_val == "vendorType") {
        $(".acquirer-input").removeAttr("readonly", false);
        $(".checkbox-label").closest(".col-md-3").addClass("d-none");
        $("input[value='VPA Verification']").closest(".col-md-3").addClass("d-none");
        $("input[value='Account Verification']").closest(".col-md-3").addClass("d-none");
        $("input[value='Vendor Report']").closest(".col-md-3").attr("disabled", true);
        $("input[value='Vendor Report']").closest(".col-md-3").removeClass("d-none");
        $("input[value='Vendor Report']").removeAttr("disabeld");
        $("input[value='Vendor Report']").prop("checked", true);
        $("input[value='Vendor Report']").closest("label").addClass("checkbox-checked");
      }

      $("#subUserPermission .col-md-3").removeClass("d-none");
    });

		populateDataTable();

		var permissionHandler = function(permissionBox, permissionValue) {
			if(document.querySelector("#" + permissionBox) != null) {
				var _checkNach = document.querySelector("#" + permissionValue).value;

				if(_checkNach == "true") {
					document.querySelector("#" + permissionBox).classList.remove("d-none");
				} else if(document.querySelector("#" + permissionBox) != null){
					document.querySelector("#" + permissionBox).classList.add("d-none");
				}
			}
		}

    var checkBookingReportPermission = function() {
      var isChecked = $("#transactionPermission input[type='checkbox']").is(":checked"),
        subUserType = $("#subUserType").val();
      
      
      if(isChecked && subUserType == "normalType") {
        $("#paymentAdvice").removeClass("d-none");
      } else {
        $("#paymentAdvice").addClass("d-none");
        $("#paymentAdvice label").removeClass("checkbox-checked");

        if(document.getElementById("paymentAdvice") !== null) {
          document.getElementById("paymentAdvice").querySelector("input[type='checkbox']").checked = false;
        }
      }
      


      if(isChecked && $("#flag-bookingReport").val() == "true") {
        $("#bookingReport").removeClass("d-none");
      } else {
        $("#bookingReport").addClass("d-none");
        $("#bookingReport label").removeClass("checkbox-checked");

        if(document.getElementById("bookingReport") !== null) {
          document.getElementById("bookingReport").querySelector("input[type='checkbox']").checked = false;
        }
      }
    }

    var checkPermissionHandler = function() {
      permissionHandler("eNachReportFlag", "eNACH-status");
      permissionHandler("MerchantInitiatedPermission", "merchantInitiatedDirectFlag");
      permissionHandler("eCollectionReportFlag", "virtualAccountFlag");
      permissionHandler("customerQrFlag", "flag-customerQrFlag");
      permissionHandler("capturedMerchantFlag", "flag-capturedMerchantFlag");
      permissionHandler("upiAutoPayReportFlag", "upiAutoPay-status");

      checkBookingReportPermission();
    }

		document.querySelector("#merchantPayId").addEventListener("change", function(e) {
      if($(this).val() !== "") {
        getSubMerchant(e, "getSubMerchantList", {
          isSuperMerchant : true,
        });
      } else {
        $(".permission-flag").each(function() {
          $(this).val("false");
        });
      }
		});

		document.querySelector("#merchantList").addEventListener("change", function(e) {
			getSubMerchant(e, "getSubMerchantList", {
				isSuperMerchant : true,
			});
		});

		$("#submit").click(function(env) {
			populateDataTable();
		});

		document.querySelector("#subMerchant").addEventListener("change", function(e) {
			var _subMerchant = document.querySelector("#subMerchant").value;
			if(_subMerchant != "") {
				$.ajax({
					type: "POST",
					url: "getMerchantInitiatedDirectUserAction",
					data: {
						"subMerchantPayId" : _subMerchant,
					},
					success: function(data) {
						document.querySelector("#subMerchant-status").value = data.subMerchantFlag;
						var _checkSubMerchant = document.querySelector("#subMerchant-status").value;
						
						if(_checkSubMerchant == "true") {
							document.querySelector("input[value='Payout']").closest(".col-md-3").classList.remove("d-none");
						} else {
							document.querySelector("input[value='Payout']").closest(".col-md-3").classList.add("d-none");
						}
					}
				});
			}
		});
	});