<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<%@page import="com.paymentgateway.commons.util.PropertiesManager"%>
<html>
<head>

<title>Load Wallet</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<!-- <script src="../js/jquery-latest.min.js" type="text/javascript"></script> -->
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js" type="text/javascript"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<script src="../js/commanValidate.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>
	<!-- <script src="../js/user-script.js"></script> -->
	<style>
		#submit { margin-left: 0; }
		@media (min-width: 768px) {
			#submit { margin-top: 17px; }
		}
		.error_field {
			position: absolute;
			top: -0px;
			padding: 5px;
			opacity: 0;
			z-index: -1;
			width: 100%;
			background-color: #fff;
			border-radius: 4px;
			border: 1px solid #ddd;
			color: #f00;
			transition: all .5s ease;
		}
		.error_field:after {
			content: "";
			width: 12px;
			height: 12px;
			background-color: #fff;
			position: absolute;

			bottom: -6px;
			right: 20px;
			z-index: 99;
			border: 1px solid #ddd;
			transform: rotate(45deg);
			border-left-color: transparent;
			border-top-color: transparent;
		}

		.has-error .error_field{
			opacity: 1;
			z-index: 1;
			top: -20px;
			transition: all .5s ease;
		}		

	</style>
	
</head>
<s:hidden value="%{#session}"></s:hidden>
<body id="mainBody">
	<section class="upload_balance lpay_section white-bg box-shadow-box mt-70 p20">
	   <div class="row">
		  <div class="col-md-12">
			 <div class="heading_with_icon mb-30">
			   <span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
			   <h2 class="heading_text">Load Wallet</h2>
			 </div>
			 <!-- /.heading_icon -->
		  </div>
          <!-- /.col-md-12 -->
			<s:if
			test="%{#session.USER.UserType.name()=='RESELLER'}">
			<div class="col-md-3 mb-20">
			 <div class="lpay_select_group">
				 <label for="">Select Merchant</label>
			<s:select name="merchantEmailId" data-var="merchantEmailId" class="selectpicker"
				id="merchant" data-submerchant="subMerchant" data-user="subUser" headerKey="Select Merchant" data-live-search="true" headerValue="ALL"
				list="merchantList" listKey="emailId"
				listValue="businessName" autocomplete="off" />
				<div class="error_field">
					Please select merchant
				</div>
				<!-- /.error_field -->
				</div>
				</div>
		 </s:if>
		 <s:else>
		 <s:if test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN' || #session.USER_TYPE.name()=='MERCHANT'}">
			<div class="col-md-3 mb-20">
			 <div class="lpay_select_group">
				 <label for="">Select Merchant</label>
				 <s:select name="payId" class="selectpicker"
					 id="merchant" headerKey="" data-var="merchantEmailId" data-submerchant="subMerchant" data-user="subUser"  data-live-search="true" headerValue="Select Merchant"
					 list="merchantList" listKey="payId"
					 listValue="businessName" autocomplete="off" />
					 <div class="error_field">
						 Please select merchant
					 </div>
					 <!-- /.error_field -->
			 </div>
			</div>
		 </s:if>
			 <s:else>
				 <div class="col-md-3 mb-20 d-none">
					 <div class="lpay_select_group ">
						 <label for="">Select Merchant</label>
				 <s:select name="payId" data-var="merchantEmailId" data-live-search="true" class="selectpicker" id="merchant"
					 list="merchantList" data-submerchant="subMerchant" data-user="subUser" listKey="payId"
					 listValue="businessName" autocomplete="off" />
					 <div class="error_field">
						Please select merchant
					</div>
					<!-- /.error_field -->
					 </div>
					 </div>
			 </s:else>
		 </s:else>
		 <s:if test="%{#session['USER'].superMerchant == true}">
				<div class="col-md-3 mb-20" data-target="subMerchant">
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchant" data-var="subMerchantEmailId" headerKey="ALL" data-submerchant="subMerchant" data-user="subUser"  name="subMerchantPayId" class="selectpicker" id="subMerchant" list="subMerchantList" listKey="payId" headerValue="Select Sub Merchant"
							listValue="businessName" autocomplete="off" />
							<div class="error_field">
								Please select merchant
							</div>
							<!-- /.error_field -->
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
			</s:if>
			<s:else> 
				<s:if test="%{#session['USER'].superMerchantId !=null}">
				<div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <s:select data-id="subMerchant" data-var="subMerchantEmailId"  data-submerchant="subMerchant" data-user="subUser"  name="subMerchantPayId" class="selectpicker" id="subMerchant" list="subMerchantList" listKey="payId"
					   listValue="businessName" autocomplete="off" />
					   <div class="error_field">
							Please select sub-merchant
						</div>
						<!-- /.error_field -->
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->	
				</s:if>	
				<s:else>
				<div class="col-md-3 mb-20 d-none" data-target="subMerchant"> 
					<div class="lpay_select_group">
					   <label for="">Sub Merchant</label>
					   <select name="subMerchantEmailId" headerKey="ALL" headerValue="ALL" data-submerchant="subMerchant" data-user="subUser" id="subMerchant" class=""></select>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 -->							
			</s:else>				
			</s:else>
            <!-- <s:hidden name="payId" value="%{#session.['USER'].payId}"></s:hidden> -->
              <div class="col-md-3 mb-20">
                <div class="lpay_input_group">
                  <label for="">Amount</label>
				  <input type="text" name="amount" id="amount" data-var="amount" onkeypress="digitDot(event)" class="lpay_input">
				  <div class="error_field">
					Please select amount
				</div>
				<!-- /.error_field -->
                </div>
                <!-- /.lpay_input_group -->
              </div>
              <!-- /.col-md-4 -->
              <div class="col-md-3">
                  <button class="lpay_button lpay_button-md lpay_button-secondary" id="submit">Submit</button>
              </div>
              <!-- /.col-md-4 -->
	   </div>
	   <!-- /.row -->
	</section>
	<!-- /.lpay_section white-bg box-shadow-box mt-70 p20 -->
	<form action="<%=new PropertiesManager().getSystemProperty("loadWalletRequestAction")%>" method="POST" target="_blank" id="loadWalletPg">

	</form>
	<script>

		   

function getSubMerchant(_this, _url, _object){
    var _merchant = _this.target.value;
    var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
    var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
    if(_merchant != ""){
        document.querySelector("body").classList.remove("loader--inactive");
        var data = new FormData();
        data.append('payId', _merchant);
        var _xhr = new XMLHttpRequest();
        _xhr.open('POST', _url, true);
        _xhr.onload = function(){
            if(_xhr.status === 200){
                var obj = JSON.parse(this.responseText);
                // console.log(obj);
                var  _option = "";
                if(_object.isSuperMerchant == true){
                    if(obj.superMerchant == true){
						document.querySelector("#"+_subMerchantAttr).setAttribute("data-var", "subMerchantEmailId");
                        document.querySelector("#"+_subMerchantAttr).innerHTML = "";
                        _option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value=''>Select Sub Merchant</option>";
                        for(var i = 0; i < obj.subMerchantList.length; i++){
                            _option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
                        }
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
                        document.querySelector("#"+_subMerchantAttr+" option[value='']").selected = true;
                        $("#"+_subMerchantAttr).selectpicker();
                        $("#"+_subMerchantAttr).selectpicker('refresh');
                    }else{
						document.querySelector("#"+_subMerchantAttr).removeAttribute("data-var");
                        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
                        document.querySelector("#"+_subMerchantAttr).value = "";
                    }
                }
                if(_object.subUser == true){
                    if(obj.subUserList.length > 0){
                        _option += document.querySelector("#"+_subUserAttr).innerHTML = "<option value='ALL'>ALL</option>";
                        for(var i = 0; i < obj.subUserList.length; i++){
                            _option += document.querySelector("#"+_subUserAttr).innerHTML += "<option value="+obj.subUserList[i]["emailId"]+">"+obj.subUserList[i]["businessName"]+"</option>";
                        }
                        document.querySelector("[data-target="+_subUserAttr+"]").classList.remove("d-none");
                        document.querySelector("#"+_subUserAttr+" option[value='ALL']").selected = true;
                        $("#"+_subUserAttr).selectpicker();
                        $("#"+_subUserAttr).selectpicker('refresh');
                    }else{
                        document.querySelector("[data-target="+_subUserAttr+"]").classList.add("d-none");
                        document.querySelector("#"+_subUserAttr).value = "";
                    }
                }
                if(_object.glocal == true){
                    if(obj.glocalFlag == true){
                        document.querySelector("[data-id=deliveryStatus]").classList.remove("d-none");
                        $("[data-id=deliveryStatus] select").selectpicker('val', 'All');
                    }else{
                        document.querySelector("[data-id=deliveryStatus]").classList.add("d-none");
                    }
                }

                if(_object.retailMerchantFlag == true){
                    $("#retailMerchantFlag").val(data.retailMerchantFlag);
                    document.querySelector("#retailMerchantFlag").value = data.retailMerchantFlag;
                }
            }
        }
        _xhr.send(data);
        setTimeout(function(e){
            document.querySelector("body").classList.add("loader--inactive");
        }, 1000);
    }else{
        document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
        document.querySelector("#"+_subMerchantAttr).value = "";

    }
}

		document.querySelector("#merchant").addEventListener("change", function(e){
			getSubMerchant(e, "getSubMerchantList", {
				isSuperMerchant : true,
			});
		});

		function digitDot(event) {
            var x = event.keyCode;
            if (x > 47 && x < 58 || x == 32 || x == 08 || x == 46) {
            } else {
                event.preventDefault();
            }
		}


		function sendWallet(){
			
			var _checked = true;
			var obj = {};
			var _getAllInput = document.querySelectorAll("[data-var]");
			_getAllInput.forEach(function(index, element, array){
				if(_getAllInput[element].value != ""){
					obj[_getAllInput[element].name] = _getAllInput[element].value;
					_getAllInput[element].closest(".col-md-3").classList.remove("has-error");
				}else{
					_checked = false;
					_getAllInput[element].closest(".col-md-3").classList.add("has-error");
				}
			})
			

			if(_checked == true){
				document.querySelector("body").classList.remove("loader--inactive");
				$.ajax({
					type: "post",
					url: "loadWalletAction",
					data: obj,
					success: function(data){
						
						var _input = "";
						_input += "<input name='TXNTYPE' value='"+data.txnType+"' />";
						_input += "<input name='AMOUNT' value='"+data.amount+"' />";
						_input += "<input name='ORDER_ID' value='"+data.orderId+"'/>";
						_input += "<input name='RETURN_URL' value='"+data.returnUrl+"' />";
						_input += "<input name='CURRENCY_CODE' value='"+data.currencyCode+"' />";
						_input += "<input name='HASH' value='"+data.hash+"'/>";
						if(data.subMerchantPayId == null){
							_input += "<input name='PAY_ID' value='"+data.payId+"' />"
						}else{
							_input += "<input name='PAY_ID' value='"+data.subMerchantPayId+"' />"
						}
						document.querySelector("#loadWalletPg").innerHTML = _input;
						document.querySelector("#loadWalletPg").submit();
						document.querySelector("#loadWalletPg").classList.add("d-none");
						document.querySelector("#loadWalletPg").innerHTML = "";
						setTimeout(function(e){
							window.location = window.location.href;
						}, 5000);
					}
				})
			}

		}

		document.querySelector("#merchant").onchange = function(e){
			var _getClass = e.target.closest(".col-md-3").classList;
			var _getExactClass = _getClass.toString();
			if(_getExactClass.indexOf("has-error") != -1){
				e.target.closest(".col-md-3").classList.remove("has-error");
			}

		}

		document.querySelector("#submit").onclick = sendWallet;

		$("#amount").on("input", function(event){
			$(this).closest(".col-md-3").removeClass("has-error");
			var _this = $(this).val();
			var _count = 0;
			if(_this.indexOf(".") != -1){
				for(var i = 0; i < _this.length; i++){
					var _pat = /[.]/;
					if(_this[i].match(_pat)){
						_count++;
					}
				}
				if(_count > 1){
					$(this).val(_this.slice(0, _this.length-1));
				}
				var _activePos = _this.indexOf(".");
				var _endPos = _this.substring(_activePos);
				if(_endPos.length > 3){
					$(this).val(_this.slice(0, _this.length-1));
				}
			}
		})
	</script>
</body>
</html>