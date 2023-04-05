<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Restrictions</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/jquery-ui.css" rel="stylesheet" />
<script src="../js/jquery.min.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/amcharts.js"></script>
<script src="../js/pie.js"></script>
<script src="../js/light.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" href="../css/popup.css" /></head>
<script type="text/javascript">
$(document).ready(function() {
	$('#popupButton').click(function(){
        $('#popup').popup('show');
   });
	$('#popupButton2').click(function(){
        $('#popup2').popup('show');
   });
	$('#popupButton3').click(function(){
        $('#popup3').popup('show');
   });
	$('#popupButton4').click(function(){
        $('#popup4').popup('show');
   });
	$('#popupButton5').click(function(){
        $('#popup5').popup('show');
   });
	$('#popupButton6').click(function(){
        $('#popup6').popup('show');
   });
	$('#popupButton7').click(function(){
        $('#popup7').popup('show');
   });
	$('#popupButton8').click(function(){
        $('#popup8').popup('show');
   });
});
</script>
<body>
<div id="popup" style="display:none;">
	<div class="modal-dialog" style="width:400px;">

	<!-- Modal content-->
	<div class="modal-content" style="background-color:transparent; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		<div class="modal-body" style="background-color:#ffffff; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		
	<table class="detailbox table98" cellpadding="20">
		<tr>
							<th colspan="2" width="16%" height="30" align="left"
								style="background-color: #002163; color: #ffffff; border-top-right-radius:0px !important;">Add a blocked IP address</th></tr>
								<tr>
							<td colspan="2" height="30" align="left"><p>Enter the IP address and subnet mask you wish to block.<br><br>

Both the IP address and subnet mask should be zero padded, e.g. 127.000.000.001<br><br>

Once added, all transactions from the IP address / subnet mask range will be blocked.</p></td></tr>	
 <tr>
							<td width="7%">IP Address *</td>
							<td width="30%"><input type="text" class="form-control"></td>
							</tr>
  <tr>
    <td>Subnet mask *</td>
    <td><input type="text" class="form-control"></td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td><input type="submit" name="remittSubmit" value="Add" id="" class="btn btn-success btn-sm" /></td>
  </tr>
							
					</table></div></div></div>
</div>
<div id="popup2" style="display:none;">
	<div class="modal-dialog" style="width:400px;">

	<!-- Modal content-->
	<div class="modal-content" style="background-color:transparent; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		<div class="modal-body" style="background-color:#ffffff; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		
	<table class="detailbox table98" cellpadding="20">
		<tr>
							<th colspan="2" width="16%" height="30" align="left"
								style="background-color: #002163; color: #ffffff; border-top-right-radius:0px !important;">Add a blocked issuing Countries</th></tr>								
 <tr>
							<td width="7%">Countries *</td>
							<td width="30%"><select name="s2countrycitizens" id="s2countrycitizens" class="form-control">
	<option value="SELECT">SELECT</option>
	<option value="Antigua Barbuda">Antigua Barbuda</option>
	<option value="Argentina"> Argentina</option>
	<option value="Armenia">Armenia</option>
	<option value=" Australia"> Australia </option>
	<option value=" Austria">Austria </option>
	<option value=" Azerbaijan"> Azerbaijan </option>
	<option value=" Bahrain"> Bahrain </option>
	<option value=" Belgium"> Belgium </option>
	<option value=" Botswana"> Botswana </option>
	<option value=" Brazil"> Brazil </option>
	<option value=" Brunei Darussalam "> Brunei Darussalam </option>
	<option value=" Cambodia "> Cambodia </option>
	<option value=" Cameroon "> Cameroon </option>
	<option value=" Canada "> Canada </option>
	<option value=" Chile "> Chile </option>
	<option value=" China "> China </option>
	<option value=" Colombia"> Colombia </option>
	<option value=" Croatia "> Croatia </option>
	<option value=" Cuba "> Cuba </option>
	<option value=" Cyprus "> Cyprus </option>
	<option value=" Czech Republic "> Czech Republic </option>
	<option value=" Denmark "> Denmark </option>
	<option value=" Egypt "> Egypt </option>
	<option value=" Estonia "> Estonia </option>
	<option value=" Ethiopia "> Ethiopia </option>
	<option value=" Fiji "> Fiji </option>
	<option value=" Finland "> Finland </option>
	<option value=" France "> France </option>
	<option value=" Germany "> Germany </option>
	<option value=" Ghana "> Ghana </option>
	<option value=" Greece "> Greece </option>
	<option value=" Hong Kong "> Hong Kong </option>
	<option value=" Hungary "> Hungary </option>
	<option value=" Iceland "> Iceland </option>
	<option value=" India "> India </option>
	<option value=" Indonesia "> Indonesia </option>
	<option value=" Ireland "> Ireland </option>
	<option value=" Italy "> Italy </option>
	<option value=" Jamaica "> Jamaica </option>
	<option value=" Japan "> Japan </option>
	<option value=" Jordan "> Jordan </option>
	<option value=" Kazakhstan "> Kazakhstan </option>
	<option value=" Kenya "> Kenya </option>
	<option value=" Kuwait "> Kuwait </option>
	<option value=" Kyrgyzstan "> Kyrgyzstan </option>
	<option value=" Laos "> Laos </option>
	<option value=" Latvia "> Latvia </option>
	<option value="Lebanon "> Lebanon </option>
	<option value="Libya">Libya</option>
	<option value="Liechtenstein">Liechtenstein</option>
	<option value="Lithuania">Lithuania</option>
	<option value="Luxembourg">Luxembourg</option>
	<option value="Madagascar">Madagascar</option>
	<option value="Maldives">Maldives</option>
	<option value="Malaysia">Malaysia</option>
	<option value="Malta">Malta</option>
	<option value="Mauritius">Mauritius</option>
	<option value="Monaco">Monaco</option>
	<option value="Mongolia">Mongolia</option>
	<option value="Morocco">Morocco</option>
	<option value="Mozambique">Mozambique</option>
	<option value="Myanmar (Burma)">Myanmar (Burma)</option>
	<option value="Namibia">Namibia</option>
	<option value="Nepal">Nepal</option>
	<option value="Netherlands">Netherlands</option>
	<option value="New Zealand">New Zealand</option>
	<option value="Nigeria">Nigeria</option>
	<option value="Norway">Norway</option>
	<option value="Oman">Oman</option>
	<option value="Pakistan">Pakistan</option>
	<option value="Peru">Peru</option>
	<option value="Philippines">Philippines</option>
	<option value="Poland">Poland</option>
	<option value="Portugal">Portugal</option>
	<option value="Qatar">Qatar</option>
	<option value="Russia">Russia</option>
	<option value="Singapore">Singapore</option>
	<option value="Slovakia">Slovakia</option>
	<option value="Slovenia">Slovenia</option>
	<option value="South Africa">South Africa</option>
	<option value="South Korea">South Korea</option>
	<option value="Spain">Spain</option>
	<option value="Sri Lanka">Sri Lanka</option>
	<option value="Sweden">Sweden</option>
	<option value="Switzerland">Switzerland</option>
	<option value="Taiwan">Taiwan</option>
	<option value="Tajikistan">Tajikistan</option>
	<option value="Tanzania">Tanzania</option>
	<option value="Thailand">Thailand</option>
	<option value="Tunisia">Tunisia</option>
	<option value="Turkey">Turkey</option>
	<option value="Turkmenistan">Turkmenistan</option>
	<option value="United Arab Emirates">United Arab Emirates</option>
	<option value="Ukraine">Ukraine</option>
	<option value="United Kingdom">United Kingdom</option>
	<option value="UK">UK</option>
	<option value="United States of America">United States of America</option>
	<option value="USA">USA</option>
	<option value="Uzbekistan">Uzbekistan</option>
	<option value="Vietnam">Vietnam</option>
	<option value="Zimbabwe">Zimbabwe</option>

</select>
</td>
							</tr>
  <tr>
    <td>&nbsp;</td>
    <td><input type="submit" name="remittSubmit" value="Add" id="" class="btn btn-success btn-sm" /></td>
  </tr>
							
					</table></div></div></div>
</div>
<div id="popup3" style="display:none;">
	<div class="modal-dialog" style="width:400px;">

	<!-- Modal content-->
	<div class="modal-content" style="background-color:transparent; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		<div class="modal-body" style="background-color:#ffffff; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		
	<table class="detailbox table98" cellpadding="20">
		<tr>
							<th colspan="2" width="16%" height="30" align="left"
								style="background-color: #002163; color: #ffffff; border-top-right-radius:0px !important;">Add a blocked card range</th></tr>
								<tr>
							<td colspan="2" height="30" align="left"><p>Enter the first 6 digits of the card number you wish to block;<br><br>

Once added, all transactions within that card range will be blocked.</p></td></tr>	
 <tr>
							<td width="7%">Card range *</td>
							<td width="30%"><input type="text" class="form-control"></td>
							</tr>  
  <tr>
    <td>&nbsp;</td>
    <td><input type="submit" name="remittSubmit" value="Add" id="" class="btn btn-success btn-sm" /></td>
  </tr>
							
					</table></div></div></div>
</div>
<div id="popup4" style="display:none;">
	<div class="modal-dialog" style="width:400px;">

	<!-- Modal content-->
	<div class="modal-content" style="background-color:transparent; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		<div class="modal-body" style="background-color:#ffffff; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		
	<table class="detailbox table98" cellpadding="20">
		<tr>
							<th colspan="2" width="16%" height="30" align="left"
								style="background-color: #002163; color: #ffffff; border-top-right-radius:0px !important;">Add a blocked issuing country</th></tr>								
 <tr>
							<td width="7%">Countries *</td>
							<td width="30%"><select name="s2countrycitizens" id="s2countrycitizens" class="form-control">
	<option value="SELECT">SELECT</option>
	<option value="Antigua Barbuda">Antigua Barbuda</option>
	<option value="Argentina"> Argentina</option>
	<option value="Armenia">Armenia</option>
	<option value=" Australia"> Australia </option>
	<option value=" Austria">Austria </option>
	<option value=" Azerbaijan"> Azerbaijan </option>
	<option value=" Bahrain"> Bahrain </option>
	<option value=" Belgium"> Belgium </option>
	<option value=" Botswana"> Botswana </option>
	<option value=" Brazil"> Brazil </option>
	<option value=" Brunei Darussalam "> Brunei Darussalam </option>
	<option value=" Cambodia "> Cambodia </option>
	<option value=" Cameroon "> Cameroon </option>
	<option value=" Canada "> Canada </option>
	<option value=" Chile "> Chile </option>
	<option value=" China "> China </option>
	<option value=" Colombia"> Colombia </option>
	<option value=" Croatia "> Croatia </option>
	<option value=" Cuba "> Cuba </option>
	<option value=" Cyprus "> Cyprus </option>
	<option value=" Czech Republic "> Czech Republic </option>
	<option value=" Denmark "> Denmark </option>
	<option value=" Egypt "> Egypt </option>
	<option value=" Estonia "> Estonia </option>
	<option value=" Ethiopia "> Ethiopia </option>
	<option value=" Fiji "> Fiji </option>
	<option value=" Finland "> Finland </option>
	<option value=" France "> France </option>
	<option value=" Germany "> Germany </option>
	<option value=" Ghana "> Ghana </option>
	<option value=" Greece "> Greece </option>
	<option value=" Hong Kong "> Hong Kong </option>
	<option value=" Hungary "> Hungary </option>
	<option value=" Iceland "> Iceland </option>
	<option value=" India "> India </option>
	<option value=" Indonesia "> Indonesia </option>
	<option value=" Ireland "> Ireland </option>
	<option value=" Italy "> Italy </option>
	<option value=" Jamaica "> Jamaica </option>
	<option value=" Japan "> Japan </option>
	<option value=" Jordan "> Jordan </option>
	<option value=" Kazakhstan "> Kazakhstan </option>
	<option value=" Kenya "> Kenya </option>
	<option value=" Kuwait "> Kuwait </option>
	<option value=" Kyrgyzstan "> Kyrgyzstan </option>
	<option value=" Laos "> Laos </option>
	<option value=" Latvia "> Latvia </option>
	<option value="Lebanon "> Lebanon </option>
	<option value="Libya">Libya</option>
	<option value="Liechtenstein">Liechtenstein</option>
	<option value="Lithuania">Lithuania</option>
	<option value="Luxembourg">Luxembourg</option>
	<option value="Madagascar">Madagascar</option>
	<option value="Maldives">Maldives</option>
	<option value="Malaysia">Malaysia</option>
	<option value="Malta">Malta</option>
	<option value="Mauritius">Mauritius</option>
	<option value="Monaco">Monaco</option>
	<option value="Mongolia">Mongolia</option>
	<option value="Morocco">Morocco</option>
	<option value="Mozambique">Mozambique</option>
	<option value="Myanmar (Burma)">Myanmar (Burma)</option>
	<option value="Namibia">Namibia</option>
	<option value="Nepal">Nepal</option>
	<option value="Netherlands">Netherlands</option>
	<option value="New Zealand">New Zealand</option>
	<option value="Nigeria">Nigeria</option>
	<option value="Norway">Norway</option>
	<option value="Oman">Oman</option>
	<option value="Pakistan">Pakistan</option>
	<option value="Peru">Peru</option>
	<option value="Philippines">Philippines</option>
	<option value="Poland">Poland</option>
	<option value="Portugal">Portugal</option>
	<option value="Qatar">Qatar</option>
	<option value="Russia">Russia</option>
	<option value="Singapore">Singapore</option>
	<option value="Slovakia">Slovakia</option>
	<option value="Slovenia">Slovenia</option>
	<option value="South Africa">South Africa</option>
	<option value="South Korea">South Korea</option>
	<option value="Spain">Spain</option>
	<option value="Sri Lanka">Sri Lanka</option>
	<option value="Sweden">Sweden</option>
	<option value="Switzerland">Switzerland</option>
	<option value="Taiwan">Taiwan</option>
	<option value="Tajikistan">Tajikistan</option>
	<option value="Tanzania">Tanzania</option>
	<option value="Thailand">Thailand</option>
	<option value="Tunisia">Tunisia</option>
	<option value="Turkey">Turkey</option>
	<option value="Turkmenistan">Turkmenistan</option>
	<option value="United Arab Emirates">United Arab Emirates</option>
	<option value="Ukraine">Ukraine</option>
	<option value="United Kingdom">United Kingdom</option>
	<option value="UK">UK</option>
	<option value="United States of America">United States of America</option>
	<option value="USA">USA</option>
	<option value="Uzbekistan">Uzbekistan</option>
	<option value="Vietnam">Vietnam</option>
	<option value="Zimbabwe">Zimbabwe</option>

</select>
</td>
							</tr> 
  <tr>
    <td>&nbsp;</td>
    <td><input type="submit" name="remittSubmit" value="Add" id="" class="btn btn-success btn-sm" /></td>
  </tr>
							
					</table></div></div></div>
</div>
<div id="popup5" style="display:none;">
	<div class="modal-dialog" style="width:400px;">

	<!-- Modal content-->
	<div class="modal-content" style="background-color:transparent; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		<div class="modal-body" style="background-color:#ffffff; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		
	<table class="detailbox table98" cellpadding="20">
		<tr>
							<th colspan="2" width="16%" height="30" align="left"
								style="background-color: #002163; color: #ffffff; border-top-right-radius:0px !important;">Add a blocked email address</th></tr>								
 <tr>
							<td width="7%">Email *</td>
							<td width="30%"><input type="text" class="form-control"></td>
							</tr>  
  <tr>
  <td>&nbsp;</td>
    <td><input type="submit" name="remittSubmit" value="Add" id="" class="btn btn-success btn-sm" /></td>
  </tr>
							
					</table></div></div></div>
</div>
<div id="popup6" style="display:none;">
	<div class="modal-dialog" style="width:400px;">

	<!-- Modal content-->
	<div class="modal-content" style="background-color:transparent; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		<div class="modal-body" style="background-color:#ffffff; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		
	<table class="detailbox table98" cellpadding="20">
		<tr>
							<th colspan="2" width="16%" height="30" align="left"
								style="background-color: #002163; color: #ffffff; border-top-right-radius:0px !important;">Add a Blocked Domains</th></tr>								
 <tr>
							<td width="7%">Domain *</td>
							<td width="30%"><input type="text" class="form-control"></td>
							</tr>  
  <tr>
  <td>&nbsp;</td>
    <td><input type="submit" name="remittSubmit" value="Add" id="" class="btn btn-success btn-sm" /></td>
  </tr>
							
					</table></div></div></div>
</div>
<div id="popup7" style="display:none;">
	<div class="modal-dialog" style="width:400px;">

	<!-- Modal content-->
	<div class="modal-content" style="background-color:transparent; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		<div class="modal-body" style="background-color:#ffffff; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		
	<table class="detailbox table98" cellpadding="20">
		<tr>
							<th colspan="2" width="16%" height="30" align="left"
								style="background-color: #002163; color: #ffffff; border-top-right-radius:0px !important;">Add a Blocked Transactional Velocity</th></tr>								
 <tr>
							<td width="7%">Number of transactions *</td>
							<td width="30%"><input type="text" class="form-control"></td>
							</tr>  
  <tr>
  <td>&nbsp;</td>
    <td><input type="submit" name="remittSubmit" value="Add" id="" class="btn btn-success btn-sm" /></td>
  </tr>
							
					</table></div></div></div>
</div>
<div id="popup8" style="display:none;">
	<div class="modal-dialog" style="width:400px;">

	<!-- Modal content-->
	<div class="modal-content" style="background-color:transparent; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		<div class="modal-body" style="background-color:#ffffff; border-radius:13px; -webkit-box-shadow:0px 0px 0px 0px; -moz-box-shadow:0px 0px 0px 0px;box-shadow:0px 0px 0px 0px; box-shadow:0px;">
		
	<table class="detailbox table98" cellpadding="20">
		<tr>
							<th colspan="2" width="16%" height="30" align="left"
								style="background-color: #002163; color: #ffffff; border-top-right-radius:0px !important;">Add a Blocked Transactional Amount</th></tr>								
 <tr>
							<td width="7%">Amount *</td>
							<td width="30%"><input type="text" class="form-control"></td>
							</tr>  
  <tr>
  <td>&nbsp;</td>
    <td><input type="submit" name="remittSubmit" value="Add" id="" class="btn btn-success btn-sm" /></td>
  </tr>
							
					</table></div></div></div>
</div>
<table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="txnf">
    <tr>
      <td align="left"><table width="100%" border="0">
      <tr>
      <td width="75%" align="left"><h2>Account Restrictions</h2></td>
      <td width="18%" align="left">
   <select name="merchants" id="merchant" class="form-control" onchange="handleChange();" autocomplete="off">
    <option value="ALL MERCHANTS">ALL MERCHANTS</option>
    <option value="test2@pg.com">Adidas Merchant</option>
    <option value="test1@gmail.com">Kotak</option>
    <option value="test@pg.com">Rahul</option>
</select>
  </td>
  <td width="4%" align="center">&nbsp;</td>
      </tr>      
      </table>
	  </td>
    </tr>
    <tr>
      <td align="left" valign="top"><div class="adduT">
          <div class="bkn">
          <h4>Blocked IP Addresses</h4>
            <div class="adduT">You have no blocked IP addresses</div>                                    
              <div style="height:30px;"></div>
              <div class="adduT" style="text-align:right; padding:14px 0 0 0;"><input type="submit" name="remittSubmit" value="Add" id="popupButton" class="btn btn-success btn-md" /></div>
            <div class="clear"></div>           
  </div>  
   <div class="bkn">
          <h4>Blocked Countries</h4>
            <div class="adduT">You have no blocked countries</div>                                    
              <div style="height:30px;"></div>
              <div class="adduT" style="text-align:right; padding:14px 0 0 0;"><input type="submit" name="remittSubmit" value="Add" id="popupButton2" class="btn btn-success btn-md" /></div>
            <div class="clear"></div>            
          </div>          
          </div>
                 
          
          <div class="clear"></div>
        </div>
        <div class="adduT">
          <div class="bkn">
          <h4>Blocked Card Ranges</h4>
            <div class="adduT">You have no blocked card ranges</div>                                    
              <div style="height:30px;"></div>
              <div class="adduT" style="text-align:right; padding:14px 0 0 0;"><input type="submit" name="remittSubmit" value="Add" id="popupButton3" class="btn btn-success btn-md" /></div>
            <div class="clear"></div>            
          </div>
          <div class="bkn">
          <h4>Blocked Issuing Countries</h4>
            <div class="adduT">You have no blocked issuing countries</div>                                    
              <div style="height:30px;"></div>
              <div class="adduT" style="text-align:right; padding:14px 0 0 0;"><input type="submit" name="remittSubmit" value="Add" id="popupButton4" class="btn btn-success btn-md" /></div>
            <div class="clear"></div>            
          </div>        
          
          <div class="clear"></div>
        </div>
        <div class="adduT">
          <div class="bkn">
          <h4>Blocked Email Addresses</h4>
            <div class="adduT">You have no blocked Email Addresses</div>                                    
              <div style="height:30px;"></div>
              <div class="adduT" style="text-align:right; padding:14px 0 0 0;"><input type="submit" name="remittSubmit" value="Add" id="popupButton5" class="btn btn-success btn-md" /></div>
            <div class="clear"></div>            
          </div>
          <div class="bkn">
          <h4>Blocked Domains</h4>
            <div class="adduT">You have no blocked Domains</div>                                    
              <div style="height:30px;"></div>
              <div class="adduT" style="text-align:right; padding:14px 0 0 0;"><input type="submit" name="remittSubmit" value="Add" id="popupButton6" class="btn btn-success btn-md" /></div>
            <div class="clear"></div>            
          </div>        
          
          <div class="clear"></div>
        </div>
        <div class="adduT">
          <div class="bkn">
          <h4>Blocked on Transactional Velocity</h4>
            <div class="adduT">You have blocked on Transactional Velocity</div>                                    
              <div style="height:30px;"></div>
              <div class="adduT" style="text-align:right; padding:14px 0 0 0;"><input type="submit" name="remittSubmit" value="Add" id="popupButton7" class="btn btn-success btn-md" /></div>
            <div class="clear"></div>            
          </div>
          <div class="bkn">
          <h4>Blocked Transactional Amount</h4>
            <div class="adduT">You have no blocked Transactional Amount</div>                                    
              <div style="height:30px;"></div>
              <div class="adduT" style="text-align:right; padding:14px 0 0 0;"><input type="submit" name="remittSubmit" value="Add" id="popupButton8" class="btn btn-success btn-md" /></div>
            <div class="clear"></div>            
          </div>        
          
          <div class="clear"></div>
        </div></td>
    </tr>
    </table>
<script>
$(document).ready(function () {

$('#fadeandscale').popup({
pagecontainer: '.container',
transition: 'all 0.3s'
});

});
</script>
<style>
#fadeandscale {
-webkit-transform: scale(0.8);
-moz-transform: scale(0.8);
-ms-transform: scale(0.8);
transform: scale(0.8);
}
.popup_visible #fadeandscale {
-webkit-transform: scale(1);
-moz-transform: scale(1);
-ms-transform: scale(1);
transform: scale(1);
}
</style>
</body>
</html>