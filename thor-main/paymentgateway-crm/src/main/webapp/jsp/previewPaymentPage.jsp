<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
<title><s:property value="%{#session.pageTittle}" /></title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/credit-card-form.css" media="all" type="text/css" rel="stylesheet" />
<script type="text/javascript" src="../js/credit-card-form.js"></script>
<style>
.textstyle, h1, h2, h3, input {	font-family: <s:property value = "%{dynamicPaymentPage.textStyle}" /> !important; }
.textcolor, h1, h2, h3, ul.nav-tabs>li>a, p, small { color: #<s:property value="%{dynamicPaymentPage.textColor}"/> !important; }
.background {background-color: #<s:property value="%{dynamicPaymentPage.backgroundColor}"/>;}
a:link {color: #<s:property value="%{dynamicPaymentPage.hyperlinkColor}"/> !important;}
.boxbackgroundcolor {background-color: #<s:property value="%{dynamicPaymentPage.boxBackgroundColor}"/> !important;}
.topbarcolor {background-color: #<s:property value="%{dynamicPaymentPage.topBarColor}"/> !important;}
ul.nav-tabs>li>a {color: #<s:property value="%{dynamicPaymentPage.tabTextColor}"/> !important; background-color: #<s:property value="%{dynamicPaymentPage.tabBackgroundColor}"/>; }
ul.nav-tabs>li>a:hover {color: #<s:property value="%{dynamicPaymentPage.tabTextColor}"/> !important; background-color: #<s:property value="%{dynamicPaymentPage.tabBackgroundColor}"/>; }
.nav-tabs>li.active>a, .nav-tabs>li.active>a:focus {color: #<s:property value="%{dynamicPaymentPage.activeTabTextColor}"/> !important;	background-color: #<s:property value="%{dynamicPaymentPage.activeTabColor}"/>;}
.buttoncolor {background-color: #<s:property value="%{dynamicPaymentPage.buttonBackgoundColor}"/> !important; border: none; color: #<s:property value="%{dynamicPaymentPage.buttonTextColor}"/> !important; }
.bordercolor, ul, .panel-heading {border-color: #<s:property value="%{dynamicPaymentPage.borderColor}"/> !important; }
</style>
</head>
<body class="background" >
	<div class="center-div">
	<div class="text-right">
	<s:label name="lblSessionTime" id="lblSessionTime" value="Your Session will expire in 14:15"></s:label>
				
			</div>
		<div class="container textstyle textcolor">
			<div class="panel panel-default bordercolor">

				<div class="panel-heading custom_class topbarcolor">
					<h1 class="panel-title pull-left" style="padding-top: 6px; color:#ffffff !important;">
						<b>( Payment : INR 100.00 )</b>
					</h1>

					<div class="pull-right">

						<form action="locale">

							<select class="form-control1 pull-right">
							<option>English</option>
							<option>Hindi</option>
							<option>Punjabi</option>
						</select>
						</form>
					</div>
					<div class="clearfix"></div>
				</div>

				<div class="panel-body boxbackgroundcolor" style="margin: 0px; padding: 6px;">

					<ul class="nav nav-tabs responsive" id="myTab"
						style="background-color: #ededed; border-radius: 4px;">
						
						<li class="active"><a href="#tab2default" data-toggle="tab" style="color: !important;">Credit Card</a></li>
						<li ><a href="#tab1default" data-toggle="tab" style="color: !important;">Debit Card</a></li>
						<li><a href="#tab3default" data-toggle="tab" style="color: !important;">Net Banking</a></li>
						<li><a href="#tab4default" data-toggle="tab" style="color: !important;">Wallet</a></li>
						
					</ul>

					<div class="tab-content active responsive">
  						<div class="tab-pane fade in active" id="tab2default">
							<h3>Credit Card</h3>
							<div class="clearfix">&nbsp;</div>
			
							<div class="row">
								<div class="form-group col-md-10">
									<div class="cardNumber__wrap">
										<input type="hidden" id="dccardNumber1" name="cardNumber" /> <input id="dccardNumber"
											name="cardNumber" class="cardNumber form-control"
											onblur="validateDebit(this);checkLuhnDebit(this);"
											onkeyup="enablePayButtonDebit(this);return isNumberKeyCardDebit(this);"
											autocomplete="off" placeholder="CARD NUMBER" />
										<div class="card" aria-hidden="true"></div>
									</div>
								</div>
							</div>
							<div class="row">
								<div class="form-group col-md-3 col-sm-4 col-xs-6">
									<s:select headerKey="1" headerValue="%{getText('Month')}" cssClass="form-control"
										list="#{'01':'01','02':'02','03':'03','04':'04','05':'05','06':'06','07':'07','08':'08','09':'09','10':'10','11':'11','12':'12'}"
										name="ExpiryMonth" id="dcExpiryMonth" value="0"
										onchange="CheckExpiryDC();enablePayButtonDebit(this);" />
								</div>
								<div class="form-group col-md-3 col-sm-4 col-xs-6">
									<s:select headerKey="1" headerValue="%{getText('YEAR')}" cssClass="form-control"
										list="#{'2016':'2016','2017':'2017','2018':'2018','2019':'2019','2020':'2020','2021':'2021','2022':'2022','2023':'2023','2024':'2024','2025':'2025','2026':'2026','2027':'2027','2028':'2028','2029':'2029','2030':'2030','2031':'2031','2032':'2032','2033':'2033','2034':'2034','2035':'2035','2036':'2036','2037':'2037','2038':'2038','2039':'2039','2040':'2040','2041':'2041','2042':'2042','2043':'2043','2044':'2044','2045':'2045','2046':'2046','2047':'2047','2048':'2048','2049':'2049'}"
										name="ExpiryYear" id="dcExpiryYear" value="0"
										onchange="CheckExpiryDC();enablePayButtonDebit(this);" />
								</div>
								<div class="form-group col-md-2 col-sm-2 col-xs-6">
									<input type='password' style='display: none' />
									<s:textfield type="password" maxlength="3" name="cvvNumber" id="dccvvNumber"
										autocomplete="off" onkeypress="return isNumberKey(event)"
										onkeyup="enablePayButtonDebit(this);" cssClass="form-control" placeholder="CVV" />
									<input type='password' style='display: none'>
								</div>
								<div class="form-group col-md-2 col-sm-2 col-xs-6">
									<s:set name="cvv textimageDD" value="getText('CVVlen3image')" />
									<s:hidden id="cvvTextImageDD" value="%{cvv textimageDD}" />
									<span onMouseOut="HideDC()" onMouseOver="ShowDC()" onMouseMove="ShowDC()" class="cvvcard">&nbsp;</span>
									<div id="cvvtextDD"></div>
								</div>
							</div>
							<div class="row">
								<div class="form-group col-md-10">
									<s:textfield id="dccardName" name="cardName" type="text" cssClass="form-control"
										autocomplete="off" onkeypress="noSpaceDebit(event);return isCharacterKey(event)"
										onkeyup="enablePayButtonDebit(this);" placeholder="NAME ON CARD" />
								</div>
							</div>
							<div class="row">
								<div class="form-group col-md-1 col-xs-1">
									<s:checkbox name="cardsaveflag" checked="checked" id="cardsaveflag1" />
								</div>
								<div class="form-group col-md-11 col-xs-10 text-left" style="padding-left:0px;">
									<label for="cardsaveflag1" class="text-muted"><s:property
											value="getText('SaveCard')" /></label>
								</div>
							</div>
							<div class="row">
								<div class="form-group col-md-4 col-sm-12 col-xs-12">
									<s:submit key="Pay" name="dcSubmit" cssClass="btn-orange buttoncolor disabled"
										 theme="simple" formtarget="_parent" id="dcSubmit"></s:submit>
								</div>
							</div>
							<div class="row">
							<div class="form-group col-md-10 col-sm-12 col-xs-12">
							<a href="#">Return to merchant</a></div>
						</div>
						</div>
						
							
							<div class="tab-pane fade" id="tab1default">
							<h3>Debit Card</h3>
							<div class="clearfix">&nbsp;</div>
							<div class="row">
							
							</div>
							<div class="row">
								<div class="form-group col-md-10">
									<div class="cardNumber__wrap">
										<input type="hidden" id="dccardNumber1" name="cardNumber" /> <input id="dccardNumber"
											name="cardNumber" class="cardNumber form-control"
											onblur="validateDebit(this);checkLuhnDebit(this);"
											onkeyup="enablePayButtonDebit(this);return isNumberKeyCardDebit(this);"
											autocomplete="off" placeholder="CARD NUMBER" />
										<div class="card" aria-hidden="true"></div>
									</div>
								</div>
							</div>
							<div class="row">
								<div class="form-group col-md-3 col-sm-4 col-xs-6">
									<s:select headerKey="1" headerValue="%{getText('Month')}" cssClass="form-control"
										list="#{'01':'01','02':'02','03':'03','04':'04','05':'05','06':'06','07':'07','08':'08','09':'09','10':'10','11':'11','12':'12'}"
										name="ExpiryMonth" id="dcExpiryMonth" value="0"
										onchange="CheckExpiryDC();enablePayButtonDebit(this);" />
								</div>
								<div class="form-group col-md-3 col-sm-4 col-xs-6">
									<s:select headerKey="1" headerValue="%{getText('YEAR')}" cssClass="form-control"
										list="#{'2016':'2016','2017':'2017','2018':'2018','2019':'2019','2020':'2020','2021':'2021','2022':'2022','2023':'2023','2024':'2024','2025':'2025','2026':'2026','2027':'2027','2028':'2028','2029':'2029','2030':'2030','2031':'2031','2032':'2032','2033':'2033','2034':'2034','2035':'2035','2036':'2036','2037':'2037','2038':'2038','2039':'2039','2040':'2040','2041':'2041','2042':'2042','2043':'2043','2044':'2044','2045':'2045','2046':'2046','2047':'2047','2048':'2048','2049':'2049'}"
										name="ExpiryYear" id="dcExpiryYear" value="0"
										onchange="CheckExpiryDC();enablePayButtonDebit(this);" />
								</div>
								<div class="form-group col-md-2 col-sm-2 col-xs-6">
									<input type='password' style='display: none' />
									<s:textfield type="password" maxlength="3" name="cvvNumber" id="dccvvNumber"
										autocomplete="off" onkeypress="return isNumberKey(event)"
										onkeyup="enablePayButtonDebit(this);" cssClass="form-control" placeholder="CVV" />
									<input type='password' style='display: none'>
								</div>
								<div class="form-group col-md-2 col-sm-2 col-xs-6">
									<s:set name="cvv textimageDD" value="getText('CVVlen3image')" />
									<s:hidden id="cvvTextImageDD" value="%{cvv textimageDD}" />
									<span onMouseOut="HideDC()" onMouseOver="ShowDC()" onMouseMove="ShowDC()" class="cvvcard">&nbsp;</span>
									<div id="cvvtextDD"></div>
								</div>
							</div>
							<div class="row">
								<div class="form-group col-md-10">
									<s:textfield id="dccardName" name="cardName" type="text" cssClass="form-control"
										autocomplete="off" onkeypress="noSpaceDebit(event);return isCharacterKey(event)"
										onkeyup="enablePayButtonDebit(this);" placeholder="NAME ON CARD" />
								</div>
							</div>
							<div class="row">
								<div class="form-group col-md-1 col-xs-1">
									<s:checkbox name="cardsaveflag" checked="checked" id="cardsaveflag" />
								</div>
								<div class="form-group col-md-11 col-xs-10 text-left" style="padding-left:0px;">
									<label for="cardsaveflag" class="text-muted"><s:property
											value="getText('SaveCard')" /></label>
								</div>
							</div>
							<div class="row">
								<div class="form-group col-md-4 col-sm-12 col-xs-12">
									<s:submit key="Pay" name="dcSubmit" cssClass="btn-orange buttoncolor disabled"
										style="background-color:;" theme="simple" formtarget="_parent" id="dcSubmit"></s:submit>
								</div>
							</div>
							<div class="row">
							<div class="form-group col-md-10 col-sm-12 col-xs-12">
							<a href="#">Return to merchant</a></div>
						</div>
						</div>
<div class="tab-pane fade" id="tab3default"><h3>Net Banking</h3>						
						<div class="text-muted"><small>Note:<br />

On clicking "Pay", you will be redirected to your bank's site for authorization.<br /> 
Please do not refresh page or click back button on browser
 while the authorization <br />process is being undertaken.</small></div>
<div class="clearfix">&nbsp;</div>
 <div class="row">
								<div class="form-group col-md-12 col-xs-12">
									<div class="mk-trc" data-style="radio" data-radius="true">
										<input name="netBankingType" id="sbi" onclick="onChange('sbi');enableNetbankingButton()" type="radio" value="SBI">
										<label for="sbi"><i></i> <img src="../image/sbi-logo.png" class=""></label>
									</div>
									<div class="mk-trc" data-style="radio" data-radius="true">
										<input name="netBankingType" id="hdfc" onclick="onChange('hdfc');enableNetbankingButton()" type="radio" value="HDFC">
										<label for="hdfc"><i></i> <img src="../image/hdfc_new.png" class=""></label>
									</div>
									<div class="mk-trc" data-style="radio" data-radius="true">
										<input name="netBankingType" id="icici" onclick="onChange('icici');enableNetbankingButton()" type="radio" value="ICICI">
										<label for="icici"><i></i>  <img src="../image/icici_new.png" class=""></label>
									</div>
									</div></div>
									 <div class="row"><div class="form-group col-md-12">
									<div class="mk-trc" data-style="radio" data-radius="true">
										<input 	name="netBankingType" type="radio" value="AXIS" id="axis" onclick="onChange('axis');enableNetbankingButton()">
										<label for="axis"><i></i> <img src="../image/axis_new.png" class=""></label>
									</div>
									<div class="mk-trc" data-style="radio" data-radius="true">
										<input name="netBankingType" type="radio" value="CITI" id="citi" onclick="onChange('citi');enableNetbankingButton()">
										<label for="citi"><i></i> <img src="../image/citibank-netbnk.png" class=""></label>
									</div>
									<div class="mk-trc" data-style="radio" data-radius="true">
										<input id="kotak" onclick="onChange('kotak');enableNetbankingButton()" name="netBankingType" type="radio" value="KOTAK">
										<label for="kotak"><i></i> <img src="../image/kotak_new.png" class=""></label>
									</div>
								</div>
							</div>
							 <hr />
							 <div class="row">
								<div class="form-group col-md-10 col-sm-12 col-xs-12">
									<select class="form-control">
									<option selected="selected">OTHER BANKS</option>
									<option></option>
									<option></option>
									<option></option>
									</select>
								</div>
							</div>
							<div class="row">
								<div class="form-group col-md-4 col-sm-12 col-xs-12">
									<s:submit key="Pay" value="Pay" name="dcSubmit" cssClass="btn-orange buttoncolor disabled"
										 theme="simple" formtarget="_parent" id="dcSubmit"></s:submit>
								</div>
							</div>
							<div class="row">
							<div class="form-group col-md-10 col-sm-12 col-xs-12">
							<a href="#">Return to merchant</a></div>
						</div></div>


<div class="tab-pane fade" id="tab4default"><h3>Wallet</h3>
						<div class="clearfix">&nbsp;</div>
						<div class="row">
								<div class="form-group col-md-12 col-xs-12">
									<div class="mk-trc" data-style="radio" data-radius="true">
										<input  id="mobikwikWallet" type="radio" name="mopType" onchange="enableWalletButton()" value="MWL" />
										<label for="mobikwikWallet"><i></i> <img src="../image/mobikwik.png" width="100" /></label>
									</div>
									<div class="mk-trc" data-style="radio" data-radius="true">
									<input  id="paytmWallet" name="mopType" type="radio" onchange="enableWalletButton()" value="PPI" />
									<label for="paytmWallet" class="css-label radGroup1"><i></i> <img src="../image/paytm-logo.png" width="80" /></label>
									</div>									
									</div></div>
									<hr />
									<div class="row">
								<div class="form-group col-md-4 col-sm-12 col-xs-12">
									<s:submit type="submit" name="button" id="walletSubmit" key="Pay" class="btn btn-block sharp btn-warning btn-md" ></s:submit>
								</div>
							</div>
							<div class="row">
							<div class="form-group col-md-10 col-sm-12 col-xs-12">
							<a href="#">Return to merchant</a></div>
						</div></div>
											</div>


					<ul class="nav pull-right formobiles">
						<li class="text-center">
						<%-- <img src="../image/userlogo/<%=(String) session.getAttribute(FieldType.PAY_ID.getName())%>.png" width="180px" /> --%>
							<table class="table table-bordered" style="background-color: #fbfbfb; font-size: 11px;">
								<tr>
									<td width="40%" align="left" valign="middle">Buyer :</td>
									<td width="60%" align="left" valign="middle">John</td>
								</tr>
								<tr>
									<td align="left" valign="middle">Order ID :</td>
									<td align="left" valign="middle">SIGORD101</td>
								</tr>
								   
							</table>
							</li>
					</ul>
				</div>
				<div class="pull-right" style="margin-top: -60px;">
					<img src="../image/ssl-logos.png" class="img-responsive">
				</div>
			</div>
		</div>
	</div>	

	<script src="../js/bootstrap.min.js"></script>
	<script src="../js/responsive-tabs.js"></script>
	<script type="text/javascript">
		$('#myTab a').click(function(e) {
			e.preventDefault();
			$(this).tab('show');
		});
		$('#moreTabs a').click(function(e) {
			e.preventDefault();
			$(this).tab('show');
		});
		(function($) { // Test for making sure event are maintained
			$('.js-alert-test').click(function() {
				alert('Button Clicked: Event was maintained');
			});
			fakewaffle.responsiveTabs([ 'xs', 'sm' ]);
		})(jQuery);
	</script>
	<script>
		$(document).ready(function() {
			$('[data-toggle="tooltip"]').tooltip();
		});
	</script>
	<script>
$('.hoverDiv').click(function() {
    $(this).addClass('active').siblings().removeClass('active');
})
</script>
</body>
</html>