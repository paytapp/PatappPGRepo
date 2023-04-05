
<%@page import="com.paymentgateway.commons.util.SaltFactory"%>
<%@page import="com.paymentgateway.commons.user.User"%>
<%@page import="com.paymentgateway.commons.util.Currency"%>
<%@page import="com.paymentgateway.commons.util.Amount"%>
<%@page import="com.paymentgateway.commons.util.FieldType"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="com.paymentgateway.commons.util.Constants"%>
<%@page import="com.paymentgateway.commons.util.PropertiesManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Create Custom Page</title>
	<link rel="icon" href="../image/favicon-32x32.png">
  <link href="../css/jquery-ui.css" rel="stylesheet" />
  <link rel="stylesheet" href="../css/froala_editor.min.css">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery-ui.js"></script>
  <script src="../js/bootstrap-select.min.js"></script>
  <style type="text/css">

  .createdLinks a{
    margin: 0 !important;
    margin-right: 10px !important;
    display: inline-block !important;
    margin-bottom: 10px !important;
  }

  .lpay_tabs_content .add_link-btn{
    display: inline-block;
    margin-left: 0 !important;
  }

  .text-red{
    color: #f00;
    margin-left: 5px;
  }

  #uploadLogo{
    z-index: 999;
  }

  </style>
</head>
<body>
  <s:hidden value="%{response}" id="responseMsg"></s:hidden>
	<section class="custom-pages lapy_section white-bg box-shadow-box mt-70 p20">
	<s:form action="saveCustomPage" id="saveImage" autocomplete="off" novalidate="true" enctype="multipart/form-data">
		<div class="row">
			<div class="col-md-9">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Create page</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
      <!-- /.col-md-12 -->
      <div class="col-md-3 mb-20">
        <div class="lpay_select_group">
          <s:if
             test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
             <s:select name="payId" class="selectpicker"
               id="merchant" headerKey="" data-live-search="true" headerValue="Select Merchant"
               list="merchantList" listKey="payId"
               listValue="businessName" autocomplete="off" />
          </s:if>
          <s:else>
            <s:select name="merchant" data-live-search="true" class="selectpicker" id="merchant"
              list="merchantList" listKey="emailId"
              listValue="businessName" autocomplete="off" />
          </s:else>
        </div>
      </div>
      <!-- /.col-md-3 -->
      <div class="col-md-12">
        <div class="lpay_success mb-20 d-none lpay_success-custom">
          <p>Your data has been saved successfully</p>
        </div>
        <!-- /.lpay_success -->
        <div class="lpay_error-msg mb-20 d-none lpay_error-custom">
          <p>Something went wrong</p>
        </div>
        <!-- /.lpay_error -->
      </div>
      <!-- /.col-md-12 -->
      <div class="col-md-12 mb-20">
        <ul class="lpay_tabs d-flex">
            <li class="lpay-nav-item active">
                <a href="#" class="lpay-nav-link" data-id="header">Header</a>
            </li>
            <li class="lpay-nav-item">
                <a href="#" class="lpay-nav-link" data-id="footer">Footer</a>
            </li>
            <li class="lpay-nav-item">
              <a href="#" class="lpay-nav-link" data-id="aboutContent">About Content</a>
          </li>
          <li class="lpay-nav-item">
            <a href="#" class="lpay-nav-link" data-id="contactInfo">Contact Info</a>
          </li>
          <li class="lpay-nav-item">
            <a href="#" class="lpay-nav-link" data-id="paymentDetails">Payment Details Inputs</a>
          </li>
          <li class="lpay-nav-item">
            <a href="#" class="lpay-nav-link" data-id="customization">Customization</a>
          </li>
          <!-- /.lpay-nav-item -->
        </ul>
        <!-- /.lpay_tabs -->
      </div>
      <!-- /.col-md-12 -->
      <div class="lpay_tabs_content w-100 d-none" data-target="customization">
        <div class="col-md-12">
          <div class="row">
            <div class="col-md-12">
              <div class="default_heading">
                <h3>Font Family</h3>
              </div>
              <!-- /.default_heading -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-3 mb-20">
                <div class="lpay_select_group">
                   <label for="">Heading</label>
                   <select name="headingFontFamilySelect" class="selectpicker"  id="headingFontFamily"></select>
                </div>
                <!-- /.lpay_select_group -->  
            </div>
            <!-- /.col-md-3 -->
            <div class="col-md-3 mb-20">
              <div class="lpay_select_group">
                 <label for="">Paragraph</label>
                 <select name="parahFontFamilySelect" class="selectpicker"  id="parahFontFamily"></select>
              </div>
              <!-- /.lpay_select_group -->  
          </div>
          <!-- /.col-md-3 -->
            <div class="col-md-12">
              <div class="default_heading">
                <h3>Font Size</h3>
              </div>
              <!-- /.default_heading -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-3 mb-20">
              <div class="lpay_input_group">
                <label for="">Heading</label>
                <input type="text" onkeypress="onlyDigit(event)" value="22" maxlength="2" name="headingFontSize" class="lpay_input">
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20">
              <div class="lpay_input_group">
                <label for="">Paragraph</label>
                <input type="text" id="paragraphSize" oninput="inputLimit(this)" onkeypress="onlyDigit(event)" value="16" maxlength="2" name="paragraphFontSize" class="lpay_input">
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-12">
              <div class="default_heading">
                <h3>Background / Text Color</h3>
              </div>
              <!-- /.default_heading -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-3 mb-20">
              <div class="lpay_input_group">
                <label for="">Header Background</label>
                <input type="color" name="headerBackgroundColor" value="#ffffff" class="lpay_input">
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20">
              <div class="lpay_input_group">
                <label for="">Footer Background</label>
                <input type="color" name="footerBackgroundColor" value="#fefefe" class="lpay_input">
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20">
              <div class="lpay_input_group">
                <label for="">Heading</label>
                <input type="color" name="headingColor" value="#002663" class="lpay_input">
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20">
              <div class="lpay_input_group">
                <label for="">Paragraph</label>
                <input type="color" name="paragraphColor" value="#333333" class="lpay_input">
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20">
              <div class="lpay_input_group">
                <label for="">Button Color</label>
                <input type="color" value="#002663" class="lpay_input">
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-3 mb-20">
              <div class="lpay_input_group">
                <label for="">Form Background</label>
                <input type="color" name="formBackgroundColor" value="#f5f5f5" class="lpay_input">
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-4 -->
          </div>
          <!-- /.row -->
        </div>
        <!-- /.col-md-12 -->
      </div>
      <!-- /.lpay_tabs_content w-100 -->
      <div class="lpay_tabs_content w-100" data-target="header">
        <div class="col-md-4 mb-20">
          <div class="lpay_input_group">
            <label for="">Page Title <span class="text-red">*</span></label>
            <input type="text" onkeypress="onlyLetters(event)" data-required="true" name="pageTitle" class="lpay_input">
          </div>
          <!-- /.lpay_input_group -->
        </div>
        <!-- /.col-md-4 -->
        
        <div class="col-md-4 mb-20">
          <div class="lpay_input_group">
            <label for="">Enter Slogan</label>
            <input type="text" onkeypress="onlyLetters(event)" name="merchantSlogan" class="lpay_input">
          </div>
          <!-- /.lpay_input_group -->
        </div>
        <!-- /.col-md-4 -->
        <div class="col-md-4 mb-20">
          <div class="upload_file-wrapper">
            <label class="lable-default">Merchant Logo <span class="text-red">*</span></label>
            <span data-toggle="modal" data-target="#logoPreview" class="preview_link d-none">Preview Image</span>
            <div for="uploadLogo" data-response="default" class="upload_file-label">
              <img src="../image/cloud-computing.png" alt="/">
              <input data-required="true" type="file" name="merchantLogoImage" id="uploadLogo" class=""> 
              <div class="uploaded_file-info lable-default uploaded_file-default">
                <span class="d-block">File Size/Dimension: <b>2 MB / 130*80</b></span>
                <span>File Type: <b>PNG</b></span>
              </div>
              <!-- /.upload_file-info -->
              <div class="uploaded_file-info lable-default uploaded_file-sizeError">
                <span>File size is too long</span>
              </div>
              <!-- /.upload_file-info -->
              <div class="uploaded_file-info lable-default uploaded_file-typeError">
                <span>Please choose valid file type</span>
              </div>
              <!-- /.upload_file-info -->
              <div class="uploaded_file-info lable-default uploaded_file-success">
                <span id="uploadedFileName"></span>
              </div>
              <!-- /.upload_file-info -->
            </div>
            <!-- .upload_file-label -->
          </div>
          <!-- /.upload_file-wrapper -->
        </div>
        <!-- /.col-md-4 -->
        <div class="col-md-4 mb-20">
          <label for="merchantType" class="checkbox-label unchecked mb-10">
            <input type="checkbox" id="merchantType" name="PgLogoFlag">
            Payment Gateway Logo
          </label>
        </div>
        <!-- /.col-md-4 -->
      </div>
      <!-- /.lpay_tabs_content -->
      <div class="lpay_tabs_content w-100 d-none" data-target="footer">
        <div class="createdLinks col-md-12"></div>
        <!-- /.newLink -->
      </div>
      <!-- /.lpay_tabs_content w-100 -->
    </div>
    <!-- /.row -->
    <div class="lpay_tabs_content d-none w-100" data-target="aboutContent">
      <div class="row" id="aboutContent">
        <!-- <input type="hidden" name="footerTnCLink"> -->
        <div class="col-md-12">
          <div class="row">
            <div class="col-md-12 mb-20">
              <div class="lpay_input_group">
                <label for="">About Content <span class="text-red">*</span></label>
                <textarea name="aboutContent" data-required="true" style="width: 100%;" id="mytextarea" class="lpay_input_textarea lpay_input" id="aboutContentTextarea" cols="30" rows="10"></textarea>
              </div>
              <!-- /.lpay_input_group -->
            </div>
            <!-- /.col-md-12 -->
          </div>
          <!-- /.row -->
        </div>
        <!-- /.col-md-4 -->
      </div>
      <!-- /.row -->
    </div>
    <!-- /.lpay_tabs_content w-100 -->
    <div class="lpay_tabs_content d-none w-100" data-target="contactInfo">
      <div class="col-md-12">
        <div class="row">
          <div class="col-md-12">
            <div class="default_heading">
              <h3>Email</h3>
            </div>
            <!-- /.default_heading -->
          </div>
          <!-- /.col-md-12 -->
          <div class="col-md-4 mb-20">
            <div class="lpay_input_group">
              <label for="">Email ID <span class="text-red">*</span></label>
              <input type="text" data-required="true" oninput="emailValidation(this)" name="contactEmail" class="lpay_input">
            </div>
            <!-- /.lpay_input_group -->
          </div>
          <!-- /.col-md-4 -->
          <div class="col-md-4 mb-20">
            <div class="lpay_input_group">
              <label for="">Email ID (Optional)</label>
              <input type="text" oninput="emailValidation(this)" name="contactEmail" class="lpay_input">
            </div>
            <!-- /.lpay_input_group -->
          </div>
          <!-- /.col-md-4 -->
          <div class="col-md-12">
            <div class="default_heading">
              <h3>Mobile Number</h3>
            </div>
            <!-- /.default_heading -->
          </div>
          <!-- /.col-md-12 -->
          <div class="col-md-4 mb-20">
            <div class="lpay_input_group">
              <label for="">Mobile Number <span class="text-red">*</span></label>
              <input type="text" data-required="true" name="contactPhone" class="lpay_input" id="">
            </div>
            <!-- /.lpay_input_group -->
          </div>
          <!-- /.col-md-4 -->
          <div class="col-md-4 mb-20">
            <div class="lpay_input_group">
              <label for="">Mobile Number (Optional)</label>
              <input type="text" name="contactPhone" class="lpay_input" id="">
            </div>
            <!-- /.lpay_input_group -->
          </div>
          <!-- /.col-md-4 -->
          <div class="col-md-12">
            <div class="default_heading">
              <h3>Address</h3>
            </div>
            <!-- /.default_heading -->
          </div>
          <!-- /.col-md-12 -->
          <div class="col-md-12 mb-20">
            <div class="lpay_input_group">
              <label for="">Address <span class="text-red">*</span></label>
              <textarea name="contactAddress" data-required="true" cols="30" id="addressTextarea" style="height: 130px;" class="lpay_input lpay_input_textarea" rows="10"></textarea>
            </div>
            <!-- /.lpay_input_group -->
          </div>
          <!-- /.col-md-4 -->
        </div>
        <!-- /.row -->
      </div>
      <!-- /.col-md-12 -->
    </div>
    <!-- /.lpay_tabs_content w-100 -->
    <div class="lpay_tabs_content d-none w-100" data-target="paymentDetails">
      <div class="row" id="paymentDetails">
        <div class="col-md-4 mb-20">
          <div class="lpay_tags-wrapper">
            <span class="lpay_tags-text">
              Name <span class="text-red">*</span>
            </span>
          </div>
          <!-- /.lpay_tags -->
        </div>
        <!-- /.col-md-4 -->
        <div class="col-md-4 mb-20">
          <div class="lpay_tags-wrapper">
            <span class="lpay_tags-text">
              Phone Number <span class="text-red">*</span>
            </span>
          </div>
          <!-- /.lpay_tags -->
        </div>
        <!-- /.col-md-4 -->
        <div class="col-md-4 mb-20">
          <div class="lpay_tags-wrapper">
            <span class="lpay_tags-text">
              Email ID <span class="text-red">*</span>
            </span>
          </div>
          <!-- /.lpay_tags -->
        </div>
        <!-- /.col-md-4 -->
        <div class="col-md-4 mb-20">
          <div class="lpay_tags-wrapper">
            <span class="lpay_tags-text">
              Amount <span class="text-red">*</span>
            </span>
          </div>
          <!-- /.lpay_tags -->
        </div>
        <!-- /.col-md-4 -->
        <div id="inputElement" style="display: none"></div>
        <!-- /#inputElement -->
      </div>
      <!-- /.row -->
    </div>
    <!-- /.lpay_tabs_content w-100 -->
    <div class="col-md-12 text-center">
      <span data-toggle="modal" data-target="#exampleModal" class="lpay_button lpay_button-md lpay_button-primary d-none add_link-btn" data-show="after-save">Add Link</span>
      <span data-toggle="modal" data-target="#inputBox" class="lpay_button lpay_button-md lpay_button-primary inputBtn d-none" data-show="after-save">Add Input</span>
      <span class="lpay_button lpay_button-md lpay_button-secondary d-none viewBtn" onclick="viewCustomPage()">View Page</span>
      <button class="lpay_button lpay_button-md lpay_button-secondary saveBtn" data-show="after-save">Submit</button>
      <span class="lpay_button lpay_button-md lpay_button-secondary d-none" id="downloadPage">Download Page</span>
      <a href="../image/sample-custom.jpg" download class="lpay_button lpay_button-md lpay_button-primary">Download Sample</a>
    </div>
    <!-- /.col-md-12 -->
    <div class="headingFontFamily"></div>
    <div class="parahFontFamily"></div>
    <!-- /.parahFontFamily -->
    <!-- /.headingFontFamily -->
    <s:hidden value="" id="footerLink" name="footerTnCLink"></s:hidden>
    <s:hidden value="" id="inputTags" name="formInputFields"></s:hidden>
  </s:form>

  <form method="POST" action="downloadCustomePage" id="downloadCustomePage" class="" autocomplete="off">
      <input type="hidden" name="payId" id="downloadPayId">
  </form>

	</section>
  <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

  <div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-sm" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
          <h5 class="modal-title" id="exampleModalLabel">Add Link</h5>
        </div>
        <div class="modal-body">
          <div class="lpay_input_group mb-20">
            <label for="">Label</label>
            <input type="text" class="lpay_input" oninput="removeErrorClass(this)" id="anchorLabel">
          </div>
          <!-- /.lpay_input_group -->
          <div class="upload_file-wrapper">
            <label class="lable-default">Upload File</label>
            <form action="" enctype="multipart/form-data" class="tncFile">
              <div for="uploadFileInput" data-response="default" class="upload_file-label">
                <img src="../image/cloud-computing.png" alt="/">
                <input type="hidden" id="uploadPayId">
                <input type="file" name="merchantTnCFile" id="uploadFileInput" class=""> 
                <div class="uploaded_file-info lable-default uploaded_file-default">
                  <span class="d-block">File Size: <b>2 MB</b></span>
                  <span>File Type: <b>PDF</b></span>
                </div>
                <!-- /.upload_file-info -->
                <div class="uploaded_file-info lable-default uploaded_file-sizeError">
                  <span>File size is too long</span>
                </div>
                <!-- /.upload_file-info -->
                <div class="uploaded_file-info lable-default uploaded_file-typeError">
                  <span>Please choose valid file type</span>
                </div>
                <!-- /.upload_file-info -->
                <div class="uploaded_file-info lable-default uploaded_file-success">
                  <span id="uploadedFileName"></span>
                </div>
                <!-- /.upload_file-info -->
              </div>
              <!-- .upload_file-label -->
            </form>
          </div>
          <!-- /.upload_file-wrapper -->
        </div>
        <div class="modal-footer">
          <button type="button" id="cancelBtn" class="btn btn-secondary" data-dismiss="modal">Close</button>
          <button type="button" id="saveBtn" class="btn btn-primary">Save changes</button>
        </div>
      </div>
    </div>
  </div>

  <!-- add input box -->
  <div class="modal fade" id="inputBox" tabindex="-1" role="dialog" aria-labelledby="inputBoxLabel" aria-hidden="true">
    <div class="modal-dialog modal-sm" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
          <h5 class="modal-title" id="inputBoxLabel">Add Input Box</h5>
        </div>
        <div class="modal-body">
          <div class="lpay_input_group mb-20">
            <input type="text" placeholder="Label *" oninput="removeErrorClass(this)" class="lpay_input inputLabel custom-input">
          </div>
          <!-- /.lpay_input_group -->
          <div class="lpay_select_group mb-20">
              <select name="" oninput="removeErrorClass(this)" class="selectpicker inputPriority custom-input">
                <option value="">Required or Optional *</option>
                <option value="Required">Required</option>
                <option value="Optional">Optional</option>
              </select>
          </div>
          <!-- /.lpay_select_group -->
          <div class="lpay_input_group mb-20">
            <input type="text" placeholder="Character Length" class="lpay_input inputCharacter">
          </div>
          <!-- /.lpay_input_group -->
          <div class="lpay_select_group mb-20">
            <select name="" class="selectpicker allowInput">
              <option value="">Input Allow</option>
              <option value="onlyAlpha(this)">Only Alpha</option>
              <option value="onlyNumberInput(this)">Only Number</option>
              <option value="onlyAlphaNumeric(this)">Alpha Numeric</option>
            </select>
        </div>
        <!-- /.lpay_select_group -->
        </div>
        <div class="modal-footer">
          <button type="button" id="cancelBtnInput" class="btn btn-secondary" data-dismiss="modal">Close</button>
          <button type="button" id="saveBtnInput" class="btn btn-primary">Save changes</button>
        </div>
      </div>
    </div>
  </div>

  <!-- add input box -->
  <div class="modal fade" id="logoPreview" tabindex="-1" role="dialog" aria-labelledby="inputBoxLabel" aria-hidden="true">
    <div class="modal-dialog modal-sm" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true">&times;</span>
          </button>
          <h5 class="modal-title" id="inputBoxLabel">Logo Preview</h5>
        </div>
        <div class="modal-body">
          <img src="" id="logoImage" alt="">
        </div>
      </div>
    </div>
  </div>

  <form action="viewCustomPage" id="viewCustomPage" method="POST">
    <input type="hidden" id="viewPayId" name="payId">
  </form>

  

  
  <script type="text/javascript" src="../js/custom-page.js"></script>
  <script type="text/javascript" src="../js/froala_editor.min.js"></script>
  <script type="text/javascript" src="../js/align.min.js"></script>
  <script type="text/javascript" src="../js/image.min.js"></script>
  <script type="text/javascript" src="../js/image_manager.min.js"></script>
  <script type="text/javascript" src="../js/link.min.js"></script>
  <script type="text/javascript" src="../js/lists.min.js"></script>
  <script type="text/javascript" src="../js/paragraph_format.min.js"></script>
  <script type="text/javascript" src="../js/paragraph_style.min.js"></script>

  <script>
    (function () {
      const editorInstance = new FroalaEditor('#mytextarea', {
        enter: FroalaEditor.ENTER_P,
        placeholderText: null,
      });
    })()
  </script>

  <script type="text/javascript">



  function viewCustomPage(){
    document.querySelector("#viewPayId").value = document.querySelector("#merchant").value;
    document.getElementById("viewCustomPage").submit();
  }

    var _button = document.querySelectorAll(".lpay-nav-link");
		_button.forEach(function(e){
			e.addEventListener("click", function(f){
				var _getAttr = f.target.attributes["data-id"].nodeValue;
				var _getAllLink = document.querySelectorAll(".lpay-nav-link");
        var _getAll = document.querySelectorAll(".lpay_tabs_content");


        if(_getAttr == "paymentDetails"){
          document.querySelector(".inputBtn").classList.remove("d-none");
        }else{
          document.querySelector(".inputBtn").classList.add("d-none");
        }

        if(_getAttr == "footer"){
          document.querySelector(".add_link-btn").classList.remove("d-none");
        }else{
          document.querySelector(".add_link-btn").classList.add("d-none");
        }

				_getAllLink.forEach(function(c){
					c.closest(".lpay-nav-item").classList.remove("active");
				})
				_getAll.forEach(function(d){
					d.classList.add("d-none");
				})
				this.closest(".lpay-nav-item").classList.add("active");
				document.querySelector("[data-target="+_getAttr+"]").classList.remove("d-none");
			})
    });

    $(document).ready(function(e){

      $('#exampleModal').on('hide.bs.modal', function () {
        console.log("helo");
          $("#anchorLabel").val(" ");
      });

			// get all input checkbox 
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
					
				} else {
					$("#"+getId).closest("label").removeClass("checkbox-checked");
					$("#"+getId).closest("label").attr("for", getId);
				}
			});
		})

    
    
    function previewImg(e){
        var _val = e.target.value;
        var _parent = $(this).closest("label");
        var _img = document.querySelector("#upload_img");
        var _inputFile = document.querySelector('#uploadMerchantLogo').files[0];
        var _reader = new FileReader();
        var _fileSize = this.files[0].size;
        var _tmpName = _val.replace("C:\\fakepath\\", "");
        if(_fileSize < 2000000){
            console.log(_parent);
            _parent.attr("data-file", "success");
            _parent.find("#merchantLogoName").text(_tmpName);
            _reader.addEventListener("load", function(){
              _img.src = _reader.result;
              document.querySelector(".uploaded-img").style.display = "block";
            }, false);
            if(_inputFile){
                _reader.readAsDataURL(_inputFile);
            }
        }else{
            _img.src = "";
            document.querySelector(".uploaded-img").style.display = "none";
            _parent.attr("data-file", "size-error");
        }
    }

    $("#uploadMerchantLogo").on("change", previewImg);

    // function for upload action
    function uploadTnc(_url, _data){
      $.ajax({
          type: "post",
          enctype: 'multipart/form-data',
          url: _url,
          data: _data,
          processData: false,
          contentType: false,
          success: function(data){
             console.log(data);
          }
      })
    }

    // generic uploader
    function genericUploader(e){
      var _getId = this.id;
      var _val = this.value; //get value
      var _size = this.files[0].size; // get size
      var _fileName = _val.replace("C:\\fakepath\\", ""); // trim name
      var _getPeriod = _fileName.lastIndexOf(".");
      var _inputFile = this.files[0];
      var _reader = new FileReader(); // create image reader
      var _getImg = document.querySelector("#logoImage");
      var _type = _fileName.slice(_getPeriod); // get type
      if(_getId == "uploadFileInput"){
        document.querySelector("#"+_getId).closest("div").classList.remove("red-line-upload");
        var _payId = document.querySelector("#merchant").value;
        var _form = $(".tncFile")[0];
        var data = new FormData(_form);
        data.append("fileName", _fileName);
        data.append("payId", _payId);
      }
      if(_type == ".png" || _type == ".pdf"){
        if(_size < 2000000){
          if(_getId == "uploadFileInput"){
            uploadTnc("uploadTnCFiles", data);
          }
          if(this.closest(".upload_file-wrapper").querySelector(".preview_link")){
            this.closest(".upload_file-wrapper").querySelector(".preview_link").classList.remove("d-none");
          }
          this.closest(".upload_file-wrapper").querySelector("#uploadedFileName").innerText = _fileName;
          this.closest("div").setAttribute("data-response", "success");
          _reader.addEventListener("load", function(e){
            _getImg.src = _reader.result;
          }, false);
          if(_inputFile){
                _reader.readAsDataURL(_inputFile);
          }
        }else{
          this.closest("div").setAttribute("data-response", "sizeError");
        }
      }else{
        this.closest("div").setAttribute("data-response", "typeError");
      }
    }

    document.querySelector("#uploadFileInput").addEventListener("change", genericUploader);
    document.querySelector("#uploadLogo").addEventListener("change", genericUploader);
    // generic uploader ends

  </script>
</body>
</html>