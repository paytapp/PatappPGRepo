<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta http-equiv="X-UA-Compatible" content="ie=edge">
  <title>Reset Password</title>
  <link rel="shortcut icon" href="../image/favicon-32x32.png" type="image/x-icon">

	<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
	<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,600,700,900&display=swap">
  <link rel="stylesheet" href="../fonts/css/font-awesome.min.css">
  <link rel="stylesheet" href="../css/common-style.css">
	<link rel="stylesheet" href="../css/login.css">
  <link rel="stylesheet" href="../css/loader-animation.css">
  <link rel="stylesheet" href="../css/styles.css">
</head>
<body class="loader--inactive">
    <div class="snackbar bg-snackbar-danger text-snackbar-danger" id="error-snackbar"></div>
    <div class="snackbar bg-snackbar-success text-snackbar-success" id="success-snackbar"></div>
  
    <!-- LOADER -->
    <div class="loader-container w-100 vh-100 lpay-center">
      <div class="loaderImage">
          <img src="../image/loader.gif" alt="Loader">
      </div>
    </div>

    <section class="login-wrapper">
      <header class="login-header">
        <div class="login-header-logo d-flex justify-content-center logo ">
          <img src="../image/white-logo.png" alt="Pg" class="img-fluid">
        </div>
        <!-- /.login-header-logo -->
        <s:a action="merchantSignup" class="lpay_button lpay_button-md">Register Yourself</s:a>
        <!-- <button class="lpay_button lpay_button-md lpay_button-primary">Make Account</button> -->
      </header>
      <!-- /.login-header -->
      <main class="login-form">
        <div class="login-box-inner">
          <div class="heading_with_icon mb-30">
            <span class="heading_icon_box"><i class="fa fa-user" aria-hidden="true"></i></span>
            <h2 class="heading_text">Set New PIN</h2>
          </div>
          <!-- /.heading_icon -->
          <s:form id="resetPassword" action="resetPasswordAction" autocomplete="off">
                <s:token/>

                <s:hidden  value="%{#session.payId}" name="payId" id="payId" />
              <!--  <s:hidden  value="%{emailId}" name="emailId" id="emailId" />-->
  
                <p id="dataValue" class="m-0 font-size-12 text-success"></p>
  
                <s:actionmessage />
                
                <div class="pin-group">
                  <div class="form-group pin-one">
                    <label for="" class="font-size-12 color-grey-light m-0 font-weight-medium">New PIN*</label>
                    <div class="position-relative new-pin">
  
                      <div class="otp-pin-wrapper">
                        <input type="text" class="otp-input-common font-family-password" data-id="pinBox1">
                        <input type="text" class="otp-input-common font-family-password" data-id="pinBox2">
                        <input type="text" class="otp-input-common font-family-password" data-id="pinBox3">
                        <input type="text" class="otp-input-common font-family-password" data-id="pinBox4">
                        <input type="text" class="otp-input-common font-family-password" data-id="pinBox5">
                        <input type="text" class="otp-input-common font-family-password" data-id="pinBox6">
                      </div>
                      <s:textfield
                          type="hidden"
                          name="newPin"
                          id="pin"
                          cssClass="signuptextfield form-control font-family-password"
                          autocomplete="off"
                          maxlength="6" onkeypress="onlyDigit(event)"/>
                    </div>
                    <!-- /.position-relative -->
                    </div>
                    <!-- /.position-relative -->
                  
                    <div class="form-group">								
                      <label for="password" class="font-size-12 color-grey-light m-0 font-weight-medium">Confirm PIN*</label>
                      <div class="position-relative confirm-pin">
                        <div class="otp-pin-wrapper">
                      <input type="text" onkeypress="onlyDigit(event)" class="otp-input-common font-family-password" data-id="pinBox1">
                      <input type="text" onkeypress="onlyDigit(event)" class="otp-input-common font-family-password" data-id="pinBox2">
                      <input type="text" onkeypress="onlyDigit(event)" class="otp-input-common font-family-password" data-id="pinBox3">
                      <input type="text" onkeypress="onlyDigit(event)" class="otp-input-common font-family-password" data-id="pinBox4">
                      <input type="text" onkeypress="onlyDigit(event)" class="otp-input-common font-family-password" data-id="pinBox5">
                      <input type="text" onkeypress="onlyDigit(event)" class="otp-input-common font-family-password" data-id="pinBox6">
                    </div>
                        <s:textfield
                          type="hidden"
                          name="confirmNewPin"
                          id="confirmPin"
                          cssClass="signuptextfield form-control font-family-password"
                          autocomplete="off"
                          maxlength="6" onkeypress="onlyDigit(event)"/>
                          <p class="error" id="errorConfirmPassword">PIN doesn't match</p>
                      </div>
                      <!-- /.position-relative -->
                    </div>
                    <!-- /.form-group -->
                </div>
                <!-- /.pin-group -->
                  <div class="row">
                    <div class="col-12 text-center">
                      <button id="submit" class="lpay_button lpay_button-md lpay_button-secondary d-none">Submit</button>
                      <!-- <s:submit
                        id="submit"
                        value="Submit"
                        class="btn btn-primary py-10 full-width d-none">
                      </s:submit> -->
                    </div>
                    <!-- /.col-md-6 -->
                  </div>
                </div>
                <!-- /.form-group -->
              </s:form>
        </div>
        <!-- /.login-box-inner -->
      </main>
      <!-- /.login-form -->
      <div class="footer-logo">
        <small>Powered by</small>
            <!-- <img src="../image/login-footer-logo.png" height="40px" alt="Pg Logo">  -->
            <span class="footer-logo_text">Paytapp</span>
      </div>
      <!-- /.footer-logo -->

    </section>
        
    <script src="https://code.jquery.com/jquery-3.4.1.min.js"></script>
    <script src="../js/jquery.minshowpop.js"></script>
    <script src="../js/jquery.formshowpop.js"></script>
    <script src="../js/login-script.js"> </script>

    <script>
    //   function onlyDigit(event){
    //     var x = event.keyCode;
    //     if (x > 47 && x < 58 || x == 32) {
    //     } else {s
    //         event.preventDefault();
    //     }
    // }
        $(document).ready(function() {
          $(".new-pin").find("[data-id='pinBox1']").focus();
          $(".new-pin").find("[data-id='pinBox1']").attr("readonly", false);
         /*  var fields = {                
              newPassword : {
                tooltip: "Password must be minimum 8 and <br> maximum 32 characters long, with <br> special characters (! @ , _ + / =) , <br> at least one uppercase and  one <br>lower case alphabet.",
                position: 'right',
                backgroundColor: "#6ad0f6",
                color: '#FFFFFF'
                },
              //color : {
                //tooltip: "This is for your cover color~~~ <a href='#'>here</a>"
                //},
              //text : {
                //tooltip: "Please provide your comment here."
                //}
              }; */
          
          //Include Global Color
          // $("#resesstPassword").formtoolip(fields, { backgroundColor: "#000000" , color : "#FFFFFF", fontSize : 14, padding : 10, borderRadius :  5});
            
          });
  </script>
  
</body>
</html>