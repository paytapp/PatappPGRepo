<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign MPA</title>
<link rel="icon" href="../image/favicon-32x32.png">
<script src="../js/jquery-latest.min.js"></script>
<script src="../js/jquery.fancybox.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<script type="text/javascript" src="../js/pdfmake.js"></script>
<link rel="stylesheet" href="../css/jquery.fancybox.min.css">
<link rel="stylesheet" href="../css/bootstrap-select.min.css">
<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
<script src="../js/assignMpaScript.js"></script>
<style>
    .fancybox-content{
        padding: 30px 20px !important;
    }
    .bg-gray{
        background-color: #ccc !important;
        color: #000 !important;
    }
    a.primary-btn:hover{
        text-decoration: none;
        color: #fff;
    }

    .error-msg {
    text-align: center;
    padding: 10px;
    background-color: #f88888;
    border-radius: 5px;
    margin-bottom: 15px;
    color: #fff;
    font-weight: 400;
    font-size: 14px;
}

.actionBtns{
    display: flex;
    align-items: center;
    justify-content: flex-start;
}

.actionBtns a{
    width: 30px;
    height: 30px;
    margin-right: 8px;
    border-radius: 5px;
}

.actionBtns a:last-child{
    margin-right: 0;
}

.btn-edit{
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(0,38,99,.1);
    color: #002163;
}   

.btn-delete{
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(230,0,0,.1);
    color: #e60000;
}

</style>
</head>
<body class="bodyColor">

    <section class="assign-mpa lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Assign MPA</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-4 mb-20">
                <div class="lpay_select_group">
                   <label for="">Select Categories</label>
                   <select name="category" class="" id="categories">
                       <option value="-1">Select Category</option>
                   </select>
                </div>
                <!-- /.lpay_select_group -->
            </div>
            <!-- /.col-md-4 mb-20 -->
            <div class="col-md-4 mb-20">
                <div class="lpay_select_group">
                   <label for="">Select Maker</label>
                   <select name="maker" class="" id="maker">
                        <option value="-1">Select Maker</option>
                   </select>
                </div>
                <!-- /.lpay_select_group -->  
            </div>
            <!-- /.col-md-4 mb-20 -->
            <div class="col-md-4 mb-20">
                <div class="lpay_select_group">
                   <label for="">Select Checker</label>
                   <select name="" class="" id="checker">
                        <option value="-1">Select Checker</option>
                    </select>
                </div>
                <!-- /.lpay_select_group -->  
            </div>
            <!-- /.col-md-4 -->
            <div class="col-md-12 text-center">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="submit-btn">submit</button>
                <a href="#" class="lpay_button lpay_button-md lpay_button-secondary cancel-btn d-none" id="cancel-btn">cancel</a>
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    <section class="assign-mpa lapy_section white-bg box-shadow-box mt-70 p20">
        <div class="row">
            <div class="col-md-12">
                <div class="heading_with_icon mb-30">
                    <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
                    <h2 class="heading_text">Assign MPA List</h2>
                </div>
                <!-- /.heading_icon -->
            </div>
            <!-- /.col-md-12 -->
            <div class="col-md-12">
                <div class="lpay_table">
                    <table width="100%" id="table_id" class="display">
                        <thead>
                            <tr class="lpay_table_head">
                                <th>Categories</th>
                                <th>Maker</th>
                                <th>Checker</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            
                        </tbody>
                    </table>
                </div>
                <!-- /.lpay_table -->
            </div>
            <!-- /.col-md-12 -->
        </div>
        <!-- /.row -->
    </section>
    <!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->
    <!-- confirmation delete box -->
    <div class="lpay_popup_confirm"  id="fancybox">
        <div class="lpay_popup_confirm_box text-center">
            <div class="lpay_popup_box_icon">
                <span class="lpay_popup_icon">!</span>
            </div>
            <!-- /.confirm-box-icon -->
            <div class="lpay_confirm_delete_text">
                <h3>Are you sure ?</h3>
                <span>Do you really want to delete these records? This process cannot be undone.</span>
            </div>
            <!-- /.confirm-delete-text -->
            <div class="confirm-delete-button">
                <button class="lpay_button lpay_button-md lpay_button-secondary" id="cancel-btn">Cancel</button>
                <button  class="lpay_button lpay_button-md lpay_button-primary" id="confirm-btn">Delete</button>
            </div>
            <!-- /.confirm-delete-button -->
        </div>
        <!-- /.confirm-popup-box -->
    </div>
    <!-- /.confrim-popup -->
    <s:hidden name="token" value="%{#session.customToken}" />
    
</body>
</html>