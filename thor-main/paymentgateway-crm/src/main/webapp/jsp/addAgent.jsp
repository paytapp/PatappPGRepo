<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
  <title>Add Agent</title>
  <link rel="icon" href="../image/favicon-32x32.png">
  <script type="text/javascript" src="../js/jquery-latest.min.js"></script>
  <link rel="stylesheet" href="../css/login.css">

  <style type="text/css">
    .error-text {
      color:#a94442;
      font-weight: bold;
      background-color:#f2dede;
      list-style-type: none;
      text-align: center;
      list-style-type: none;
      margin-top: 10px;
    }

    .response-message {
      padding: 10px;
      font-weight: bold;
      text-align: center;
      font-size: 12px;
    }

    .response-message.error {
      background-color:#fbe9eb;
      color:#e34c5e;
    }

    .response-message.success {
      background-color: #b1deb1;
      color: green;
    }

    .error-text li { list-style-type:none; }

    .positioin-relative { position: relative; }

    #response { color:green; }

    .errorMessage { display: none; }

    .position-relative { position: relative; }

    .fixHeight {
      display: none;
      position: absolute;
      right: 0px;
      bottom: -16px;
    }

    .fixHeight p {
      margin: 0 !important;
      font-size: 13px;
      color: #f00
    }

    .show { display: block; }
  </style>
</head>
<body>
  <section class="add-acquirer lapy_section white-bg box-shadow-box mt-70 p20">
    <!-- <s:form action="addAcquirer" id="frmAddUser" autocomplete="off"> -->
      <s:token/>
      <div class="row">
        <div class="col-md-12">
          <div class="heading_with_icon mb-30">
            <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
              <h2 class="heading_text">Add Agent</h2>
            </div>
          </div>

          
          <div class="col-md-12 mb-20 d-none" id="response-block">
            <div class="response-message"></div>
          </div>
          <div class="col-md-4 common-validation mb-20">
            <div class="lpay_input_group">
              <label for="">First Name <span>*</span></label>
              <s:textfield name="firstName" id="fname" cssClass="lpay_input acquirer-input" autocomplete="off" />
              <div class="fixHeight">
                <p id="fNameError" class="errorInpt frstNameError" style="margin-left: 3px !important;">Please Enter First Name.</p>
              </div>
            </div>
          </div>
          
          <div class="col-md-4 common-validation mb-20">
            <div class="lpay_input_group">
              <label for="">Last Name <span>*</span></label>
              <s:textfield name="lastName" id="lname" cssClass="lpay_input acquirer-input" autocomplete="off"  />
              <div class="fixHeight">
                <p id="lNameError" class="errorInpt lstNameError">Please Enter Last Name.</p>
              </div>
            </div>
          </div>
          
          <div class="col-md-4 common-validation mb-20">
            <div class="lpay_input_group">
              <label for="">Business Name <span>*</span></label>
              <s:textfield name="businessName" id="bname" cssClass="lpay_input acquirer-input" autocomplete="off"/>
              <div class="fixHeight">
                <p id="businessError" class="errorInpt businessNameError">Please Enter Business Name.</p>
              </div>
            </div>
          </div>
          
          <div class="col-md-4 common-validation mb-20">
            <div class="lpay_input_group">
              <label for="">Email Id</label>
              <div class="position-relative">
                <s:textfield name="emailId" id="EmailIdInpt" cssClass="lpay_input acquirer-input" autocomplete="off"/>
                <img src="../image/right-tick.png" alt="/" class="right-tick status-img">
                <img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
              </div>
              <div class="fixHeight">
                <p id="emailError" class="errorInpt emailIdError">Please Enter Valid Email Id.</p>
              </div>
            </div>
          </div>
          
          <div class="col-md-4 common-validation mb-20">
            <div class="lpay_input_group">
              <label for="">Mobile Number <span>*</span></label>
              <div class="positioin-relative">
                <s:textfield name="mobileNumber" id="phoneIdInpt" onkeypress="javascript:return isNumber (event)" cssClass="lpay_input acquirer-input" autocomplete="off" maxlength="10"/>
                <img src="../image/right-tick.png" alt="/" class="right-tick status-img">
                <img src="../image/wrong.png" alt="/" class="wrong-tick status-img">
              </div>
              <div class="fixHeight">
                <p id="phoneError" class="errorInpt phoneError">Please Enter Mobile Number.</p>
              </div>
            </div>
          </div>

          <div class="col-md-12 text-center">
            <input type="submit" id="btnEditUser" name="btnEditUser" class="lpay_button lpay_button-md lpay_button-secondary" disabled>
          </div>
      </div>
      <s:hidden name="token" value="%{#session.customToken}" id="custom-token"></s:hidden>
    <!-- </s:form> -->
  </section>

  <script src="../js/phoneEmailValidation.js"> </script>
  <script src="../js//commanValidate.js"></script>
  <script src="../js/addAgent.js"></script>
</body>
</html>