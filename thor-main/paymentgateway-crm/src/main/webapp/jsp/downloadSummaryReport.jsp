<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>Download Summary Report</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<script src="../js/jquery.min.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script src="../js/jquery.popupoverlay.js"></script> 
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>  
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<script type="text/javascript" src="../js/bootstrap-select.min.js"></script>

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
		e.stopPropagation();
	}

	function getCheckBoxValue() {
		var allInputCheckBox = document.getElementsByClassName("myCheckBox");
  		
  		var allSelectedAquirer = [];
  		for(var i=0; i<allInputCheckBox.length; i++) {  			
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

	var _select = "<option value='ALL'>ALL</option>"
		$("[data-id=subMerchant]").find('option:eq(0)').before(_select);
		$("[data-id=subMerchant] option[value=ALL]").attr("selected", "selected");
		$("#merchants").on("change", function(e){
			var _merchant = $(this).val();
			if(_merchant != ""){
				$("body").removeClass("loader--inactive");
				$.ajax({
					type: "POST",
					url: "getSubMerchantListByPayId",
					data: {"payId": _merchant},
					success: function(data){
						console.log(data);
						$("#subMerchant").html("");
						if(data.superMerchant == true){
							var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
							for(var i = 0; i < data.subMerchantList.length; i++){
								_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
							}
							$("[data-id=submerchant]").removeClass("d-none");
							$("#subMerchant option[value='']").attr("selected", "selected");
							$("#subMerchant").selectpicker();
							$("#subMerchant").selectpicker("refresh");
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
						}else{
							setTimeout(function(e){
								$("body").addClass("loader--inactive");
							},500);
							$("[data-id=submerchant]").addClass("d-none");
							$("#subMerchant").val("");
						}
					}
				});
			}else{
				$("[data-id=submerchant]").addClass("d-none");
				$("#subMerchant").val("");	
			}
		})

	$(document).click(function(){
		expanded = false;
		$('#checkboxes').hide();
	});
	$('#checkboxes').click(function(e){
		e.stopPropagation();
	});


});
</script>

<style>
  .divalignment{
	  margin-top: -30px !important;
  }

  .d-none{
	  display: none !important;
  }
  
  .case-design{
	  text-decoration:none;
	  cursor: default !important;
  }
  .my_class:hover{
	  color: white !important;
  }
 .multiselect {
    
	display:block;
		
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
/* .download-btn {
	background-color:#002163;
	display: block;
    width: 100%;

    padding: 3px 4px;
    font-size: 14px;
    line-height: 1.42857143;
    color: #fff;
    border: 1px solid #ccc;
    border-radius: 4px;
	margin-top:30px;
} */
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
     vertical-align: middle;
    display: block;
    font-weight: 700;
    color: #333;
    margin-bottom:8px;
}
</style>


</head>
<body>

	<section class="download-summary-report lapy_section white-bg box-shadow-box mt-70 p20">
		<form id="downloadSummaryReportAction" name="downloadSummaryReportAction" action="downloadSummaryReportAction">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Download Summary Report</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Select Merchant</label>
					   <s:if
					   test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'  || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
					   <s:select name="merchantPayId" class="selectpicker" id="merchants"
						   headerKey="ALL" headerValue="ALL" list="merchantList"
						   listKey="payId" listValue="businessName"  autocomplete="off" />
						</s:if>
						<s:else>
							<s:select name="merchantPayId" class="selectpicker" id="merchants"
								headerKey="" headerValue="" list="merchantList"
								listKey="payId" listValue="businessName" autocomplete="off" />
						</s:else>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				<s:if test="%{#session['USER'].superMerchant == true}">
					<div class="col-md-3 mb-20" data-id="submerchant">
						<div class="lpay_select_group">
							<label>Sub Merchant</label><br>
							<div class="txtnew">
								<s:select data-id="subMerchant" name="subMerchantPayId" class="selectpicker" id="subMerchant"
									list="subMerchantList" listKey="emailId"
									listValue="businessName" autocomplete="off" />
							</div>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-3 -->	
				</s:if>
				<s:else>
					<div class="col-md-3 mb-20 d-none" data-id="submerchant">
						<div class="lpay_select_group">
							<label for="">Sub Merchant</label>
							<div class="txtnew">
								<select name="subMerchantPayId" id="subMerchant"></select>
							</div>
						</div>
						<!-- /.lpay_select_group -->
					</div>
					<!-- /.col-md-3 -->								
				</s:else>
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
						<label for="">Acquirer</label>
						<s:select
							data-size="5"
							title="ALL"
							multiple="true"
							data-selected-text-format="count>2"
							data-actions-box="true"
							list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
							listValue="name"
							listKey="code"
							id="acquirer"
							name="acquirer"
							class="selectpicker"
							value="acquirer"
						/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Payment Method</label>
					   <s:select name="paymentMethods" id="paymentMethods" headerValue="ALL"
						headerKey="ALL" list="@com.paymentgateway.commons.util.PaymentType@values()"
						listValue="name" listKey="code" 
						class="selectpicker" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Currency</label>
					   <s:select name="currency" id="currency" headerValue="ALL"
					   headerKey="ALL" list="currencyMap" class="selectpicker"/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Date From</label>
					<s:textfield type="text" readonly="true" id="dateFrom"
					name="dateFrom" class="lpay_input" autocomplete="off"
					onchange="handleChange();" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
				  <div class="lpay_input_group">
					<label for="">Date To</label>
					<s:textfield type="text" readonly="true" id="dateTo"
					name="dateTo" class="lpay_input" onchange="handleChange();"
					autocomplete="off" />
				  </div>
				  <!-- /.lpay_input_group -->
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Transaction Region</label>
					   <s:select headerKey="ALL" headerValue="ALL" class="selectpicker"
					   list="#{'INTERNATIONAL':'International','DOMESTIC':'Domestic'}" name="paymentsRegion" id = "paymentsRegion" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Card Holder Type</label>
					   <s:select headerKey="ALL" headerValue="ALL" class="selectpicker"
						list="#{'CONSUMER':'Consumer','COMMERCIAL':'Commercial','PREMIUM':'Premium'}" name="cardHolderType" id = "cardHolderType" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Transaction Type</label>
					   <s:select headerKey="ALL" headerValue="ALL" class="selectpicker"
						list="#{'SALE':'SALE','REFUND':'REFUND'}" name="transactionType" id = "transactionType" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Mop Type</label>
					   <s:select name="mopType" id="mopType" headerValue="ALL"
						headerKey="ALL" list="@com.paymentgateway.commons.util.MopTypeUI@values()"
						listValue="name" listKey="code" class="selectpicker"/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-3 mb-20">
					<div class="lpay_select_group">
					   <label for="">Settlement Type</label>
					   <s:select name="partSettleflag" id="partSettleflag" headerValue="ALL"
					   headerKey="ALL" list="#{'N':'Normal','Y':'Part'}"
					 	class="selectpicker"/>
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-3 mb-20 -->
				<div class="col-md-12 text-center">
					<button class="lpay_button lpay_button-md lpay_button-secondary">Download</button>
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row -->
		</form>
	</section>
	<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

<script type="text/javascript">
	   function handleChange() {
			var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
			var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
			if (transFrom == null || transTo == null) {
				alert('Enter date value');
				return false;
			}

			if (transFrom > transTo) {
				alert('From date must be before the to date');
				$('#dateFrom').focus();
				return false;
			}
			if (transTo - transFrom > 31 * 86400000) {
				alert('No. of days can not be more than 31');
				$('#dateFrom').focus();
				return false;
			}
     }
</script>

<script src="../js/common-scripts.js"></script>
</body>
</html>