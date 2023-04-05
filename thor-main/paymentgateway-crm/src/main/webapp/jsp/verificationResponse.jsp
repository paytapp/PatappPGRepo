<%@page import="java.util.Map.Entry"%>
<%@page import="com.paymentgateway.commons.util.Fields"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.paymentgateway.commons.api.Hasher"%>
<%@ page import="org.owasp.esapi.ESAPI"%>
<%@page import="java.util.Map"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Payment Gateway Payment Solution</title>
	<link rel="shortcut icon" href="../img/favicon.ico" type="image/x-icon">

	<link href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,600,700&display=swap" rel="stylesheet">
	<link rel="stylesheet" href="../css/font-awesome/4.3.0/css/font-awesome.min.css">
	<link rel="stylesheet" href="../css/bootstrap-4.min.css">
	<link rel="stylesheet" href="../css/loader-animation.css">
	<link rel="stylesheet" href="../css/response.css">
	<link rel="stylesheet" href="../css/common-style-response.min.css">
</head>
<body class="bg-grey-primary">
	<input type="hidden" id="pageFlag" value="response">

    <s:hidden value="%{response}" id="response"></s:hidden>

	<div class="loader-container w-100 vh-100 d-flex justify-content-center align-items-center flex-column">
		<div class="loaderImage">
			<img src="../image/loader.gif" alt="Loader">
		</div>
		<div id="loading2Loader" class="defaultText mt-10 d-none">
			<h3 class="lang">Please wait while we redirecting to merchant website...</h3>
		</div>
	</div>
	
	<div class="container custom-container">
		<div class="bg-grey-secondary p-15 pb-0 p-lg-60 pb-lg-0 border-radius-20 box-shadow-primary border-primary">
			<div class="row">
				<div class="col-12 d-flex align-items-center justify-content-between">
					<div>
						<img src="../images/logo-response.png" alt="">
					</div>
				</div>
				<!-- /.col-12 -->
			</div>
			<!-- /.row -->
	
			<div class="row my-30">
				<div class="col-12 d-flex justify-content-center">
					<div class="card_box p-15 w-100">
						<div class="card_box_icon text-center line-height-30 d-flex justify-content-center align-items-center text-white font-size-20 mb-15 mx-auto rounded-circle"></div>
						<!-- /.card_box_icon -->
			
						<h3 class="text-center statusResult text-capitalize mb-10 font-weight-medium font-size-18"></h3>
			
						<ul class="list-unstyled">
							<li class="d-flex justify-content-center pt-10">
								<span id="responseMessage"><s:property value="%{responseMsg}" /></span>
							</li>
						</ul>
						<!-- /.list-unstyled -->
					</div>
					<!-- /.card_box -->
				</div>
				<!-- /.col-12 -->
			</div>
			<!-- /.row -->
	
			<div class="row custom-footer bg-grey-ternary position-relative">
				<div class="col-sm-6 col-lg-8 d-flex payment-accept justify-content-center d-sm-block py-15 py-lg-30">
					<img src="../images/verified_by_visa_logo_small.png">
					<img src="../images/mcard.png">
					<img src="../images/pci-dss.png">
				</div>				
				<div class="col-sm-6 col-lg-4 bg-grey-dark-primary d-flex align-items-center py-15 border-radius-br-20 border-radius-bl-20 border-radius-bl-sm-0 border-radius-lg-none justify-content-center">
					<span class="text-grey-light-primary mtn-30">Powered By</span>
					<span class="font-family-logo ml-5 mr-5 font-size-20 text-white">Payment Gateway</span>
					<span class="text-white">&copy;</span>
				</div>
			</div>
			<!-- /.row custom-footer bg-grey-ternary position-relative -->
		</div>
		<!-- /.bg-grey-secondary p-15 pb-0 p-lg-60 pb-lg-0 border-radius-999 box-shadow-primary border-primary -->
	</div>
	<!-- /.container custom-container my-30 -->

    <script src="../js/jquery.min.js"></script>
    <script src="../js/response.js"></script>
</body>
</html>