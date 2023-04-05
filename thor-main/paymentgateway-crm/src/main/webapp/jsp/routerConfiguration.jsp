<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri="/struts-tags" prefix="s"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	
	<title>Router Configuration Platform</title>
	<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
	<script type="text/javascript" src="../js/jquery.min.js"></script>
	<script src="../js/jquery.popupoverlay.js"></script>
	<link rel="stylesheet" type="text/css" href="../css/popup.css" />
	<link rel="stylesheet" href="../css/bootstrap-select.min.css">
	<script src="../js/bootstrap-select.min.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {	
			
			var _getTest = document.querySelector("#merchantName").value;
			if(_getTest == ""){
				document.querySelector("#datatable2").classList.add("d-none");
			}
			
			$('#Download').prop('disabled',true).addClass('disabled');
			
			$('.card-list-toggle').on('click', function(){
				$(this).toggleClass('active');
				$(this).next('.card-list').slideToggle();
			});

			$(".lpay_input").on("change", function(e){
				var _getMerchant = $("#merchantName").val();
				var _paymentMethods = $("#paymentMethods").val();
				var _cardHolderType = $("#cardHolderType").val();
				var _acquiringMode = $("#acquiringMode").val();
				if(_getMerchant != "Select Merchant" && _paymentMethods != "Select PaymentType" && _acquiringMode != "Select Acquiring mode"){
					$('#Download').prop('disabled',false).removeClass('disabled');
				}else{
					$('#Download').prop('disabled',true).addClass('disabled');
				}
			});

			$(".lpay_toggle").each(function(e){
				$(this).find("input[type=checkbox]").attr("id", "toggle_"+e);
				var _getId = $(this).find("input[type=checkbox]").attr("id");
				$(this).attr("for", _getId);
				if($(this).find("input[type=checkbox]").is(":checked")){
					$(this).addClass("lpay_toggle_on");
				}
			});

			$(".lpay_toggle").on("change", function(e){
				var _getChecked = $(this).find("input[type=checkbox]").is(":checked");
				if(_getChecked == true){
					$(this).closest("label").addClass("lpay_toggle_on");
				}else{
					$(this).closest("label").removeClass("lpay_toggle_on");
				}
			})

			
			var cancelButton = document.getElementById("cancelBtn1");
			if (cancelButton != null){
					cancelButton.disabled = true;
			}
			
			
				var btnArray = document.getElementsByName("cancelBtn");
				//cancelBtnArray;
					for (var i=0;i<btnArray.length ; i++){
						var cancelBtnCurrent = btnArray[i];
						cancelBtnCurrent.disabled = true;
						//}
			}

		});

		var editMode;

		function saveDetails(val, event) {
			var _body = document.getElementsByTagName("body")[0];
			_body.classList.remove("loader--inactive");

			document.getElementById("saveBtnFirst").disabled =true;
				
			var tet = val.parentNode.children[0].id;
			var identifier = tet.slice(0, -3);
			var onOffName = "";
			var loadCheck = "0";
			var rowLength = val.parentNode.children[0].children[0].rows.length ;
			var cellLength = val.parentNode.children[0].children[0].rows[1].cells.length ;
			var rowData = "";
			for (var i = 1; i < rowLength; i++) {
				var acquirer = val.parentNode.children[0].children[0].rows[i].cells[1].innerText;
				var status = val.parentNode.children[0].children[0].rows[i].cells[2].children[0].children[0].children[0].children[0].checked;
				var description = val.parentNode.children[0].children[0].rows[i].cells[3].innerText;
				var mode = 'MANUAL';
				var paymentType = val.parentNode.children[0].children[0].rows[i].cells[5].innerText;
				var mopType = val.parentNode.children[0].children[0].rows[i].cells[6].innerText;
				var allowedFailureCount = val.parentNode.children[0].children[0].rows[i].cells[7].children[0].children[0].children[0].value;
				var alwaysOn = val.parentNode.children[0].children[0].rows[i].cells[8].children[0].children[0].children[0].children[0].checked;
				var loadPercentage = val.parentNode.children[0].children[0].rows[i].cells[9].children[0].children[0].children[0].value;
				var priority = val.parentNode.children[0].children[0].rows[i].cells[10].innerText;
				var retryMinutes = val.parentNode.children[0].children[0].rows[i].cells[11].children[0].children[0].children[0].value;
				var minAmount = val.parentNode.children[0].children[0].rows[i].cells[12].children[0].children[0].children[0].value;
				var maxAmount = val.parentNode.children[0].children[0].rows[i].cells[13].children[0].children[0].children[0].value;
				var onOffValue =  val.parentNode.children[0].children[0].rows[i].cells[14].children[0].children[0].children[0].value;
				
				onOffName = onOffValue;
				
				if (retryMinutes == '') {
					event.preventDefault();

					alert ("Please enter retry minutes!");
					document.getElementById("saveBtnFirst").disabled = false;
					_body.classList.add("loader--inactive");

					return false;
				}
				
				if (parseInt(retryMinutes) < 1) {
					event.preventDefault();

					alert ("Retry minute should be greater than 0!");
					document.getElementById("saveBtnFirst").disabled = false;
					_body.classList.add("loader--inactive");

					return false;
				}
				
				if (allowedFailureCount == '') {
					event.preventDefault();

					alert ("Please enter allowed failed count!");
					document.getElementById("saveBtnFirst").disabled = false;
					_body.classList.add("loader--inactive");

					return false;
				}
				
				if (parseInt(allowedFailureCount) < 0) {
					event.preventDefault();

					alert ("Retry minute should not be negative!");
					document.getElementById("saveBtnFirst").disabled = false;
					_body.classList.add("loader--inactive");

					return false;
				}
				
				if (loadPercentage == ''){
					event.preventDefault();
					
					alert ("Please enter load for each acquirer!");
					document.getElementById("saveBtnFirst").disabled =false;
					_body.classList.add("loader--inactive");

					return false;
				}
				
				if (retryMinutes % 1 != 0 ) {
					event.preventDefault();

					alert ("Decimal values are not allowed !");
					document.getElementById("saveBtnFirst").disabled =false;
					_body.classList.add("loader--inactive");

					return false;
				}
				
				if (allowedFailureCount % 1 != 0 ) {
					event.preventDefault();

					alert ("Decimal values are not allowed !");
					document.getElementById("saveBtnFirst").disabled = false;
					_body.classList.add("loader--inactive");
					
					return false;
				}
				
				if (loadPercentage % 1 != 0 ){
					event.preventDefault();
					alert ("Decimal values are not allowed !");
					document.getElementById("saveBtnFirst").disabled = false;
					_body.classList.add("loader--inactive");
					return false;
				}
				
				if (retryMinutes < 0 || retryMinutes == 0 ){
					event.preventDefault();

					alert ("Retry time should be atleast one minute !");
					document.getElementById("saveBtnFirst").disabled =false;
					_body.classList.add("loader--inactive");

					return false;
				}
				
				if (allowedFailureCount < 0  ) {
					event.preventDefault();

					alert ("Allowed failure count should be greater than zero !");
					document.getElementById("saveBtnFirst").disabled = false;
					_body.classList.add("loader--inactive");

					return false;
				}
				
				if (loadPercentage < 0 ) {
					event.preventDefault();

					alert ("Load percentage should be greater than or equal to zero !");
					document.getElementById("saveBtnFirst").disabled = false;
					_body.classList.add("loader--inactive");

					return false;
				}
				
				if (status) {
					loadCheck = parseInt(loadCheck) + parseInt(loadPercentage);
				}
				
				var rowElement = acquirer.trim()+","+status+","+description.trim()+","+mode.trim()+","+paymentType.trim()+","+mopType.trim()+","+allowedFailureCount.trim()+","+alwaysOn+","+loadPercentage.trim()+","+priority.trim()+","+retryMinutes.trim()+","+minAmount.trim()+","+maxAmount.trim()+","+onOffValue;
				
				if (rowData == ""){
					rowData = rowElement+";";
				}
				else {
					rowData = rowData + rowElement+";";
				}
			}
			
			if (parseInt(loadCheck) > 100 && onOffValue == "OFF_US") {
				event.preventDefault();
					
				alert('Total load percentage sum is greater than 100 % , please enter correct values');
				document.getElementById("saveBtnFirst").disabled = false;
				_body.classList.add("loader--inactive");
				
				return false;
			}
			
			if (parseInt(loadCheck) < 100 && onOffValue == "OFF_US") {
				event.preventDefault();

				alert('Total load percentage sum is lesser than 100 % , please enter correct values');
				document.getElementById("saveBtnFirst").disabled = false;
				_body.classList.add("loader--inactive");

				return false;
			}
			
			var token  = document.getElementsByName("token")[0].value;	
			identifier = identifier + "-" + onOffName;
				
			$.ajax({
				type: "POST",
				url:"editRouterConfiguration",
				timeout: 0,
				data:{"routerConfig":rowData,"identifier":identifier,"mode":"MANUAL","token":token,"struts.token.name": "token",},
				success:function(data){
					var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
					if(null != response) {
						alert(response);
						document.getElementById("saveBtnFirst").disabled =false;
						
						setTimeout(function() {
							_body.classList.add("loader--inactive");
						}, 1000);

						window.location.reload();
					}
				},
				error:function(data) {
					alert("Invalid Input , router rules not updated");
					document.getElementById("saveBtnFirst").disabled = false;

					setTimeout(function() {
						_body.classList.add("loader--inactive");
					}, 1000);

					window.location.reload();
				}
			});		
		}

		function cancel(curr_row,ele){
			var parentEle = ele.parentNode;
			
			if(editMode){
				window.location.reload();
			}
		}
	</script>
	<script type="text/javascript">

		function setHolderMode(_load){
			var _paymentMode = document.querySelector("#paymentMethods").value;
			if(_paymentMode == "CC" || _paymentMode == "DC" || _paymentMode == "EMCC" || _paymentMode == "EMDC" || _paymentMode == "Select PaymentType"){
				if(_load != "load"){
					$("#cardHolderType").selectpicker('val', 'Select Payment region');
					$("#cardHolderType").selectpicker('refresh');
					$("#acquiringMode").selectpicker('val', 'Select Acquiring mode');
					$("#acquiringMode").selectpicker('refresh');
					$(".link_payment_type").closest(".lpay_select_group").removeClass("disable-picker")
				}
			}else{
				$(".link_payment_type").closest(".lpay_select_group").addClass("disable-picker");
				$("#cardHolderType").selectpicker('val', 'CONSUMER');
				$("#cardHolderType").selectpicker('refresh');
				$("#acquiringMode").selectpicker('val', 'OFF_US');
				$("#acquiringMode").selectpicker('refresh');
			}
		}

		// setHolderMode("load");

		$(document).ready(function(){


			setHolderMode("load");

			if($("#paymentType").value != ""){
				$(".surcharge-bank").addClass('active');
			}
			
			$("#paymentType").on("change", function(){
				if(this.value == ""){
					$(".surcharge-bank").removeClass('active');
				}
			});
		});    
	</script>
	<script type="text/javascript">
		$(document).ready(function(){
			$(".surcharge-bank").click(function(){
				$("#datatable2").show();
				$(".surcharge-bank").addClass("active");
				$(".surcharge-report").removeClass("active");
			});
			$(".surcharge-report").click(function(){
				$("#datatable2").show();
				$(".surcharge-report").addClass("active");
				$(".surcharge-bank").removeClass("active");
			});
			var _ruleData = $("#routerRuleData").val();
			if(_ruleData.length > 2){
				$(".empty-data").addClass("d-none");
			}else{
				$(".empty-data").removeClass("d-none");
			}
		});



	</script>
	<style>
		.router_configuration .card-list #saveBtnFirst{
			top: -64px !important;
		}
		.disable-picker .bootstrap-select .dropdown-toggle{ background-color: #f8f8f8;pointer-events: none; }
	</style>
</head>
<body>

<s:actionmessage class="error error-new-text" />

<s:hidden id="routerRuleData" value="%{routerRuleData}"></s:hidden>

<s:form id="routerConfigurationForm" action="routerConfigurationActionMerchant" method="post">
<section class="router_configuration lpay_section white-bg box-shadow-box mt-70 p20">
	<div class="row">
		<div class="col-md-12">
			<div class="heading_with_icon mb-30">
				<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
				<h2 class="heading_text">Router Configuration</h2>
			</div>
			<!-- /.heading_icon -->
		</div>
		<!-- /.col-md-12 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_select_group">
				<label for="">Merchant</label>
			   <s:select headerKey="" data-live-search="true" headerValue="Select Merchant" name="merchantName" id="merchantName" list="merchantList" class="selectpicker lpay_input" autocomplete="off" />
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_select_group">
				<label for="">Payment Type</label>
				<s:select headerKey="Select PaymentType" data-live-search="true" headerValue="Select Payment Type" onchange="setHolderMode('click')" class="lpay_input selectpicker" list="@com.paymentgateway.commons.util.PaymentType@values()" listValue="name" listKey="code" name="paymentMethod" id="paymentMethods" autocomplete="off" />
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_select_group">
				<label for="">Cardholder Type</label>
				<s:select headerKey="Select Payment region"  headerValue="Select Cardholder Type" class="selectpicker lpay_input link_payment_type" list="#{'CONSUMER':'Consumer', 'COMMERCIAL':'Commercial', 'PREMIUM': 'Premium'}" name="cardHolderType" id = "cardHolderType" />
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-3 mb-20">
			<div class="lpay_select_group">
				<label for="">Acquiring Mode</label>
				<s:select headerKey="Select Acquiring mode" data-live-search="true" headerValue="Select Acquiring mode" class="selectpicker lpay_input link_payment_type" list="#{'OFF_US':'Off Us', 'ON_US':'On Us'}" name="acquiringMode" id = "acquiringMode" />
			</div>
			<!-- /.lpay_select_group -->  
		</div>
		<!-- /.col-md-3 mb-20 -->
		<div class="col-md-12 text-center">
			<button class="lpay_button lpay_button-md lpay_button-secondary" id="Download">Submit</button>
		 </div>
		 <!-- .col-md-12 -->
	</div>
	<!-- /.row -->
	<div class="row">
		<div class="col-md-12 mt-30">
			<div id="datatable2" class="scrollD">
				<s:if test="%{routerRuleData.isEmpty()}">
					<div class="empty-data d-none">
						No Rule Found !!
					</div>
					<!-- /.empty-data -->
				</s:if>
				<s:else>							 
					<s:iterator value="routerRuleData" status="pay">
						<div class="text-primary card-list-toggle" id = "test">
							<strong><s:property value="key" /></strong>
						</div>
						<div class="scrollD card-list">
							<s:div class="lpay_table_wrapper" id="%{key +'Div'}" value = "key">
								<table width="100%" border="0" align="center" class="product-spec lpay_custom_table">
									<tr class="lpay_table_head">
										<th width="5%" height="25" valign="middle" style="display: none">id</th>
										<th width="4%" align="left" valign="middle">Acquirer</th>	
										<th width="6%" align="left" valign="middle">Status</th>
										<th width="4%" align="left" valign="middle">Description</th>
										<th width="6%" align="left" valign="middle">Mode</th>
										<th width="6%" align="left" valign="middle">Payment Type</th>
										<th width="5%" align="left" valign="middle">Mop Type</th>
										<th width="3%" align="left" valign="middle">Allowed Fail count</th>
										<th width="4%" align="left" valign="middle">Always On</th>
										<th width="3%" align="left" valign="middle">Load %</th>
										<th width="3%" align="left" valign="middle">Priority</th>
										<th width="3%" align="left" valign="middle">Retry Time</th>
										<th width="4%" align="left" valign="middle">Min Txn</th>
										<th width="4%" align="left" valign="middle">Max Txn</th>
										<th width="4%" align="left" valign="middle">Acq Mode</th>
									</tr>
									<s:iterator value="value" status="itStatus">
										<tr class="boxtext">
											<td align="left" valign="middle" style="display: none"><s:property value="merchant" /></td>
											<td align="left" valign="middle"><s:property value="acquirer" /></td>
											<td align="center" valign="middle">
												<label for="" class="lpay_toggle">
													<s:checkbox name="currentlyActive" value="currentlyActive" data-toggle="toggle" />
												</label>
											</td>	
											<td align="left" valign="middle"><s:property value="statusName" /></td>
											<td align="left" valign="middle"><s:property value="mode" /></td>
											<td align="left" valign="middle"><s:property value="paymentTypeName" /></td>
											<td align="left" valign="middle"><s:property value="mopTypeName" /></td>
											<td align="left" valign="middle">
												<s:textfield
													class="router_input"
													min="0"
													name="allowedFailureCount"
													type='number'
													value="%{allowedFailureCount}"
												/>
											</td>
											<td align="center" valign="middle">
												<label for="" class="lpay_toggle">
													<s:checkbox name="alwaysOn" value="alwaysOn" data-toggle="toggle" />
												</label> 
												<!-- /.lpay_toggle -->
											</td>
											<td align="left" valign="middle">
												<s:textfield
													type='number'
													class="router_input"
													min="0"
													max="100"
													name="loadPercentage"
													value="%{loadPercentage}"
												/>
											</td>		
											<td align="left" valign="middle"><s:property value="priority" /></td>
											<td align="left" valign="middle">
												<s:textfield
													type='number'
													class="router_input"
													min="0"
													name="retryMinutes"
													value="%{retryMinutes}"
												/>
											</td>	
											<td align="left" valign="middle">
												<s:textfield
													readonly="true"
													class="router_input min_input"
													name="minTxn"
													value="%{minAmount}"
												/>
											</td>	
													
											<td align="left" valign="middle">
												<s:textfield
													readonly="true"
													class="router_input min_input"
													name="minTxn"
													value="%{maxAmount}"
												/>
											</td>											
											<td align="left" valign="middle">
												<s:textfield
													readonly="true"
													class="router_input min_input"
													name="onUsoffUsName"
													value="%{onUsoffUsName}"
												/>
											</td>													
										</tr>
									</s:iterator>
								</table>
							</s:div>
							<input id="saveBtnFirst" class="lpay_button lpay_button-sm lpay_button-secondary" type="button" value = "Save" align="center" onClick = saveDetails(this,event) style="margin-top:10px;"></input>
						</div>
					</s:iterator>
				</s:else> 
			</div>
		</div>
		<!-- /.col-md-12 -->
	</div>
	<!-- /.row -->
</section>
<!-- /.lapy_section white-bg box-shadow-box mt-70 p20 -->

	
		
<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
</s:form>
	
</body>
</html>