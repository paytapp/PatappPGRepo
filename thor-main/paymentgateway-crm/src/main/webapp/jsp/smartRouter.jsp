<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/struts-tags" prefix="s"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Acquirer Routing Rules</title>
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript" src="../js/sweetalert.js"></script>
<script type="text/javascript" src="../js/offus.js"></script>
<link rel="stylesheet" href="../css/sweetalert.css">
<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>
<style type="text/css">
	[class^="Acquirer"] .wwlbl .label {
		color: #666;
		font-size: 12px;
		padding: 2px 0 8px;
		display: block;
		margin: 0;
		text-align: left;
		font-weight: 600; 
	} 

	[class^="Acquirer"] .wwlbl+br { display: none; }

	.card-list-toggle {
		cursor: pointer;
		padding: 8px 12px;
		border: 1px solid #ccc;
		position: relative;
		background: #ddd;
	}

	.card-list-toggle:before {
		position: absolute;
		right: 10px;
		top: 7px;
		content: "\f078";
		font-family: 'FontAwesome';
		font-size: 15px;
	}

	.card-list-toggle.active:before { content: "\f077"; }

	.card-list { display: none; }

	.acquirerRemoveBtn { float: left; }

	[class*="AcquirerList"]>div {
		clear: both;
		border-bottom: 1px solid #ccc;
		padding: 0 8px 5px;
		margin: 0 -8px 8px;
	}

	[class*="AcquirerList"]>div:last-child {
		border-bottom: none;
		padding-bottom: 0;
		margin-bottom: 0;
	}

	[class^="AcquirerList"] input[disabled] {
		color: #bbb;
		display: none;
	}

	[class^="AcquirerList"] input[disabled]+label {
		color: #bbb;
		display: none;
	}

	[class*="OtherList"]>div {
		clear: both;
		border-bottom: 1px solid #ccc;
		padding: 0 8px 5px;
		margin: 0 -8px 8px;
	}

	[class*="OtherList"]>div:last-child {
		border-bottom: none;
		padding-bottom: 0;
		margin-bottom: 0;
	}

	[class^="OtherList"] input[disabled] {
		color: #bbb;
		display: none;
	}

	[class^="OtherList"] input[disabled]+label {
		color: #bbb;
		display: none;
	}

	.sweet-alert .sa-icon { margin-bottom: 30px; }

	.sweet-alert .lead.text-muted { font-size: 14px; }

	.sweet-alert .btn {
		font-size: 12px;
		padding: 8px 30px;
		margin: 0 5px;
	}

	table.product-spec.disabled {
		cursor: not-allowed;
		opacity: 0.5;
	}

	table.product-spec.disabled .btn { pointer-events: none; }

	.merchantFilter {
		padding: 15px 0;
		width: 200px;
	}

	.AcquirerList input[type="radio"] {
		vertical-align: top;
		float: left;
		margin: 2px 5px 0 0;
	}

	.AcquirerList label {
		vertical-align: middle;
		display: block;
		font-weight: normal;
	}

	
	/* .Acquirer1 input[type="radio"] {
		vertical-align: top;
		float: left;
		margin: 2px 5px 0 0;
	}

	.Acquirer1 label {
		vertical-align: middle;
		display: block;
		font-weight: normal;
	} */

	
	.boxtext td div input[type="radio"] {
		vertical-align: top;
		float: left;
		margin: 2px 5px 0 0;
	}

	.boxtext td div label {
		vertical-align: middle;
		display: block;
		font-weight: normal;
	}

	#onus_section .checkbox, #offus_section .checkbox { margin: 0; }

	.checkbox .wwgrp input[type="checkbox"] { margin-left: 0; }

	.checkbox label .wwgrp input[type="checkbox"] { margin-left: -20px; }

	.select2-container { width: 200px !important; }

	.btn:focus { outline: 0 !important; }

	.labelClass {
		font-size: 13px;
		font-weight: 700;
		color: black;
	}

	#loading {
		width: 100%;
		height: 100%;
		top: 0px;
		left: 0px;
		position: fixed;
		display: block;
		z-index: 99
	}

	#loading-image {
		position: absolute;
		top: 40%;
		left: 55%;
		z-index: 100;
		width: 10%;
	}

	.errorRule {
		text-align: center;
		color: #a94442;
		font-size: 15px;
		background: #f2dede;
		border-color: #f2dede;
		border-radius: 5px;
		margin-left: 10px;
		margin-right: 10px;
		display: none;
	}
	.paymentSlab { font-size: 10px; }
</style>
<script type="text/javascript">
	$(document).ready(function() {
		$('#offus_section .card-list-toggle').trigger('click');

		// Initialize select2
		$("#offus_merchant").select2();
		$("#onus_merchant").select2();
		$("#selectMerchant").select2();
	});
</script>

</head>
<body>
	<h1 class="pageHeading">Rule Engine</h1>
	<br>

	<s:actionmessage class="success success-text" />
	<s:div>
		<!-----Add Merchant Filter To show Rule List----->
		<div>
			<p class="errorRule" id="errorOfNoRule">Sorry, there is no such rule for this merchant</p>
		</div>

		<div class="merchantFilter">
			<s:select
				name="merchants"
				class="form-control"
				id="selectMerchant"
				headerKey=""
				headerValue="Select Merchant"
				listKey="payId"
				listValue="businessName"
				list="merchantList"
				autocomplete="off"
			/>
		</div>

		<div id="onus_section">
			<div class="text-primary card-list-toggle">
				<strong>ON US</strong>
			</div>
			<div class="card-list">
				<div class="table-responsive" id="onUs_default">
					<table width="100%" border="0" align="center"
						class="product-spec onus_table" style="display: none">
						<tr class="boxheading">
							<th align="150" valign="middle">Merchant</th>
							<th align="left" valign="middle">Acquirer</th>
							<th width="100" align="left" valign="middle">Currency</th>
							<th width="200" align="left" valign="middle">Payment Type</th>
							<th width="150" align="left" valign="middle">Mop</th>
							<th width="150" align="left" valign="middle">Transaction Type</th>
							<th width="150" align="left" valign="middle">Cardholder Type</th>
							<th width="150" align="left" valign="middle">Payment Region</th>
							<th width="150" align="left" valign="middle">Amount Slab</th>
							<th width="150" align="left" valign="middle">Action</th>
						</tr>
					</table>

					<div class="merchantFilter">
						<label class="labelClass">Create Rules :</label>
						<s:select
							name="merchants"
							class="form-control"
							id="onus_merchant"
							listKey="payId"
							listValue="businessName"
							list="merchantList"
							autocomplete="off"
						/>
					</div>

					<table width="100%" border="0" align="center" class="product-spec onusFormTable">
						<tr class="boxheading">
							<th align="left" valign="middle">Acquirer</th>
							<th width="100" align="left" valign="middle">Currency</th>
							<th width="200" align="left" valign="middle">Payment Type</th>
							<th width="150" align="left" valign="middle">Mop</th>
							<th width="150" align="left" valign="middle">Transaction Type</th>
							<th width="150" align="left" valign="middle">Cardholder Type</th>
							<th width="150" align="left" valign="middle">Payment Region</th>
							<th width="150" align="left" valign="middle">Amount Slab</th>
							<th width="150" align="left" valign="middle">Action</th>
						</tr>
						<tr class="boxtext">
							<td align="left" valign="top">
								<s:iterator value="@com.paymentgateway.commons.util.AcquirerTypeUI@values()">
									<div class="checkbox">
										<label>
											<s:checkbox name="acquirer" fieldValue="%{code}" value="false"></s:checkbox> 
											<s:property />
										</label>
									</div>
								</s:iterator>
							</td>
							<td align="left" valign="top">
								<s:iterator value="currencyMap">
									<div class="checkbox">
										<label>
											<s:checkbox name="currency" fieldValue="%{value}" value="false"></s:checkbox> 
											<s:property value="%{value}" />
										</label>
									</div>
								</s:iterator>
							</td>
							<td align="left" valign="top" class="paymentType">
								<%-- <s:iterator value="@com.paymentgateway.commons.util.PaymentType@values()" status="piteratorstatus" var="payment">
									<div class="checkbox">
										<s:checkbox name="paymentType" fieldValue="%{code}" label="%{name}" id="%{top+'boxOnUs'}"></s:checkbox>
										<s:property />
									</div>
								</s:iterator> --%>
								<s:iterator value="paymentTypeList" status="piteratorstatus" var="payment">
									<s:div class="checkbox" data-type="%{code}">
										<s:checkbox name="paymentType" fieldValue="%{code}" label="%{name}" id="%{top+'boxOnUs'}"></s:checkbox>
									</s:div>
								</s:iterator>
							</td>
							<td align="left" valign="top">
								<s:iterator value="mopList">
									<div class="checkbox">
										<s:checkbox name="mopType" fieldValue="%{code}" label="%{name}" id="%{top+'mopBoxOnUs'}"></s:checkbox>
									</div>
								</s:iterator>
							</td>
							<td align="left" valign="top">
								<s:iterator value="transactionTypeList" status="iteratorstatus" var="txn">
									<div class="checkbox">
										<label>
											<s:div>
												<s:checkbox name="txnType" fieldValue="%{top}" id="%{top+'boxOnUs'}"></s:checkbox>
												<s:property />
											</s:div>
										</label>
									</div>
								</s:iterator>
							</td>
							<td align="left" valign="top" style="width: 10%;">
								<div class="checkbox">
									<s:checkbox name="typeCard" fieldValue="CONSUMER" label="Consumer" id="consumerTypeCard" />
								</div>
								<div class="checkbox">
									<s:checkbox name="typeCard" fieldValue="COMMERCIAL" label="Commercial" id="commercialTypeCard" />
								</div>
								<div class="checkbox">
									<s:checkbox name="typeCard" fieldValue="PREMIUM" label="Premium" id="premiumTypeCard" />
								</div>
							</td>

							<td align="left" valign="top" style="width: 10%;">
								<div class="checkbox">
									<s:checkbox name="region" fieldValue="DOMESTIC" label="Domestic" id="domesticRegion" />
								</div>
								<div class="checkbox">
									<s:checkbox name="region" fieldValue="INTERNATIONAL" label="International" id="internationalRegion" />
								</div>
							</td>

							<td align="left" valign="top" style="width: 10%;" class="paymentSlab">
								<div class="checkbox">
									<s:checkbox name="slabId" fieldValue="01" label="0 - 1000" id="slabId01" />
								</div>
								<div class="checkbox">
									<s:checkbox name="slabId" fieldValue="02" label="1000 - 2000" id="slabId02" />
								</div>
								<div class="checkbox">
									<s:checkbox name="slabId" fieldValue="03" label="2000 - 1000000" id="slabId03" />
								</div>
							</td>
							
							<td align="left" valign="top">
								<button type="submit" id="onus_submit" class="btn btn-primary disabled" onclick="getOnUs()">Save</button>
								<button type="button" id="onus_reset" class="btn btn-warning disabled">Reset</button>
							</td>
						</tr>
					</table>
				</div>
			</div>
		</div>
	</s:div>

	<s:div>
		<br>

		<div id="offus_section">
			<div class="text-primary card-list-toggle">
				<strong>OFF US</strong>
			</div>

			<div class="card-list">
				<div class="table-responsive">
					<table width="100%" border="0" align="center" class="product-spec offus_table" style="display: none">
						<tr class="boxheading">
							<th width="100" align="left" valign="middle">Merchant</th>
							<th width="100" align="left" valign="middle">Currency</th>
							<th width="100" align="left" valign="middle">Payment Type</th>
							<th width="150" align="left" valign="middle">Mop</th>
							<th width="150" align="left" valign="middle">Transaction Type</th>
							<th align="left" valign="middle">Region</th>
							<th align="left" valign="middle">Type</th>
							<th align="left" valign="middle">Amount Slab</th>
							<th align="left" valign="middle">Acquirer</th>
							<th width="150" align="left" valign="middle">Action</th>
						</tr>
					</table>
					<div class="offusFormTable">
						<div class="merchantFilter">
							<label class="labelClass">Create Rules :</label>
							<s:select
								name="merchants"
								class="form-control"
								id="offus_merchant"
								listKey="payId"
								listValue="businessName"
								list="merchantList"
								autocomplete="off"
							/>
						</div>

						<table width="100%" border="0" align="center" class="product-spec">
							<tr class="boxheading">
								<th width="100" align="left" valign="middle">Currency</th>
								<th width="200" align="left" valign="middle">Payment Type</th>
								<th width="150" align="left" valign="middle">Mop</th>
								<th width="150" align="left" valign="middle">Transaction Type</th>
								<th align="left" valign="middle">Region</th>
								<th align="left" valign="middle">Type</th>
								<th align="left" valign="middle">Amount Slab</th>
								<th align="left" valign="middle">Acquirer</th>
								<th width="150" align="left" valign="middle">Action</th>
							</tr>
							<tr class="boxtext">
								<td align="left" valign="top">
									<s:iterator value="currencyMap">
										<div class="checkbox">
											<label>
												<s:checkbox name="currency" fieldValue="%{value}" value="false"></s:checkbox> 
												<s:property value="%{value}" />
											</label>
										</div>
									</s:iterator>
								</td>

								<td align="left" valign="top" class="paymentType">
									<s:iterator value="paymentTypeList" status="piteratorstatus" var="payment">
										<s:div class="checkbox" data-type="%{code}">
											<s:checkbox name="paymentType" fieldValue="%{code}" label="%{name}" id="%{top+'boxOffUs'}"></s:checkbox>
										</s:div>
									</s:iterator>
								</td>

								<td align="left" valign="top">
									<s:iterator value="mopList">
										<div class="checkbox">											
											<s:checkbox name="mopType" fieldValue="%{code}" label="%{name}" id="%{top+'mopBoxOffUs'}"></s:checkbox>											
										</div>
									</s:iterator>
								</td>

								<td align="left" valign="top">
									<s:iterator value="transactionTypeList" status="iteratorstatus" var="txn">
										<div class="checkbox">
											<label>
												<s:div>
													<s:checkbox name="txnType" fieldValue="%{top}" id="%{top+'boxOffUs'}"></s:checkbox>
													<s:property />
												</s:div>
											</label>
										</div>
									</s:iterator>
								</td>
								
								<td align="left" valign="top" style="width: 10%;">
									<div class="checkbox">
										<s:checkbox name="region" fieldValue="DOMESTIC" label="Domestic" id="regionDomestic"></s:checkbox>
									</div>
									<div class="checkbox">
										<s:checkbox name="region" fieldValue="INTERNATIONAL" label="International" id="regionInternational"></s:checkbox>
									</div>
								</td>

								<td align="left" valign="top" style="width: 10%;">
									<div class="checkbox">
										<s:checkbox name="typeCard" fieldValue="CONSUMER" label="Consumer" id="typeCardConsumer"></s:checkbox>
									</div>
									<div class="checkbox">
										<s:checkbox name="typeCard" fieldValue="COMMERCIAL" label="Commercial" id="typeCardCommercial"></s:checkbox>
									</div>
									<div class="checkbox">
										<s:checkbox name="typeCard" fieldValue="PREMIUM" label="Premium" id="typeCardPremium"></s:checkbox>
									</div>
								</td>

								<td align="left" valign="top" style="width: 10%;" class="paymentSlab">
									<div class="checkbox">
										<s:checkbox name="slabId" fieldValue="01" label="0 - 1000" id="offusSlabId01"></s:checkbox>
									</div>
									<div class="checkbox">
										<s:checkbox name="slabId" fieldValue="02" label="1000 - 2000" id="offusSlabId02"></s:checkbox>
									</div>
									<div class="checkbox">
										<s:checkbox name="slabId" fieldValue="03" label="2000 - 1000000" id="offusSlabId03"></s:checkbox>
									</div>
								</td>

								<td align="left" valign="top" style="width: 13%;">
									<div class="AcquirerList">
										<div class="Acquirer1">
											<s:radio
												list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
												name="Acquirer1" listKey="%{code}" listValue="%{name}"
												label="Acquirer1"
											/>
										</div>
									</div>
									<button type="button" class="btn btn-primary acquirerCloneBtn" style="display: none;">Add</button>
								</td>

								<td align="left" valign="top">
									<button type="submit" id="offus_submit" class="btn btn-primary disabled" onclick="getOffUs()">Save</button>
									<button type="button" id="offus_reset" class="btn btn-warning disabled">Reset</button>
								</td>
							</tr>
						</table>
					</div>
				</div>
			</div>
		</div>
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:div>
</body>
</html>