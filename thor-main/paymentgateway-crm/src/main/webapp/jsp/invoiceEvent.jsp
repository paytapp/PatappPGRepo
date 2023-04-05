<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>Promotional Payment</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link href="../fonts/css/font-awesome.min.css" rel="stylesheet" />
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script src="../js/jquery.dataTables.js"></script>
<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>

<script type="text/javascript">
$(document).ready(function(){
 
	$("#merchantPayId").on("change", function(e){
		var _merchant = $(this).val();
		if(_merchant != "" && _merchant != "-1"){
			$("#btnSave").attr("disabled", false);
		}else{
			$("#btnSave").attr("disabled", true);
		}
	});

  // Initialize select2
  $("#merchantPayId").select2();
});
</script>

<script type="text/javascript">
		
	function changeCurrencyMap() {
		var token = document.getElementsByName("token")[0].value;
		var emailId = document.getElementById("merchant").value;
		$.ajax({
			url : 'setMerchantCurrency',
			type : 'post',
			timeout: 0,
			data : {
				emailId : emailId,
				currency : document.getElementById("currencyCode").value,
				token : token
			},
			success : function(data) {
				var dataValue = data.currencyMap;
				var currenyMapDropDown = document.getElementById("currencyCode");
				var test = "";
				var parseResponse = '<select>';
				for (index in dataValue) {
					var key = dataValue[index];
					parseResponse += "<option value = "+index+">" + key + "</option> ";
				
				}
				parseResponse += '</select>';
				test += key;
				currenyMapDropDown.innerHTML = parseResponse;
			},
			error : function(data) {
				alert("Something went wrong, so please try again.");
			}
		});
	}
	class FieldValidator{
		constructor(x){
			this.x = x;
		}
		
		static valdInvoiceNo(errMsgFlag){
			var invoiceexp = /^[0-9a-zA-Z-/\_]+$/;;
		    var invoiceElement = document.getElementById("invoiceNo");
		    var invoiceValue = invoiceElement.value;
		    if (invoiceValue.trim() != "") {
		        if (!invoiceValue.match(invoiceexp)) {
		        	FieldValidator.addFieldError("invoiceNo", "Enter valid Invoice no.", errMsgFlag);
		            return false;
		        } else {
		            FieldValidator.removeFieldError('invoiceNo');
		            return true;
		        }
		    } else {
		    	FieldValidator.addFieldError("invoiceNo", "Please enter Invoice No.", errMsgFlag);
	            return false;
		    }	
		}
		static valdPhoneNo(errMsgFlag){
			var phoneElement = document.getElementById("phone");
			var value = phoneElement.value.trim();
			if ( value.length >0) {
				var phone = phoneElement.value;
				var phoneexp =  /^[0-9]{10,15}$/;
				if (!phone.match(phoneexp)) {
					FieldValidator.addFieldError("phone", "Enter valid phone no.", errMsgFlag);
					return false;
				}else{
					FieldValidator.removeFieldError('phone');
					return true;
				}
			}else{FieldValidator.removeFieldError('phone');
				return true;
			}
		}
		
		static valdProductName(errMsgFlag){
			var productNameElement = document.getElementById("productName"); 
			var value = productNameElement.value.trim();
			if (value.length>0) {
				var productName = productNameElement.value;
				var regex = /^[ A-Za-z0-9_@./#&+-]*$/;
				if (!productName.match(regex)) {
					FieldValidator.addFieldError("productName", "Enter valid product name", errMsgFlag);
					return false;
				}else{
					FieldValidator.removeFieldError('productName');
					return true;
				}
			}else{FieldValidator.removeFieldError('productName');
			return true;
			}	
		}
		
		static valdProductDesc(errMsgFlag){
			var productDescElement = document.getElementById("productDesc");
			var value = productDescElement.value.trim(); 
			if ( value.length>0) {
				var productDesc = productDescElement.value;
				var regex = /^[ A-Za-z]/;
				if (!productDesc.match(regex)) {
					FieldValidator.addFieldError("productDesc", "Enter valid product description", errMsgFlag);
					return false;
				}else{
					FieldValidator.removeFieldError('productDesc');
					return true;
				}
			}else{FieldValidator.removeFieldError('productDesc');
			return true;	
			}	
		}
		
		static valdCurrCode(errMsgFlag){
			var currencyCodeElement = document.getElementById("currencyCode");
			if (currencyCodeElement.value == "Select Currency") {
				FieldValidator.addFieldError("currencyCode", "Select Currency Type", errMsgFlag)
				return false;
			} else {
				FieldValidator.removeFieldError('currencyCode');
				return true;
			}
			
		}
		static valdRecptMobileNo(errMsgFlag){
			var recipientMobileElement = document.getElementById("recipientMobile");
			if (recipientMobileElement.value.trim().length >0) {
				var recipientMobile = recipientMobileElement.value;
				var phoneexp =  /^[0-9]{10,15}$/;
				if (!recipientMobile.match(phoneexp)) {
					FieldValidator.addFieldError("recipientMobile", "Enter valid mobile no", errMsgFlag);
					return false;
				}else{
					FieldValidator.removeFieldError('recipientMobile');
					return true;
				}
			} else {
				FieldValidator.addFieldError("recipientMobile", "Enter recipient mobile no", errMsgFlag);
				return false;
			}
		} 
		
		static valdRecptMsg(errMsgFlag){
			var messageBodyElement = document.getElementById("messageBody");
			if (messageBodyElement.value.trim().length >0) {
				if (!messageBodyElement.value.length > 2) {
					FieldValidator.addFieldError("messageBody", "Enter valid message", errMsgFlag);
					return false;
				}else{
					FieldValidator.removeFieldError('messageBody');
					return true;
				}
			} else {
				FieldValidator.addFieldError("messageBody", "Enter message", errMsgFlag);
				return false;
			} 			
		}
		
		static valdExpDayAndHour(errMsgFlag){
			var expDayElement = document.getElementById("expiresDay");
			var expHorElement = document.getElementById("expiresHour");
			var days = expDayElement.value.trim();
			var hors = expHorElement.value.trim();
			if (days.length >0 && parseInt(days)>=0) {
				if(parseInt(days) > 31){
					FieldValidator.addFieldError("expiresDay", "Enter valid no. of days (Max:31)", errMsgFlag);
					return false;
				}
				FieldValidator.removeFieldError('expiresDay');
				if(hors.length > 0 && parseInt(hors)>=0){
					if(parseInt(hors) > 24 || parseInt(hors)<0){
						FieldValidator.addFieldError("expiresHour", "Enter valid no. of hours (Max:24)", errMsgFlag);
						return false;
					}
					       else if(parseInt(days)==0 && parseInt(hors)==0){
							FieldValidator.addFieldError("expiresDay", "Enter valid no. of days (Max:31) or hours (Max:24)", errMsgFlag);
					        return false;
					}
					
					FieldValidator.removeFieldError('expiresHour');
					return true;
				}
				    else{FieldValidator.addFieldError("expiresHour", "Enter valid no. of hours", errMsgFlag);
				    return false;
				}
			}else{FieldValidator.addFieldError("expiresDay", "Enter valid no. of days", errMsgFlag);
				return false;
			}
		}
		
		static valdMerchant(errMsgFlag){
			var element =document.getElementById("merchant") 
			if ((element) != null) {
				if (element.value != "Select Merchant") {
					FieldValidator.removeFieldError('merchant');
					return true;
				} else {
					FieldValidator.addFieldError("merchant", "Select Merchant", errMsgFlag)
					return false;
				}
			}else{
				return true;
			}
		}
		
		static valdSrvcChrg(errMsgFlag){
			var element = document.getElementById('serviceCharge');
			var value = parseFloat(element.value.trim());
			
			if(element.value.indexOf(".") > -1){
				var index = element.value.indexOf(".");
				if((element.value.substr(index, element.value.length)).length>3){
					FieldValidator.addFieldError("serviceCharge", "Enter valid Service Charge", errMsgFlag)
					return false;
				}
			}
			if(parseFloat(value)>= parseFloat(0)){
				FieldValidator.removeFieldError('serviceCharge');
				return true;
			}else{
				FieldValidator.addFieldError("serviceCharge", "Enter valid Service Charge", errMsgFlag)
				return false;
			}
		}
		
		//valdiating the amount of the product
		static valdAmount(errMsgFlag){
			var element = document.getElementById('amount');
			var value = parseFloat(element.value.trim());
		
				if(element.value.indexOf(".") > -1){
					var index = element.value.indexOf(".");
					if((element.value.substr(index, element.value.length)).length>3){
						FieldValidator.addFieldError("amount", "Enter valid amount", errMsgFlag)
						return false;
					}
				}
			if(!value<parseFloat(value)){ // change here for custom amount
				FieldValidator.removeFieldError('amount');
				return true;
			}else{
				FieldValidator.addFieldError("amount", "Enter amount", errMsgFlag)
				return false;
			}
		}
		
		static valdEmail(errMsgFlag){
		
			var emailRegex = /[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/;
			var element = document.getElementById("emailId");
			var value = element.value.trim();
			if(value.length>0){
				if(!value.match(emailRegex)){FieldValidator.addFieldError('emailId', "Enter valid email address", errMsgFlag);
				return false;
				}else{FieldValidator.removeFieldError('emailId');
				return true;
				}
			}else{FieldValidator.removeFieldError('emailId');
			return true;
			}
		}

		static valdName(errMsgFlag){
			var nameRegex =  /^[a-zA-Z ]+$/;
			var element = document.getElementById("name");
			var value = element.value.trim();
			if(value.length	>0){
				if(!(value).match(nameRegex)){
					FieldValidator.addFieldError('name', "Enter valid name", errMsgFlag);
					return false;
				}else{FieldValidator.removeFieldError('name');
				return true;
				}
			}else{FieldValidator.removeFieldError('name');
			return true;
			}	
		}
		static valdCountry(errMsgFlag){
			var nameRegex =  /^[a-zA-Z ]+$/;
			var element = document.getElementById("country");
			var value = element.value.trim();
			if(value.length	>0){
				if(!(value).match(nameRegex)){
					FieldValidator.addFieldError('country', "Enter valid country", errMsgFlag);
					return false;
				}else{FieldValidator.removeFieldError('country');
				return true;
				}
			}else{FieldValidator.removeFieldError('country');
			return true;
			}	
		}
		static valdCity(errMsgFlag){
			var nameRegex =  /^[a-zA-Z ]+$/;
			var element = document.getElementById("city");
			var value = element.value.trim();
			if(value.length	>0){
				if(!(value).match(nameRegex)){
					FieldValidator.addFieldError('city', "Enter valid city", errMsgFlag);
					return false;
				}else{FieldValidator.removeFieldError('city');
				return true;
				}
			}else{FieldValidator.removeFieldError('city');
			return true;
			}	
		}
		static valdState(errMsgFlag){
			var nameRegex =  /^[a-zA-Z ]+$/;
			var element = document.getElementById("state");
			var value = element.value.trim();
			if(value.length	>0){
				if(!(value).match(nameRegex)){
					FieldValidator.addFieldError('state', "Enter valid state", errMsgFlag);
					return false;
				}else{FieldValidator.removeFieldError('state');
				return true;
				}
			}else{FieldValidator.removeFieldError('state');
			return true;
			}	
		}
	
		static valdZip(errMsgFlag){
			var zipRegex = "^[a-zA-Z0-9]{4,10}$";
			var element = document.getElementById("zip");
			var value = element.value.trim();
			if(value.length	>0){
				if(!(value).match(zipRegex)){
					FieldValidator.addFieldError('zip','Enter valid zip code', errMsgFlag);
					return false;
				}else{FieldValidator.removeFieldError('zip');
				return true;
				}
			}else{FieldValidator.removeFieldError('zip');
			return true;
			}
		}
		
		static valdReturnUrl(errMsgFlag){
			var urlRegex = "^(https?|http?|www.?)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
			var element = document.getElementById("returnUrl");
			var value = element.value.trim();
			if(value.length	>0){
				if(!(value).match(urlRegex)){
					FieldValidator.addFieldError('returnUrl', "Enter valid return url", errMsgFlag);
					return false;
				}else{FieldValidator.removeFieldError('returnUrl');
				return true;
				}
			}else{FieldValidator.removeFieldError('returnUrl');
			return true;
			}	
		}
		
		static valdAddress(errMsgFlag){
			var addRegex = /^[a-zA-Z0-9 -/() .,@;:# \r\n]+$/; //-/() .,@;:# \r\n
			var element = document.getElementById("address");
			var value = element.value.trim();
			if(value.length	>0){
				if(!(value).match(addRegex)){
					FieldValidator.addFieldError('address', "Enter valid address", errMsgFlag);
					return false;
				}else{FieldValidator.removeFieldError('address');
				return true;
				}
			}else{FieldValidator.removeFieldError('address');
			return true;
			}
			
		}

		
		
		static valdQty(errMsgFlag){
			var qtyRgx = "^([1-9][0-9]+|[1-9])$";
			var element = document.getElementById("quantity");
			var value = element.value.trim();
			if(value==0){
				FieldValidator.addFieldError('quantity', "Enter valid quantity", errMsgFlag);
				return false;
			}
			if(value.length	>0){
				if(!(value).match(qtyRgx)){
					FieldValidator.addFieldError('quantity', "Enter valid quantity", errMsgFlag);
					return false;
				}else{FieldValidator.removeFieldError('quantity');
				return true;
				}
			}else{FieldValidator.addFieldError('quantity',"Enter valid quantity", errMsgFlag);
			return false;
			}	
		}
		
		
		static valdAllFields(){
			
			var flag = FieldValidator.valdMerchant(true);
			flag = flag && FieldValidator.valdInvoiceNo(true);
			flag = flag && FieldValidator.valdName(true);
			flag = flag && FieldValidator.valdCity(true);
			flag = flag && FieldValidator.valdAddress(true);
			flag = flag && FieldValidator.valdCountry(true);
			flag = flag && FieldValidator.valdState(true);
			flag = flag && FieldValidator.valdZip(true);
			flag = flag && FieldValidator.valdPhoneNo(true);
			flag = flag && FieldValidator.valdEmail(true);
			flag = flag && FieldValidator.valdReturnUrl(true);
			flag = flag && FieldValidator.valdProductName(true);
			flag = flag && FieldValidator.valdProductDesc(true);
			flag = flag && FieldValidator.valdExpDayAndHour(true) ;
			flag = flag && FieldValidator.valdCurrCode(true);
			flag = flag && FieldValidator.valdQty(true);
			flag = flag && FieldValidator.valdAmount(true);
			//flag = flag && FieldValidator.valdSrvcChrg(true);
			//flag = flag && FieldValidator.valdRecptMobileNo(true);
			//flag = flag && FieldValidator.valdRecptMsg(true);
			//submitting form
			if(flag){
			document.forms["frmInvoice"].submit();}
		}
		
		//to show error in the fields
		static addFieldError(fieldId, errMsg, errMsgFlag){
			var errSpanId = fieldId+"Err";
			var elmnt = document.getElementById(fieldId);
			elmnt.className = "textFL_merch_invalid";
			elmnt.focus();
			if(errMsgFlag){
				document.getElementById(errSpanId).innerHTML = errMsg;
			}
		}
		
		// to remove the error 
		static removeFieldError(fieldId){
			var errSpanId = fieldId+"Err";
			document.getElementById(errSpanId).innerHTML = "";
			document.getElementById(fieldId).className = "textFL_merch";
		}
	}

	function sum() {
	    var txtFirstNumberValue = document.getElementById('amount').value;
	    if(txtFirstNumberValue == "") {
	    	txtFirstNumberValue = "0.00";
	    }
	    var txtSecondNumberValue = document.getElementById('serviceCharge').value;
	    if(txtSecondNumberValue == "" || txtSecondNumberValue == ".") {
	    	txtSecondNumberValue = "0.00";
	    }
	    var txtQuantity = document.getElementById('quantity').value;
	    var result =  parseInt(txtQuantity)* parseFloat(txtFirstNumberValue);
	    if (!isNaN(result)) {
	       document.getElementById('totalAmount').value =(result + parseFloat(txtSecondNumberValue)).toFixed(2);
	    }
	}

	$(document).ready(function() {
		if(window.location.pathname.substr(9,window.location.pathname.length) == "saveInvoiceEvent"){
			document.getElementById('promoLink').style="display:block";
			document.getElementById('copyBtn').style="display:block";
			document.getElementById('btnSave').style="display:none";
		}
		copyBtn.disabled = !document.queryCommandSupported('copy');
		document.getElementById("copyBtn").addEventListener("click", function(event){
			var copiedLink = document.getElementById('promoLink');
			copiedLink.select();
			document.execCommand('copy');
		});

		
		$('#btnSave').click(function(event) {
			event.preventDefault();
			
			FieldValidator.valdAllFields();
		});

		$('#merchant').change(function() {
			changeCurrencyMap();
			$('#spanMerchant').hide();
			$('#currencyCodeloc').hide();
		});

		$('#serviceCharge').on('keyup', function() {
			if (this.value[0] === '.') {
				this.value = '0' + this.value;
			}
		});
		
		$(document).ready(function() {
		    $('#example').DataTable( {
		        dom: 'B',
		        buttons: [
		            'csv'
		        ]
		    });
		});
		
		
			
	});
</script>
<style>	
	.textFL_merch {
    border: 1px solid #c0c0c0;
    background: #fff;
    padding: 8px;
    width: 100%;
    color: #000;
    border-radius: 3px;
}

.textFL_merch:hover {
    border: 1px solid #d5d0a3;
    padding: 8px;
    width: 100%;
    border-radius: 3px;
}
	.textFL_merch_invalid {
    border: 1px solid #c0c0c0;
    background: #fff;
    padding: 8px;
    width: 100%;
    border-color: #FF0000;
    border-radius: 1px;
}
.btn-fl{float: right;clear: right;margin-right: 80px;}
.btn-small{padding: 6px!important;}
.bws-tp{margin-bottom: 0px!important; margin-top: 8px;}
.inputfieldsmall {height: 28px!important;padding:4px 15px!important;} 
	
	#message {
    position:relative;
    width:auto;
    height: auto;
   /* overflow:scroll;*/
  
}
.btnPdng {
    padding: 7px 36px !important;
}

.invo6 {
    margin-bottom: 0 !important;
    min-height: 79px;
}
.buttons-csv{
	      background: #002163;
    color: #fff;
    padding: 2px 7px;
    border-radius: 3px;
    display: block;
    width: 40px;
    cursor: pointer;
    font-size: 11px;
    margin-top: 30px;
}
.buttons-csv:hover{
	    background: #999;
    color: #000;
    text-decoration: none;
}
.btn:focus{
		outline: 0 !important;
}

</style>
</head>
<body>
	<s:form name="f1" action="saveInvoiceEvent" id='frmInvoice' method="POST"
				enctype="multipart/form-data"
		autocomplete="off">
		
		<script type="text/javascript">
    $("body").on("click", "#btnSave", function () {
        var allowedFiles = [".csv"];
        var fileUpload = $("#fileUpload");
        var lblError = $("#lblError");
        var regex = new RegExp("([a-zA-Z0-9\s_\\.\-:])+(" + allowedFiles.join('|') + ")$");
        if (!regex.test(fileUpload.val().toLowerCase())) {
            lblError.html("Please upload files having extensions: <b>" + allowedFiles.join(', ') + "</b> only.");
            return false;
        }
        lblError.html('');
        return true;
    });
</script>
<script type="text/javascript">
function isNumberKey(evt){
    var charCode = (evt.which) ? evt.which : event.keyCode
    if (charCode > 31 && (charCode < 48 || charCode > 57))
        return false;
    return true;
}
</script>
<script type="text/javascript">
	var specialKeys = new Array();
	specialKeys.push(8); //Backspace
	specialKeys.push(9); //Tab
	specialKeys.push(46); //Delete
	specialKeys.push(36); //Home
	specialKeys.push(35); //End
	specialKeys.push(37); //Left
	specialKeys.push(39); //Right
	function IsAlphaNumeric(e) {
		var keyCode = e.keyCode == 0 ? e.charCode : e.keyCode;
		var ret = ((keyCode >= 48 && keyCode <= 57) || (keyCode >= 65 && keyCode <= 90) || (keyCode >= 97 && keyCode <= 122) || (specialKeys.indexOf(e.keyCode) != -1 && e.charCode != e.keyCode));
		return ret;
	}
</script>
<script type="text/javascript">
	function lettersOnly(e, t) {
            try {
                if (window.event) {
                    var charCode = window.event.keyCode;
                }
                else if (e) {
                    var charCode = e.which;
                }
                else { return true; }
                if ((charCode > 64 && charCode < 91) || (charCode > 96 && charCode < 123) || charCode == 8)
                    return true;
                else
                    return false;
            }
            catch (err) {
                alert(err.Description);
            }
        }
</script>
<td width="29%" align="center" valign="middle">
                    <div class="input-group bws-tp">
					<span class=""> <input type="text" class="inputfieldsmall" id="fileUpload" readonly> 
                    <span class="file-input btn-success btn-file btn-small btn-fl"> 
                    <span class="glyphicon glyphicon-folder-open"></span>
					&nbsp;&nbsp;Browse <s:file name="fileName" />
					</span>
					</span></br>
					<span id="lblError" style="color: red;font-size: 11px;"></span> <br />
					</div></td>
					<tr>
			<td width="46%" height="50" align="left" valign="bottom"><table
					id="example" style="display: none;">
					<thead>

						<tr>
							<th>Email Id</th>
							<th>Mobile No</th>
						
						</tr>
					</thead>
				</table> Sample CSV File Format</td>
			
		</tr>
		<table width="100%" border="0" align="center" cellpadding="0"
			cellspacing="0" class="txnf">
			<tr>
			<div  id="message" colspan="3" align="left"><s:actionmessage /></div>

			</tr>
			<tr>
				<td align="left">
					<div style="display: none" id="response"></div>
					<table width="100%" border="0">
						<tr>
							<td width="82%" align="left"><h2>Promotional Payment</h2></td>
							<td width="25%" align="left">
								<div class="txtnew">

									<s:if
										test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
										<s:select name="merchantPayId" class="form-control" id="merchantPayId"
											headerKey="-1" headerValue="Select Merchant" list="merchantList"
											listKey="payId" listValue="businessName" autocomplete="off" />
									</s:if>
								</div> <span id="merchantErr" class="invocspan"></span>
							</td>
							<td width="2%" align="center">&nbsp;</td>
						</tr>
					</table>
				</td>
			</tr>

			<tr>
				<td height="30" colspan="5" align="left" valign="middle"><h3>
						Detail Information</h3></td>
			</tr>
			<tr>
				<td colspan="5" align="left" valign="top"><div class="MerchBx"
						style="background: #f2f2f2; border-radius: 5px; padding: 10px">
						<div class="invoCont2">
							<div class="invo6">
								Invoice no* <br />
								<div class="txtnew">
									<s:textfield type="text" class="textFL_merch" id="invoiceNo"
										name="invoiceNo" autocomplete="off"
										onkeyup="FieldValidator.valdInvoiceNo(false)" onkeypress="return IsAlphaNumeric(event);"/>
									<span id="invoiceNoErr" class="invocspan"></span>
								</div>
							</div>
							<div class="invo6">
								Name<br />
								<div class="txtnew">
									<s:textfield type="text" class="textFL_merch" id="name" onkeyup="FieldValidator.valdName(false)"
										name="name" autocomplete="off" onkeypress="return lettersOnly(event,this);"/>
								</div>
								<span id="nameErr" class="invocspan"></span>
							</div>
							<div class="invo6">
								City<br />
								<div class="txtnew">
									<s:textfield type="text" class="textFL_merch" id="city" onkeyup="FieldValidator.valdCity(false)"
										name="city" autocomplete="off" onkeypress="return lettersOnly(event,this);"/>
								</div>
								<span id="cityErr" class="invocspan"></span>
							</div>
							<div class="clear"></div>
							<div class="invo6">
								Country<br />
								<div class="txtnew">
									<s:textfield type="text" id="country" name="country" onkeyup="FieldValidator.valdCountry(false)"
										class="textFL_merch" autocomplete="off" onkeypress="return lettersOnly(event,this);"/>
								</div>
								<span id="countryErr" class="invocspan"></span>
							</div>

							<div class="invo6">
								State<br />
								<div class="txtnew">
									<s:textfield type="text" class="textFL_merch" id="state" onkeyup="FieldValidator.valdState(false)"
										name="state" autocomplete="off" onkeypress="return lettersOnly(event,this);"/>
								</div>
								<span id="stateErr" class="invocspan"></span>
							</div>
							<div class="invo6">
								Zip<br />
								<div class="txtnew">
									<s:textfield type="text" class="textFL_merch" id="zip"
										name="zip" autocomplete="off" onkeyup="FieldValidator.valdZip(false)" onkeypress="return isNumberKey(event)" />
								</div>
								<span id="zipErr" class="invocspan"></span>
							</div>
							<div class="invo6">
								Phone<br />
								<div class="txtnew">
									<s:textfield type="text" id="phone" name="phone" maxlength="15"
										class="textFL_merch" autocomplete="off" onkeyup="FieldValidator.valdPhoneNo(false)" onkeypress="return isNumberKey(event)"/>
								</div>
								<span id="phoneErr" class="invocspan"></span>
							</div>
							<div class="invo6">
								Email<br />
								<div class="txtnew">
									<s:textfield type="text" id="emailId" name="email"
										value="%{invoice.email}" class="textFL_merch" onkeyup="FieldValidator.valdEmail(false)"
										autocomplete="off" />
								</div>
								<span id="emailIdErr" class="invocspan"></span>
							</div>
							<div class="invo6">
								Return URL<br />
								<div class="txtnew">
									<s:textfield type="text" class="textFL_merch" id="returnUrl"
										onkeyup="FieldValidator.valdReturnUrl(false)"  name="returnUrl" autocomplete="off" />
								</div>
								<span id="returnUrlErr" class="invocspan"></span>
							</div>
							<div class="clear"></div>
						</div>
						<div class="invoCont3">
							<div class="invo7">
								Address<br />
								<div class="txtnew">
									<s:textarea type="text" class="textFL_merch" id="address" onkeyup="FieldValidator.valdAddress(false)"
										name="address" autocomplete="off" cols="30" rows="11" />
								</div>
								<span id="addressErr" class="invocspan"></span>
							</div>
							<div class="clear"></div>
						</div>
						<div class="clear"></div>
					</div></td>
			</tr>
			<tr>
				<td colspan="5" align="left" valign="top">&nbsp;</td>
			</tr>
			<tr>
				<td colspan="5" align="left" valign="top"><h3>Product Information</h3></td>
			</tr>
			<tr>
				<td colspan="5" align="left" valign="top"><div class="MerchBx"
						style="background: #f2f2f2; border-radius: 5px; padding: 10px">
						<div class="invoCont">
							<div class="productIn">
								Name<br />
								<div class="txtnew">
									<s:textfield type="text" class="textFL_merch" id="productName"
										name="productName" autocomplete="off" onkeyup="FieldValidator.valdProductName(false)" onkeypress="return lettersOnly(event,this);"/>
									<span id="productNameErr" class="invocspan"></span>
								</div>
							</div>
							<div class="productIn">
								Description<br />
								<div class="txtnew">
									<s:textfield type="text" class="textFL_merch" id="productDesc" onkeyup="FieldValidator.valdProductDesc(false)"
										name="productDesc" autocomplete="off" />
									<span id="productDescErr" class="invocspan"></span>
								</div>
							</div>
							<div class="productIn">
								Expiry *<br />
								<div class="txtnew">
									<s:textfield type="text" class="textFL_merch" id="expiresDay"
										name="expiresDay" autocomplete="off" value="0" maxlenth="2" onkeyup="FieldValidator.valdExpDayAndHour(false)" onkeypress="return isNumberKey(event)" />
								</div>
								<span id="expiresDayErr" class="invocspan"></span>
							</div>
							<div class="productIn">
								Expire in hours* <br />
								<div class="txtnew">
									<s:textfield type="text" class="textFL_merch" id="expiresHour"
										name="expiresHour" autocomplete="off" value="0" maxlength="2" onkeyup="FieldValidator.valdExpDayAndHour(false)" onkeypress="return isNumberKey(event)" />
								</div>
								<span id="expiresHourErr" class="invocspan"></span>
							</div>
							<div class="productIn">
								All prices are in*<br />
								<div class="txtnew">
									<s:select name="currencyCode" id="currencyCode"
										headerValue="Select Currency" headerKey="Select Currency"
										list="currencyMap" listKey="key" listValue="value"
										class="form-control" onchange="FieldValidator.valdCurrCode(false)"
										autocomplete="off" onkeypress="return isNumberKey(event)"  style="height: 33px!important;" />
								</div>
								<span id="currencyCodeErr" class="invocspan"></span>
							</div>
							<div class="invoC1">
								<table width="97%" border="0" cellpadding="0" cellspacing="0"
									class="greyroundtble">
									<tr>
										<td width="6%" align="left" valign="middle"><h6>Quantity</h6></td>
										<td width="2%" align="center" valign="middle">&nbsp;</td>
										<td width="15%" align="left" valign="top"><h6>
												Amount*</h6></td>
										<td width="2%" align="left" valign="middle">&nbsp;</td>
									</tr>
									<tr>
										<td align="left" valign="top"><div class="txtnew">
												<s:textfield type="text" class="textFL_merch" value="1"
													id="quantity" name="quantity" onkeyup="sum();FieldValidator.valdQty(false);"
													autocomplete="off" onkeypress="return isNumberKey(event)" />
											</div>
												<span id="quantityErr" class="invocspan"></span></td>
										<td align="center" valign="middle"><strong>x</strong></td>
										<td height="37" align="left" valign="top"><div
												class="txtnew">
												<s:textfield type="text" class="textFL_merch"
													onkeyup="sum();FieldValidator.valdAmount(false);" id="amount" name="amount"
													value="%{invoice.amount}"
													autocomplete="off" onkeypress="return isNumberKey(event)" />
											</div> <span id="amountErr" class="invocspan"></span></td>
										<td align="left" valign="middle">&nbsp;</td>
									</tr>
									<tr>
										<td align="left" valign="top">&nbsp;</td>
										<td align="left" valign="middle">&nbsp;</td>
										<td align="left" valign="top" class="labelfont">Service
											Charge</td>
										<td align="left" valign="middle">&nbsp;</td>
									</tr>
									<tr>
										<td align="right" valign="middle" class="labelfont">&nbsp;</td>
										<td align="right" valign="middle" class="labelfont">&nbsp;</td>
										<td height="37" align="left" valign="top"><div
												class="txtnew">
												<s:textfield type="text" class="textFL_merch" value="%{invoice.serviceCharge}"
													id="serviceCharge" name="serviceCharge" placeholder="0.00"
													onkeyup="sum();FieldValidator.valdSrvcChrg(false);" onkeypress="return isNumberKey(event)"
													autocomplete="off" />
											</div>
											<span id="serviceChargeErr" class="invocspan"></span></td>
										<td align="left" valign="middle">&nbsp;</td>
									</tr>
									<tr>
										<td align="right" valign="middle" class="labelfont">&nbsp;</td>
										<td align="right" valign="middle" class="labelfont">&nbsp;</td>
										<td align="left" valign="top" class="labelfont">Total
											Amount</td>
										<td align="left" valign="middle">&nbsp;</td>
									</tr>
									<tr>
										<td align="right" valign="middle" class="labelfont">&nbsp;</td>
										<td align="right" valign="middle" class="labelfont">&nbsp;</td>
										<td align="left" valign="middle"><div class="txtnew">
												<s:textfield type="text" class="textFL_merch"
													readonly="true" placeholder="0.00" id="totalAmount"
													name="totalAmount" autocomplete="off" />
											</div></td>
										<td align="left" valign="middle">&nbsp;</td>
									</tr>
								</table>
							</div>
						</div>
						<div class="clear"></div>
					</div></td>
			</tr>
			<%-- <tr>
				<td><h3>SMS Information</h3></td>
			</tr>
			<tr>
				<td><div class="MerchBx"
						style="background: #f2f2f2; border-radius: 5px; padding: 10px">

						<div class="invoC">
							Recipient Mobile*<br />
							<s:textfield type="text" class="textFL_merch2"
								id="recipientMobile" name="recipientMobile" onkeyup="FieldValidator.valdRecptMobileNo(false)" autocomplete="off" />
							<span id="recipientMobileErr" class="invocspan"></span>
						</div>
					
						<div class="invo8">
							Message Body* <br />
							<s:textarea type="text" class="textFL_merch2" id="messageBody"
								name="messageBody" onkeyup="FieldValidator.valdRecptMsg(false)" autocomplete="off"
								placeholder="maximum 160 character" maxlength="160" />
						</div>
						<span id="messageBodyErr" class="invocspan"></span>
						<div class="clear"></div>
					</div></td>
			</tr> --%>
			<tr>
				<td align="center" valign="middle"><table width="97%"
						border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td align="center" valign="middle" height="40" class="bluelinkbig">
							<input id="promoLink" onkeydown="document.getElementById('copyBtn').focus();" type="text" style="display:none" class="textFL_merch" value= <s:property value="url" />>
							
							
							</td>
							
						</tr>
						<tr>
						<td align="center" valign="middle" height="40">
						<input type="button" style="margin-top:5px;display:none;" id="copyBtn" class="btn btn-success btn-medium" value="Copy Payment Link "/>
						</td>
						</tr>
					</table></td>
			</tr>
			<tr>
				<td align="center" valign="top"><table width="100%" border="0"
						cellpadding="0" cellspacing="0">
						<tr>
							<td width="15%" align="left" valign="middle"></td>
							<td width="5%" align="right" valign="middle"><s:submit
									id="btnSave" disabled="true" name="fileName" class="btn btn-success btn-md btnPdng"
									value="Save">
								</s:submit></td>
							<td width="3%" align="left" valign="middle"></td>
							<td width="15%" align="left" valign="middle"></td>
						</tr>
					</table></td>
			</tr>
		</table>
		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
	</s:form>

<!-- 
	<table width="100%" border="0" align="center" cellpadding="0"
		cellspacing="0" class="txnf product-spec">
		<tr>
			<th colspan="3" align="left">UPLOAD FILE</th>
		</tr>
		
	</table> -->
	<script>
		$(document).on(
				'change',
				'.btn-file :file',
				function() {
					var input = $(this), numFiles = input.get(0).files ? input
							.get(0).files.length : 1, label = input.val()
							.replace(/\\/g, '/').replace(/.*\//, '');
					input.trigger('fileselect', [ numFiles, label ]);
				});

		$(document)
				.ready(
						function() {
							$('.btn-file :file')
									.on(
											'fileselect',
											function(event, numFiles, label) {

												var input = $(this).parents(
														'.input-group').find(
														':text'), log = numFiles > 1 ? numFiles
														+ ' files selected'
														: label;

												if (input.length) {
													input.val(log);
												} else {
													if (log)
														alert(log);
												}

											});
						});
	</script>




</body>
</html>