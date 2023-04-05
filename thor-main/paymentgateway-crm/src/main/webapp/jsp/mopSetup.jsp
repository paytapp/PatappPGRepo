<%@taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">

	<title>Merchant Mapping</title>
	<link rel="icon" href="../image/favicon-32x32.png">	
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet"></link>	
	<link rel="stylesheet" href="../css/bootstrap-select.min.css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/common-style.css">
	<script type="text/javascript" src="../js/jquery.min.js"></script>
	<script src="../js/bootstrap-select.min.js"></script>
	
	<style>
		.checkbox-label { margin-bottom: 10px !important; }		
		.btn:focus { outline: 0 !important; }
		.text-danger { color: red; }

		.error {
			position: absolute;
			bottom: -20px;
			right: 0;
			font-size: 12px;
			color: red;
			visibility: hidden;
			opacity: 0;
			transition: all ease .5s;
		}
		.error.show {
			visibility: visible;
			opacity: 1;
		}
		.d-none { display: none; }
		.d-flex { display: flex; }
		.d-inline-block { display: inline-block; }
		.flex-wrap { flex-wrap: wrap; }
		.align-items-center { align-items: center; }
		.mb-0 { margin-bottom: 0 !important; }
		.pl-20 { padding-left: 20px !important; }
		.pt-10 { padding-top: 10px !important; }
		.mt-10 { margin-top: 10px !important; }
		.mr-15 { margin-right: 15px !important; }
		.p-20 { padding: 20px !important; }
		.payment-boxes:not(:last-child) { margin-bottom: 10px !important; }

		div[id="Net Banking"] > .checkbox-label { width: 100% !important; margin-left: 15px !important; }
		div[id="Net Banking"] { padding-left: 5px !important; }
	</style>
</head>
<body>
	<div class="transactions"></div>
	<s:form theme="simple" id="mopSetupForm" name="mopSetupForm" action="mopSetUpAction">
		<section class="mop-setup lpay_section white-bg box-shadow-box mt-70 p20">
			<div class="row">
				<div class="col-md-12">
					<div class="heading_with_icon mb-30">
						<span class="heading_icon_box"><i class="fa fa-bar-chart-o" aria-hidden="true"></i></span>
						<h2 class="heading_text">Merchant Mapping</h2>
					</div>
					<!-- /.heading_icon -->
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-12">
					<div id="err2" class="bg-snackbar-danger text-snackbar-danger text-center py-10 mb-20 font-weight-medium d-none">Atleast one currency is required for Merchant Mapping</div>
					<div id="success" class="bg-snackbar-success text-snackbar-success text-center py-10 mb-20 font-weight-medium d-none">Mappings saved successfully</div>
				</div>
				<!-- /.col-md-12 -->
				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
						<label for="user">User <span class="text-danger">*</span></label>
						<s:select list="#{'1':'Merchant'}" class="selectpicker" id="user" name="user" value="1" />
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4  mb-20-->

				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
						<label for="merchants">Select Merchant <span class="text-danger">*</span></label>
						<div class="position-relative">
							<s:select
								name="merchantEmailId"
								class="selectpicker"
								id="merchants"
								headerKey=""
								headerValue="Select Merchant"
								list="listMerchant"
								data-live-search="true"
								listKey="emailId"
								listValue="businessName"
								value="%{merchantEmailId}"							
								autocomplete="off"
							/>
							<span error-id="merchants" class="error">Please select merchant</span>
						</div>
						<!-- /.position-relative -->
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 -->

				<div class="col-md-4 mb-20">
					<div class="lpay_select_group">
						<label for="acquirer">Acquirer <span class="text-danger">*</span></label>
						<div class="position-relative">
							<s:select
								name="acquirer"
								class="selectpicker"
								id="acquirer"
								headerKey=""
								data-live-search="true"
								headerValue="Select Acquirer"
								list="@com.paymentgateway.commons.util.AcquirerTypeUI@values()"
								listKey="code"
								listValue="name"								
								autocomplete="off"
							/>
							<span error-id="acquirer" class="error">Please select acquirer</span>
						</div>
						<!-- /.position-relative -->
					</div>
					<!-- /.lpay_select_group -->  
				</div>
				<!-- /.col-md-4 -->
			</div>
			<!-- /.row -->
		</section>
		<!-- /.lpay_section white-bg box-shadow-box mt-70 p20 -->

		<div class="mop-setup-box d-none">
			<section class="mop-setting lpay_section white-bg box-shadow-box mt-20 p20 pb-10">
				<div class="row">
					<div class="col-md-12">
						<div id="id+checkBoxes" class="filters" style="display: none">
							<s:set value="0" var="listCounter" />
	
							<s:iterator value="currencies">
								<label class="checkbox-label unchecked mb-10">
									<s:property value="value" />
									<s:checkbox
										name="currency"
										id="%{'id+' +key}"
										fieldValue="%{key}"
										value="false"
										onclick="hidefields(this)"
									/>
								</label>
								
								<s:div id="%{'boxdivid+' +key}" style="display: none">
									<s:div id="%{'elements'+key}" class="row d-flex flex-wrap">
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Merchant ID</label>
												<s:textfield
													name="merchantId"
													id="%{'idmerchantid+' +key}"
													value="%{merchantId}"
													placeholder="MerchantId"
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-3 -->
										
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Txn Key</label>
												<s:textfield
													name="txnKey"
													id="%{'idtxnkey+' +key}"
													value="%{txnKey}"
													placeholder="Txn Key"
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-3 -->
										
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Password</label>
												<s:textfield
													name="password"
													id="%{'idpassword+' +key}"
													value="%{password}"
													placeholder="Password"
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-3 -->
	
										<s:if test="%{acquirer  in {'FSS','CITRUS'}}">											
											<div class="col-md-3 mb-20 d-flex align-items-center">
												<label class="checkbox-label unchecked">
													<s:checkbox
														name="directTxn"
														id="%{'id3dflag+' +key}"														
														type="checkbox"
														value="%{directTxn}">
													</s:checkbox>
													Non 3DS
												</label>
											</div>
										</s:if>

	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf1</label>
												<s:textfield
													name="adf1"
													id="%{'idadf1+' +key}"
													value="%{adf1}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-3 -->
	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf2</label>
												<s:textfield
													name="adf2"
													id="%{'idadf2+' +key}"
													value="%{adf2}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-3 -->
	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf3</label>
												<s:textfield
													name="adf3"
													id="%{'idadf3+' +key}"
													value="%{adf3}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf4</label>
												<s:textfield
													name="adf4"
													id="%{'idadf4+' +key}"
													value="%{adf4}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf5</label>
												<s:textfield
													name="adf5"
													id="%{'idadf5+' +key}"
													value="%{adf5}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf6</label>
												<s:textfield
													name="adf6"
													id="%{'idadf6+' +key}"
													value="%{adf6}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf7</label>
												<s:textfield
													name="adf7"
													id="%{'idadf7+' +key}"
													value="%{adf7}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf8</label>
												<s:textfield
													name="adf8"
													id="%{'idadf8+' +key}"
													value="%{adf8}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf9</label>
												<s:textfield
													name="adf9"
													id="%{'idadf9+' +key}"
													value="%{adf9}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf10</label>
												<s:textfield
													name="adf10"
													id="%{'idadf10+' +key}"
													value="%{adf10}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
	
										<div class="col-md-3 mb-20">
											<div class="lpay_input_group">
												<label for="">Adf11</label>
												<s:textfield
													name="adf11"
													id="%{'idadf11+' +key}"
													value="%{adf11}"
													placeholder=""
													autocomplete="off"
													class="lpay_input">
												</s:textfield>
											</div>
											<!-- /.lpay_input_group -->
										</div>
										<!-- /.col-md-4 -->
									</s:div>
								</s:div>
							</s:iterator>
	
							<div id="paymentsRegionDiv" style="display: none;">
								<span style="display: none;">Transaction Region</span>

								<s:checkbox
									name="international"
									id="international"
									style="display:inline; margin-left:20px;"
									type="text" value="1">
								</s:checkbox>	
								<span class="checkbox-align">International</span>

								<s:checkbox
									name="domestic"
									id="domestic"
									style="display:inline; margin-left:20px;"
									type="text"
									value="1">
								</s:checkbox>								
								<span class="checkbox-align">Domestic</span>
							</div>
						</div>
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</section>
			<!-- /.lpay_section white-bg box-shadow-box mt-70 p20 -->
	
			<section class="mt-20">
				<div class="row">
					<div class="col-md-12">
						<div id="paymentCheck">
							<s:iterator value="mopList" status="itStatus">
								<ul class="filters lpay_section white-bg box-shadow-box mb-20 p-20 pb-10">
									<li>
										<div class="payment-types">
											<label class="checkbox-label unchecked mb-0">
												<s:checkbox
													name="paymentSelectedList"
													type="checkbox"
													id="%{key+'box'}"
													value="false"
													onclick="showMe('%{key}', this)"
													cssClass="roundedTwo">
												</s:checkbox> 
												<s:property value="key" />
											</label>
										</div>

										<s:div id="%{key}" cssStyle="display:none;" cssClass="pl-20 mt-10 flex-wrap">
											<s:if test="%{key=='Net Banking'}">												
												<label class="checkbox-label unchecked mb-10">
													<s:checkbox
														name="selectAll"
														value="false"
														id="id+selectAllButton"
														fieldValue="selectAll"
														label="All"
														onclick="selectAllCheckboxesWithinDiv('%{key}')">
													</s:checkbox>
													<span class="redsmalltext"><b>Select All</b></span>
												</label>
											</s:if>
	
											<s:iterator value="value" status="mopStatus">
												<s:if test="%{[1].key!=('Net Banking')}">
													<div class="payment-boxes">
														<label class="mop-type checkbox-label unchecked mb-0">
															<s:checkbox
																name="mopSelectedList"
																id="id+%{[1].key+'-'+code}"
																fieldValue="%{name}"
																label="%{name}"
																value="false"
																onclick="showMe('%{[1].key+'-'+code}', this)">
															</s:checkbox> 
															<s:property value="name" />
														</label>
														<s:div id="%{[1].key+'-'+code}" cssStyle="display:none;" cssClass="pl-20 pt-10">
															<s:iterator value="transList" status="txnStatus">
																<s:if test="%{code == 'SALE'}">
																	<label class="txn-type checkbox-label unchecked mb-10">
																		<s:checkbox
																			id="%{[2].key+'-'+[1].code+'-'+code}"
																			name="txnSelectedList"
																			fieldValue="%{name}"
																			label="%{name}"
																			value="false">
																		</s:checkbox>
																		<s:property value="name" />
																	</label>
																</s:if>
															</s:iterator>
														</s:div>
													</div>	
												</s:if>
	
												<s:else>
													<s:div cssClass="col-md-3">
														<label class="netbankinglabel checkbox-label unchecked mb-10">
															<s:checkbox
																name="mopSelectedList"
																id="%{key+'-'+code}"
																fieldValue="%{name}"
																label="%{name}"
																value="false">
															</s:checkbox>
															<s:property value="name" />
														</label>
													</s:div>
												</s:else>
											</s:iterator>
										</s:div>
									</li>									
								</ul>
							</s:iterator>
						</div>
						<!-- #paymentCheck -->
					</div>
					<!-- /.col-md-12 -->
				</div>
				<!-- /.row -->
			</section>
			<!-- /.lpay_section white-bg box-shadow-box mt-70 p20 -->
	
			<div class="row">
				<div class="col-md-12 text-center">
					<button class="lpay_button lpay_button-md lpay_button-primary" id="btnsubmit">Save</button>
				</div>
				<!-- /.col-md-12 -->
			</div>
			<!-- /.row -->
		</div>
		<!-- /.mop-setup-box -->
		
		

		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
		<s:hidden id="userType" value="%{#session.USER.UserType.name()}"></s:hidden>
		<s:hidden id="userEmail" value="%{#session.USER.EmailId}"></s:hidden>
	</s:form>

	<script src="../js/mapping.js"></script>
</body>
</html>
