<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Download Payment Advise Report</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/bootstrap-flex.css">

	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>
	<script src="../js/common-scripts.js"></script>

	<script type="text/javascript">
		$(document).ready(function() {

			

			var _select = "<option value='ALL'>ALL</option>";
			$("[data-id=subMerchant]").find('option:eq(0)').before(_select);
			$("[data-id=subMerchant] option[value=ALL]").attr("selected", "selected");

			$("#merchants").on("change", function(e) {
				var _merchant = $(this).val();
				if(_merchant != "") {
					$("body").removeClass("loader--inactive");
					$.ajax({
						type: "POST",
						url: "getSubMerchantListByPayId",
						data: {"payId": _merchant},
						success: function(data){							
							$("#subMerchant").html("");
							if(data.superMerchant == true) {
								var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
								for(var i = 0; i < data.subMerchantList.length; i++){
									_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["payId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
								}
								$("[data-id=submerchant]").removeClass("d-none");
								$("#subMerchant option[value='']").attr("selected", "selected");
								$("#subMerchant").selectpicker();
								$("#subMerchant").selectpicker("refresh");

								hideDownloadBtn();

								setTimeout(function(e){
									$("body").addClass("loader--inactive");
								},500);
							} else {
								setTimeout(function(e){
									$("body").addClass("loader--inactive");
								},500);
								$("[data-id=submerchant]").addClass("d-none");
								$("#subMerchant").val("");
								hideDownloadBtn();
							}
						}
					});
				} else {
					$("[data-id=submerchant]").addClass("d-none");
					$("#subMerchant").val("");	
				}
			});

			$("#subMerchant").on("change", function() {
				hideDownloadBtn();
			});

			var hideDownloadBtn = function() {
				var _merchant = $("#merchants").val(),
					userType = $("#userType").val();
				
				if(_merchant == "ALL" && userType !== "RESELLER") {
					$("#downloadReport").addClass("d-none");
				} else {
					// if(!$('[data-id="submerchant"]').hasClass("d-none")) {
					// 	var _subMerchant = $("#subMerchant").val();
					// 	if(_subMerchant == "ALL") {
					// 		$("#downloadReport").addClass("d-none");
					// 	} else {
					// 		$("#downloadReport").removeClass("d-none");
					// 	}
					// } else {
						$("#downloadReport").removeClass("d-none");
					// }
				}
			}

			hideDownloadBtn();
		});
	</script>

	<script>
 		var expanded = false;

		function showCheckboxes(e) {
			var checkboxes = document.getElementById("checkboxes");
			if (!expanded) {
				checkboxes.style.display = "block";
				expanded = true;
			} else {
				checkboxes.style.display = "none";
				expanded = false;
			}
			// e.stopPropagation();
		}

		function getCheckBoxValue(){
			var allInputCheckBox = document.getElementsByClassName("myCheckBox");

			var allSelectedAquirer = [];
			for(var i=0; i<allInputCheckBox.length; i++){
				
				if(allInputCheckBox[i].checked){
					allSelectedAquirer.push(allInputCheckBox[i].value);	
				}
			}

			document.getElementById('selectBox').setAttribute('title', allSelectedAquirer.join());
			if(allSelectedAquirer.join().length>28){
				var res = allSelectedAquirer.join().substring(0,27);
				document.querySelector("#selectBox option").innerHTML = res+'...............';
			}else if(allSelectedAquirer.join().length==0){
				document.querySelector("#selectBox option").innerHTML = 'ALL';
			}else{
				document.querySelector("#selectBox option").innerHTML = allSelectedAquirer.join();
			}
		}
	</script>

	<script type="text/javascript">
		$(document).ready(function(){
			$(document).click(function(){
				expanded = false;
				$('#checkboxes').hide();
			});
			$('#checkboxes').click(function(e){
				// e.stopPropagation();
			});
		});
	</script>

	<style>
		.divalignment{
			margin-top: -30px !important;
		}
	
		.case-design {
			text-decoration:none;
			cursor: default !important;
		}

		.my_class:hover{
			color: white !important;
		}

		.multiselect {
			width: 170px;
			display:block;
			margin-left:-4px;	
		}

		.selectBox {
			position: relative;
		}

		#checkboxes {
			display: none;
			border: 1px #dadada solid;
			height:300px;
			overflow-y: scroll;
			position:Absolute;
			background:#fff;
			z-index:1;
			margin-left:5px;
		}

		#checkboxes label {
			width: 74%;
		}

		#checkboxes input {
			width:18%;
		}

		.selectBox select {
			width: 95%;  
		}

		.overSelect {
			position: absolute;
			left: 0;
			right: 0;
			top: 0;
			bottom: 0;
		}

		.d-none{
			display: none !important;
		}

		.download-btn {
			background-color:#002163;
			display: inline-block;
			padding: 8px 30px;
			font-size: 14px;
			line-height: 1.42857143;
			color: #fff;
			border: 1px solid #ccc;
			border-radius: 4px;
			margin-top:30px;
		}
		.form-control{
			margin-left: 0!important;
			width: 100% !important;
		}
		.padding10{
			padding: 10px;
		}
		.OtherList input{
			vertical-align: top;
			float: left;
			margin-left: 10px !important;
		}
		.OtherList label{
			display: block;
			font-weight: 700;
			color: #333;
			margin-bottom:8px;
		}
		.pl-15 { padding-left: 15px !important; }
		.pr-15 { padding-right: 15px !important; }
	</style>
</head>
<body>
	<s:textfield type="hidden" value="%{#session.USER.UserType.name()}" id="userType" />
	<section class="payment-download lapy_section white-bg box-shadow-box mt-70 p20">
		<div class="row d-flex flex-wrap">
			<div class="col-md-12">
				<div class="heading_with_icon mb-30">
					<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
					<h2 class="heading_text">Payment Advice Report</h2>
				</div>
				<!-- /.heading_icon -->
			</div>
			<!-- /.col-md-12 -->
			<form id="downloadPaymentAdvise" name="downloadPaymentAdvise" action="downloadPaymentAdvise" class="w-100 d-md-flex flex-wrap">
				<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'  || #session.USER.UserType.name()=='RESELLER' || #session.USER_TYPE.name()=='SUPERADMIN'}">
					<div class="col-md-4 mb-20">
						<div class="lpay_select_group">
							<label for="">Select Merchant</label>
							<s:select
								name="merchantPayId"
								class="selectpicker"
								id="merchants"
								data-var="merchantPayId"
								data-live-search="true"
								headerKey="ALL"
								headerValue="ALL"
								list="merchantList"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-4 -->
				</s:if>
				<s:elseif test="%{#session['USER'].superMerchant == false && #session.USER.UserType.name()=='MERCHANT' && #session.USER.superMerchantId != null }" >
					<div class="col-md-4 mb-20 d-none">
						<div class="lpay_select_group">
							<label for="">Merchant Name</label>
							<s:select
								name="merchantPayId"
								data-var="merchantPayId"
								class="selectpicker"
								id="merchants"
								headerKey=""
								list="subMerchantList"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
					</div>
				</s:elseif>
				<s:else>
					<div class="col-md-4 mb-20 d-none">
						<div class="lpay_select_group">
							<label for="">Merchant Name</label>
							<s:select
								name="merchantPayId"
								data-var="merchantPayId"
								class="selectpicker"
								id="merchants"
								headerKey=""
								list="merchantList"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
					</div>
				</s:else>
					
				<s:if test="%{#session['USER'].superMerchant == true}">
					<div class="col-md-4 mb-20" data-id="submerchant">
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<s:select
								data-id="subMerchant"
								data-var="subMerchant"
								name="subMerchantPayId"
								class="selectpicker"
								id="subMerchant"
								list="subMerchantList"
								data-live-search="true"
								listKey="payId"
								listValue="businessName"
								autocomplete="off"
							/>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->
				</s:if>
				<s:else>
					<div class="col-md-4 mb-20 d-none" data-id="submerchant">
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<select data-var="subMerchant" name="subMerchantPayId" id="subMerchant"></select>
						</div>
						<!-- /.lpay_select_group -->  
					</div>
					<!-- /.col-md-3 -->							
				</s:else>
				
				<div class="col-md-4 mb-20">
					<div class="lpay_input_group">
						<label for="">Payout Date</label>
						<s:textfield
							type="text"
							data-var="payoutDate"
							readonly="true"
							id="dateFrom"
							name="payoutDate"
							class="lpay_input"
							autocomplete="off"
							onchange="handleChange();"
						/>
					</div>
					<!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
						<label for="">Currency</label>
						<s:select name="currency" data-download="currency" data-var="currency" id="currency" list="currencyMap" class="selectpicker" />
					 </div>
					 <!-- /.lpay_select_group --> 
				</div>
				<!-- /.col-md-3 mb-20 -->

				<div class="col-xs-12 text-center">
					<button type="submit" id="downloadReport" class="lpay_button lpay_button-md lpay_button-secondary d-none">Download</button>
					<s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'}">
						<span class="lpay_button lpay_button-md lpay_button-primary" id="sendEmail">Send Email</span>
					</s:if>
				</div>
				<!-- /.col-md-12 -->
			</form>
		</div>
		<!-- /.row -->
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	<div class="lpay_popup">
        <div class="lpay_popup-inner">
            <div class="lpay_popup-innerbox" data-status="default">
                <div class="lpay_popup-innerbox-success lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Email has been sent successfully.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->

                <div class="lpay_popup-innerbox-error lpay-center">
                    <div class="lpay_popup_icon">
                        <img src="../image/wrong-tick.png" alt="">
                    </div>
                    <!-- /.lpay_popup_icon -->
                    <div class="lpay_popup-content">
                        <h3 class="responseMsg">Something went wrong.</h3>
                    </div>
                    <!-- /.lpay_popup-content -->
                    <div class="lpay_popup-button">
                        <button class="lpay_button lpay_button-md lpay_button-secondary confirmButton">Ok</button>
                    </div>
                    <!-- /.lpay_popup-button -->
                </div>
                <!-- /.lpay_popup-innerbox-success -->
            </div>
            <!-- /.lpay_popup-innerbox -->
        </div>
        <!-- /.lpay_popup-inner -->
    </div>
    <!-- /.lpay_popup -->

	<script type="text/javascript">
		function handleChange() {
			var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
			
			$('#dateTo').val($.datepicker.formatDate('dd-mm-yy', transFrom));
		}
		 
		$(document).ready(function() {
			$("#currency").selectpicker('refresh');
			$("#currency").selectpicker('val', '356');

			function sendEmail() {
				$("body").removeClass("loader--inactive");
				var obj = {};
		
				var _getAllInput = document.querySelectorAll("[data-var]");
				_getAllInput.forEach(function(index, element, array) {
					var isVisible =  _getAllInput[element].closest(".col-md-4").classList.contains("d-none");

					if(!isVisible) {
						obj[_getAllInput[element].name] = _getAllInput[element].value
					}
				});
				
				var token = document.getElementsByName("token")[0].value;
	            
	            obj["token"] = token;
				obj["struts.token.name"] = "token";

				$.ajax({
					type: "post",
					url: "sendPaymentAdviseEMail",
					data: obj,
					success: function(data){
						//console.log(data)
						if(data.responseMsg == "success"){
							$(".lpay_popup-innerbox").attr("data-status", "success")
							$(".lpay_popup").fadeIn();
							$("body").addClass("loader--inactive");
						}else{
							$(".lpay_popup-innerbox").attr("data-status", "error")
							$(".lpay_popup").fadeIn();
							$("body").addClass("loader--inactive");
						}
					},
					error: function(data) {
						$(".lpay_popup-innerbox").attr("data-status", "error")
						$(".lpay_popup").fadeIn();
						$("body").addClass("loader--inactive");
					}
				});
			}

			$(".confirmButton").on("click", function(e){
				$(".lpay_popup").fadeOut();
			});

			var _sendEmailBtn = document.getElementById("sendEmail");
			if(_sendEmailBtn !== null) {
				_sendEmailBtn.onclick = sendEmail;
			}

			$("#downloadReport").on("click", function(e) {
				let merchants = $("#merchants").val();
				let dateFrom = $("#dateFrom").val();
				let dateTo = $("#dateTo").val();

				if(merchants == "") {
					alert("Please select Merchant.");
					return false;
				} else if(dateFrom == "") {
					alert("Please select payout date.");
					return false;
				} else if(dateTo == "") {
					alert("Please choose end date.");
					return false;
				}

				return true;
			});
		});
	</script>
</body>
</html>