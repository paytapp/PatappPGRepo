<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="refresh" content="905; url=redirectLogin" /> 
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- <script src="../js/jquery.min.js"></script> -->
    <title><decorator:title default="Payment Gateway Solution Private Limited" /></title>
    <link rel="icon" href="../image/favicon-32x32.png">

    <link href="../css/bootstrap.min.css" rel="stylesheet">
    <script src="https://kit.fontawesome.com/0976996b96.js" crossorigin="anonymous"></script>
    <link rel="stylesheet" href="../css/loader-animation.css">
    <!-- <link href="../css/default.css" rel="stylesheet"> -->
   
    <decorator:head />


<script type="text/javascript">
   function refreshMetaTag() {
	    var metaTag = document.getElementsByTagName("meta");
	    var i;
	    for (i = 0; i < metaTag.length; i++) {
	        if (metaTag[i].getAttribute("http-equiv")=='refresh') {
                $('meta[http-equiv=refresh]').remove();
                $('head').append( '<meta http-equiv="refresh" content="905;url=redirectLogin" />' );	           
	        }	        
        }
    }

	// to show new loader
	// $.ajaxSetup({
    //     global: false,
    //         beforeSend: function () {
    //         toggleAjaxLoader();
    //     },
    //     timeout:30000,
    //     complete: function () {
    //         toggleAjaxLoader();
    //         refreshMetaTag();
    //     }           
    // });
    if (self == top) {
        var theBody = document.getElementsByTagName('body')[0];
        if(theBody!=null) {
            theBody.style.display = "block";	
        }
    } else {
        top.location = self.location;
    }
</script>

<style>
    .loaderImage img{ max-width: 70px; }
</style>

<!--  loader scripts -->
<link rel="stylesheet" href="../css/loader/main.css">
<link rel="stylesheet" href="../css/loader/customLoader.css">
<script src="../js/loader/main.js"></script>
</head>
<body onLoad="" class="nav-md">
    <!-- LOADER -->
	<!-- <div class="loader-container">
		<div class="loader-box">
			<div class="loader--dot"></div>
			<div class="loader--dot"></div>
			<div class="loader--dot"></div>
			<div class="loader--dot"></div>
			<div class="loader--dot"></div>
			<div class="loader--dot"></div>
			<div class="loader--text"></div>
		</div>
    </div> -->

    <div class="loader-container w-100 vh-100 lpay-center">
        <div class="loaderImage">
            <img src="../image/loader.gif" alt="Loader">
        </div>
    </div>

    <div class="body">
	    <s:set name ="tokenS" value="%{#session.customToken}"/>
	    <s:hidden id="token" name="token" value="%{tokenS}"></s:hidden>
        <div class="main_container">
            <s:if test="%{#session.USER.UserType.name()=='SUPERADMIN'}"> <%@ include file="/jsp/menuSuperAdmin.jsp"%> </s:if> 		
            <s:if test="%{#session.USER.UserType.name()=='ADMIN'}"> <%@ include file="/jsp/menuAdmin.jsp"%> </s:if> 
            <s:elseif test="%{#session.USER.UserType.name()=='SUBADMIN'}"><%@ include file="/jsp/menuSubAdmin.jsp"%></s:elseif>
            <s:elseif test="%{#session.USER.UserType.name()=='MERCHANT'}"><%@ include file="/jsp/menuMerchant.jsp"%></s:elseif>
            <s:elseif test="%{#session.USER.UserType.name()=='SUBUSER'}"><%@ include file="/jsp/menuSubUser.jsp"%></s:elseif>
            <s:elseif test="%{#session.USER.UserType.name()=='POSMERCHANT'}"><%@ include file="/jsp/menuMerchant.jsp"%></s:elseif>
            <s:elseif test="%{#session.USER.UserType.name()=='RESELLER'}"><%@ include file="/jsp/menuReseller.jsp"%></s:elseif>
            <s:elseif test="%{#session.USER.UserType.name()=='ACQUIRER'}"><%@ include file="/jsp/menuAcqire.jsp"%></s:elseif>
            <s:elseif test="%{#session.USER.UserType.name()=='SUBACQUIRER'}"><%@ include file="/jsp/menuAcquirerSubUser.jsp"%></s:elseif>
            <s:elseif test="%{#session.USER.UserType.name()=='AGENT'}"><%@ include file="/jsp/menuAgent.jsp"%></s:elseif>
            <s:elseif test="%{#session.USER.UserType.name()=='RECONUSER'}"><%@ include file="/jsp/menuReconUser.jsp"%></s:elseif>
            <s:elseif test="%{#session.USER.UserType.name()=='PARENTMERCHANT'}"><%@ include file="/jsp/menuParentMerchant.jsp"%></s:elseif>
            
            <!-- page content -->
           <div class="right_col">
               <div class="row">
                   <div class="col-md-12 text-left mt-10">
                       <div class="topBar">
                           <div class="topBar_pageTitle">
                               <span class="icon_box collapse-nav white-bg mr-15 box-shadow">
                                   <i class="fa fa-ellipsis-v" aria-hidden="true"></i>
                                </span>
                                <h2 class="lpay_title">Paytapp CRM</h2>
                           </div>
                           <!-- /.topBar_pageTitle -->
                           <div class="topBar_quickLinks">
                               <div class="topBar_notification">
                                   <span id="menuIcon" class="icon_box white-bg box-shadow notification mr-15">
                                    <i class="fa fa-bars" aria-hidden="true"></i>
                                   </span>
                               </div>
                               <!-- /.topBar_notification -->
                               <div class="tobBar_admin pos-r">
                                <span class="icon_box white-bg box-shadow">
                                    <i class="fa fa-user fa-fw"></i>
                                </span>  
                                <nav class="lpay_dropDown">
                                    <ul class="dropDown_ul dropdown_style_1 white-bg">
                                        <li><a href="#" class="unlink">Welcome <s:property value="%{#session.USER.businessName}" /></a></li>
                                        <li><s:a action="logout">Logout</s:a></li>
                                    </ul>
                                </nav>
                                <!-- /.lpay_dropDown -->
                               </div>
                               <!-- /.tobBar_admin -->
                           </div>
                           <!-- /.topBar_quickLinks -->
                       </div>
                       <!-- /.topBar -->
                       <!-- <a href="home" class="newredtxt">Home</a> | <a href="javascript:window.history.back();" class="newredtxt">Back</a> -->
                    </div>
                </div>

                <decorator:body />
                <%@ include file="/jsp/footer.jsp" %>
			
              <!-- programmer work eara content -->
            </div>

            <!-- /page content -->
        </div>
    </div>

    <div id="custom_notifications" class="custom-notifications dsp_none">
        <ul class="list-unstyled notifications clearfix" data-tabbed_notifications="notif-group"></ul>
        <div class="clearfix"></div>
        <div id="notif-group" class="tabbed_notifications"></div>
    </div>
    <s:token/>

    <script>
        window.addEventListener("load", function() {
            setTimeout(function() {
                document.getElementsByTagName("body")[0].classList.add("loader--inactive");
            }, 1000);

            var dataTableFilter = document.querySelector(".dataTables_filter input");
            if(dataTableFilter !== null) {
                dataTableFilter.addEventListener("paste", function(e) {
                    e.preventDefault();
                    
                    var clipboardData = e.clipboardData || window.clipboardData || e.originalEvent.clipboardData,
                        pastedData = clipboardData.getData('Text').replace(/<[^>]*>/g, "");
                    this.value = pastedData.trim();
                });
            }
        });
    </script>
</body>
</html>