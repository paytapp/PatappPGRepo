<%@ taglib uri="/struts-tags" prefix="s"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<title>Merchant Profile</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/profile-page.css" rel="stylesheet" />
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="../js/jquery.js"></script>
<script type="text/javascript" src="../js/jquery.easing.js"></script>
<script type="text/javascript" src="../js/jquery.dimensions.js"></script>
<script type="text/javascript" src="../js/jquery.accordion.js"></script>
<script type="text/javascript" src="../js/commanValidate.js"></script>
<script>
	$(document).ready(function() {
		$('.nav-tabs > li > a').click(function(event){
		event.preventDefault();//stop browser to take action for clicked anchor

		//get displaying tab content jQuery selector
		var active_tab_selector = $('.nav-tabs > li.active > a').attr('href');

		//find actived navigation and remove 'active' css
		var actived_nav = $('.nav-tabs > li.active');
		actived_nav.removeClass('active');

		//add 'active' css into clicked navigation
		$(this).parents('li').addClass('active');

		//hide displaying tab content
		$(active_tab_selector).removeClass('active');
		$(active_tab_selector).addClass('hide');

		//show target tab content
		var target_tab_selector = $(this).attr('href');
		$(target_tab_selector).removeClass('hide');
		$(target_tab_selector).addClass('active');
	     });
	  });
	</script>
<script>
	if (self == top) {
		var theBody = document.getElementsByTagName('body')[0];
		theBody.style.display = "block";
	} else {
		top.location = self.location;
	}
</script>
<script type="text/javascript">
	jQuery().ready(function(){
		// simple accordion
		jQuery('#list1a').accordion();
		jQuery('#list1b').accordion({
			autoheight: false
		});
		
		// second simple accordion with special markup
		jQuery('#navigation').accordion({
			active: false,
			header: '.head',
			navigation: true,
			event: 'click',
			fillSpace: false,
			animated: 'easeslide'
		});
	});
	</script>
    <script type="text/javascript">
function showDivs(prefix,chooser) {
        for(var i=0;i<chooser.options.length;i++) {
                var div = document.getElementById(prefix+chooser.options[i].value);
                div.style.display = 'none';
        }
 
		var selectedvalue = chooser.options[chooser.selectedIndex].value;
 
		if(selectedvalue == "PL")
		{
			displayDivs(prefix,"PL");
		}
		else if(selectedvalue == "PF")
		{
			displayDivs(prefix,"PF");
		}
		else if(selectedvalue == "PR")
		{
			displayDivs(prefix,"PR");
		}
		else if(selectedvalue == "CSA")
		{
			displayDivs(prefix,"CSA");
		}
		else if(selectedvalue == "LLL")
		{
			displayDivs(prefix,"LLL");
		}
		else if(selectedvalue == "RI")
		{
			displayDivs(prefix,"RI");
		}
		else if(selectedvalue == "AP")
		{
			displayDivs(prefix,"AP");
		}
		else if(selectedvalue == "T")
		{
			displayDivs(prefix,"T");
		}
 
}
 
function displayDivs(prefix,suffix) {
 
        var div = document.getElementById(prefix+suffix);
        div.style.display = 'block';
}
 
window.onload=function() {
  document.getElementById('select1').value='a';//set value to your default
}

</script>
<script>
var _validFileExtensions = [".jpg", ".pdf", ".png"];    
function Validate(oForm) {
    var arrInputs = oForm.getElementsByTagName("input");
    for (var i = 0; i < arrInputs.length; i++) {
        var oInput = arrInputs[i];
        if (oInput.type == "file") {
            var sFileName = oInput.value;
            if (sFileName.length > 0) {
                var blnValid = false;
                for (var j = 0; j < _validFileExtensions.length; j++) {
                    var sCurExtension = _validFileExtensions[j];
                    if (sFileName.substr(sFileName.length - sCurExtension.length, sCurExtension.length).toLowerCase() == sCurExtension.toLowerCase()) {
                        blnValid = true;
                        break;
                    }
                }
                
                if (!blnValid) {
                    alert("Sorry, " + sFileName + " is invalid, allowed extensions are: " + _validFileExtensions.join(", "));
                    return false;
                }
            }
        }
    }
  
    return true;
}



</script>
<script type="text/javascript">
function onlyalphabate(element, AlertMessage){
	var regexp = /^[a-zA-Z]+$/; 
	if(element.value.match(regexp)) { 
	alert("Letter Validation: Successful."); 
	return true; 
	}
	else{ 
	alert(AlertMessage); 
	element.focus(); 
	return false; 
	}
}
</script>
<%-- <script>

$( document ).ready(function() {
 
	//var token = document.getElementsByName("token")[0].value;
	$.ajax({
		url : 'checkFileExist',
		type : "POST",
			data : {
				 payId : document.getElementById("payId").value ,						
				 
				/*  token:token,
			    "struts.token.name": "token",  */
			},
				
				success:function(data){		       	    		       	    		
       	   		
       	   		var fileList = new Array;
       	   		filelist =data.fileName;	
       	        filelist= filelist.split(",");
       	     for (i = 0; i < filelist.length; i++) { 
       	    	document.getElementById(filelist[i]).style.visibility = "visible";
       	    	
       	     }
          		},
				
          		
				});		 


});
	</script> --%>
<style>
			/** Start: to style navigation tab **/
			.nav {
			  margin-bottom: 18px;
			  margin-left: 0;
			  list-style: none;
			}

			.nav > li > a {
			  display: block;
			}

			.nav-tabs{
			  *zoom: 1;
			}

			.nav-tabs:before,
			.nav-tabs:after {
			  display: table;
			  content: "";
			}

			.nav-tabs:after {
			  clear: both;
			}

			.nav-tabs > li {
			  float: left;
			}

			.nav-tabs > li > a {
			  padding-right: 12px;
			  padding-left: 12px;
			  margin-right: 2px;
			  line-height: 14px;
			}

			.nav-tabs {
			  border-bottom: 1px solid #ddd;
			}

			.nav-tabs > li {
			  margin-bottom: -1px;
			}

			.nav-tabs > li > a {
			  padding-top: 8px;
			  padding-bottom: 8px;
			  line-height: 18px;
			  border: 1px solid transparent;
			  -webkit-border-radius: 4px 4px 0 0;
				 -moz-border-radius: 4px 4px 0 0;
					  border-radius: 4px 4px 0 0;
			}

			.nav-tabs > li > a:hover {
			  border-color: #eeeeee #eeeeee #dddddd;
			}

			.nav-tabs > .active > a,
			.nav-tabs > .active > a:hover {
			  color: #555555;
			  cursor: default;
			  background-color: #ffffff;
			  border: 1px solid #ddd;
			  border-bottom-color: transparent;
			}

			li {
			  line-height: 18px;
			}

			.tab-content.active{
				display: block;
			}

			.tab-content.hide{
				display: none;
			}
            .nav-tabs>li>a:hover{border-top: 0px solid transparent;}

			/** End: to style navigation tab **/
		</style>
</head>

<body class="profilebg">

     <div class="blueback">
     <div class="bluebackL"><table class="table98 padding0">
          <tr class="tdhide">
            <td align="center" valign="top"><br /><img src="../image/profile-logo.png" /></td>
          </tr>
          <tr>
            <td align="left" valign="top">&nbsp;</td>
          </tr>
          <tr>
            <td align="left" valign="top"><div id="main">
  <div>
    <ul id="navigation">
      <li><s:a action='home' class="head1 myprofile">My Profile</s:a></li>
      <li><s:a action='passwordChangeSignUp' class="head1 changepassword" >Change Password</s:a></li>
        </ul>
  </div>
</div></td>
          </tr>
          <tr>
            <td align="left" valign="top">&nbsp;</td>
          </tr>
        </table></div>
     <div class="rightblu"><table class="table98 padding0">
          <tr>
            <td align="left" valign="top"><table class="table98 padding0">
              <tr>
                <td align="left" valign="top" class="welcometext">Welcome <s:property value="%{user.businessName}" /></td>
                <td align="right" valign="top">
      <s:a action="logout" class="btn btn-danger"><span class="glyphicon glyphicon-log-out"></span> Log out</s:a>
    
                               </td>
              </tr>
            </table></td>
          </tr>
          <tr>
            <td align="left" valign="top" class="borderbottomgrey">&nbsp;</td>
          </tr>
          <tr>
            <td align="left" valign="top">&nbsp;</td>
          </tr>
          <tr>
            <td align="left" valign="top">
      <table class="table98 padding0">
        
        <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
        <ul class="nav nav-tabs" style="border-bottom:none;">
        <li class="active"><a href="#MyPersonalDetails">My Personal Details</a></li>
        <li><a href="#MyContactDetails">My Contact Details</a></li>
        <li><a href="#MyBankDetails">My Bank Details</a></li>
        <li><a href="#MyBusinessDetails">My Business Details</a></li>
<!--         <li><a href="#DocumentsUploads">Upload Documents</a></li>
  <li><a href="#LogoUpload">Upload Logo</a></li>        -->      
    </ul>
    </td>
        </tr>
 
  <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center"><s:form action="newMerchantSaveAction" autocomplete="off" name="form1">
    <!-- <div id="my-tab-content1" class="tab-content"> -->
      <section id="MyPersonalDetails" class="tab-content active">
        <div>
            <br/><s:div  >
               
                  <table class="table98 padding0 profilepage">
                          <tr>
                          <td width="19%" height="25" align="left" class="bluetdbg"><strong>Business name:</strong></td>
                          <td width="81%" align="left"> <s:textfield name="businessName"  id="businessName"  cssClass="inputfield" type="text" value="%{user.businessName}" readonly="true" autocomplete="off"></s:textfield>
                          <s:hidden id="payId" name="payId" value="%{user.payId}"/>
                          </td>
                        </tr>
                          <tr>
                          <td height="30" align="left" class="bluetdbg"><strong>Email ID:</strong></td>
                          <td align="left"><s:textfield name="emailId" id="emailId" class="inputfield" type="text" value="%{user.emailId}" readonly="true" autocomplete="off"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" class="bluetdbg"><strong>First Name:</strong></td>
                          <td align="left">
                          <s:textfield name="firstName" id="firstName"  class="inputfield" type="text" value="%{user.firstName}" autocomplete="off" onkeypress="return lettersOnly(event,this);"></s:textfield>
                         </td>
                        </tr>
                          <tr>
                          <td height="30" align="left" class="bluetdbg"><strong>Last Name:</strong></td>
                          <td align="left"><s:textfield name="lastName" id="lastName"  class="inputfield" type="text" value="%{user.lastName}" autocomplete="off" onkeypress="return lettersOnly(event,this);"></s:textfield></td>
                        </tr>
						<%-- <tr class="addfildn">                        
						<td class="fl_wrap bluetdbg">
						<strong>Industry Category:</strong>
						</td>
						 <td align="left">
						<s:select class="fl_input" id="industryCategory" name="industryCategory" list="industryTypes" 																						value="%{user.industryCategory}"  autocomplete="off" onKeyPress="return ValidateAlpha(event);"></s:select>
						</td>
						</tr>                                                             
						<tr class="addfildn">
						<td class="fl_wrap bluetdbg">
						<strong>Industry Sub Category:</strong>
						</td>
						<td align="left"><s:textfield class="fl_input" id="industrySubCategory" name="industrySubCategory"
						type="text" value="%{user.industrySubCategory}" autocomplete="off" onKeyPress="return ValidateAlpha(event);"></s:textfield>
						</td>
						</tr> --%>
                          <tr>
                          <td height="30" align="left" class="bluetdbg"><strong>Company Name:</strong></td>
                          <td align="left">    <s:textfield name="companyName" class="inputfield" id="companyName" type="text" value="%{user.companyName}" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);"></s:textfield></td>
                        </tr>
                      <%--   <tr>
                          <td height="30" align="left"><strong>Business Type:</strong></td>
                          <td align="left"><s:textfield name="businessType" id="businessType" class="inputfield" type="text" value="%{user.businessType}" autocomplete="off"></s:textfield></td>
                        </tr> --%>
                        <tr>
                          <td height="30" align="left"></td>
                          <td align="left" style="float:left;"><s:submit  value="Save" class="btn btn-success"> </s:submit></td>
                        </tr>
                                               
                        </table>
                </s:div>
                </div>
                </section>
       <!--  </div> -->
       <section id="MyContactDetails" class="tab-content hide">
        <div>
            <br/><s:div  >
             <table class="table98 padding0 profilepage">
                          <tr>
                          <td width="18%" height="30" align="left" valign="middle" class="bluetdbg"><strong>Mobile:</strong></td>
                          <td width="82%" align="left" ><s:textfield name="mobile" id="mobile" class="inputfield" type="text" value="%{user.mobile}" autocomplete="off" onkeypress="javascript:return isNumber (event)"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" valign="middle" class="bluetdbg"><strong>Telephone No.:</strong></td>
                          <td align="left" ><s:textfield name="telephoneNo" id="telephoneNo" class="inputfield" type="text" value="%{user.telephoneNo}" autocomplete="off" onkeypress="javascript:return isNumber (event)"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" valign="middle" class="bluetdbg"><strong>Address:</strong></td>
                          <td align="left" ><s:textfield name="address" id="address" class="inputfield" type="text" value="%{user.address}" autocomplete="off"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" valign="middle" class="bluetdbg"><strong>City:</strong></td>
                          <td align="left" ><s:textfield name="city" id="city" class="inputfield" type="text" value="%{user.city}" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" valign="middle" class="bluetdbg"><strong>State:</strong></td>
                          <td align="left" ><s:textfield name="state" id="state" class="inputfield" type="text" value="%{user.state}" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" valign="middle" class="bluetdbg"><strong>Country:</strong></td>
                          <td align="left" ><s:textfield name="country" id="country" class="inputfield" type="text" value="%{user.country}" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);"></s:textfield></td>
                        </tr>
                        <tr>
                            <td height="30" align="left" valign="middle" class="bluetdbg"><strong>Postal Code:</strong></td>
                            <td align="left" ><s:textfield name="postalCode" id="postalCode" class="inputfield"  type="text" value="%{user.postalCode}" autocomplete="off" onkeypress="javascript:return isNumber (event)"></s:textfield></td>
  </tr>
                          <tr>
                          <td height="30" align="left"></td>
                          <td align="left" style="float:left;"><s:submit  value="Save" class="btn btn-success"> </s:submit></td>
                        </tr>
         
                        </table>
                </s:div>
        </div>
        </section>
        <section id="MyBankDetails" class="tab-content hide">
        <div>
            <br /><s:div  >
                <table class="table98 padding0 profilepage">
                          <tr>
                          <td width="18%" height="30" align="left" valign="middle" class="bluetdbg"><strong>Bank Name:</strong></td>
                          <td width="82%" align="left"><s:textfield name="bankName" class="inputfield" id="bankName" type="text" value="%{user.bankName}" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" valign="middle" class="bluetdbg"><strong>IFSC Code:&nbsp;</strong></td>
                          <td align="left"><s:textfield name="ifscCode" id="ifscCode" class="inputfield" type="text" value="%{user.ifscCode}" autocomplete="off" onkeypress="return IsAlphaNumeric(event);"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" valign="middle" class="bluetdbg"><strong>Acc Holder Name:&nbsp;&nbsp;</strong></td>
                          <td align="left"><s:textfield name="accHolderName" id="accHolderName" class="inputfield" type="text" value="%{user.accHolderName}" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" valign="middle" class="bluetdbg"><strong>Currency:&nbsp;&nbsp;</strong></td>
                          <td align="left"><s:textfield name="currency" id="currency" type="text" class="inputfield" value="%{user.currency}" autocomplete="off" onkeypress="return IsAlphaNumeric(event);"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" valign="middle" class="bluetdbg"><strong>Branch Name:&nbsp;&nbsp;</strong></td>
                          <td align="left"><s:textfield name="branchName" id="branchName" type="text" class="inputfield" value="%{user.branchName}" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);"></s:textfield></td>
                        </tr>
                          <tr>
                          <td height="30" align="left" valign="middle" class="bluetdbg"><strong>Pan Card:&nbsp;&nbsp;</strong></td>
                          <td align="left"><s:textfield name="panCard" id="panCard" type="text" class="inputfield" value="%{user.panCard}" autocomplete="off" onkeypress="return IsAlphaNumeric(event);"></s:textfield></td>
                        </tr>
                        <tr>
                            <td height="30" align="left" valign="middle" class="bluetdbg"><strong>Account No.:&nbsp;&nbsp;</strong></td>
                            <td align="left"><s:textfield name="accountNo" id="accountNo" class="inputfield" type="text" value="%{user.accountNo}" autocomplete="off" onkeypress="javascript:return isNumber (event)"></s:textfield></td>
  </tr>
  <tr>
                          <td height="30" align="left"></td>
                          <td align="left" style="float:left;"><s:submit  value="Save" class="btn btn-success"> </s:submit></td>
                        </tr>
                      
                        </table>
                </s:div>
        </div>   
        </section>
        <section id="MyBusinessDetails" class="tab-content hide">  
        <div>
            <br /><s:div>
             <table class="table98 padding0 profilepage">
                          <tr>
                            <td width="32%" height="50" align="left" valign="middle" class="bluetdbg"><strong>Organisation Type:&nbsp;&nbsp;</strong></td>
                            <td width="68%" align="left" class="text1">
                           <s:select headerKey="" headerValue="Select Title" cssClass="dropdownfield" list="#{'Proprietship':'Proprietship','Indivisual':'Indivisual','Partnership':'Partnership','Private Limited':'Private Limited','Public Limited':'Public Limited','LLP':'LLP','NGO':'NGO','Educational Institutes':'Educational Institutes','Trust':'Trust','Society':'Society'}"
																name="organisationType" id="organisationType" value="%{user.organisationType}" autocomplete="off"/></td>
                        </tr>
                          
                          <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>Website URL:&nbsp;&nbsp;</strong></td>
                            <td align="left" class="text1">
                           <s:textfield name="website" id="website" class="inputfield" type="text" value="%{user.website}" autocomplete="off"></s:textfield></td>
                        </tr>
                          <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>Multicurrency Payments Required?:&nbsp;&nbsp;</strong></td>
                            <td align="left" class="text1">
                            
                          <s:select headerKey=""
																headerValue="Select" cssClass="dropdownfield"
																list="#{'YES':'YES','NO':'NO'}"
																name="multiCurrency" id="multiCurrency" value="" autocomplete="off"/>
</td>
                        </tr>
                         
                        <tr>
                          <td height="50" align="left" valign="middle" class="bluetdbg"><strong> Business Model:&nbsp;&nbsp;</strong></td>
                          <td align="left" valign="middle">
                            <s:textfield name="businessModel" class="inputfield" id="businessModel" type="text" value="%{user.businessModel}" autocomplete="off"></s:textfield>
                            <span class="redsmalltext">Please give a brief explanation of your business model and future plans (Essential for startups)</span></td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>Operation Address:&nbsp;&nbsp;</strong></td>
                            <td align="left" class="text1">
                            <s:textfield name="operationAddress" class="inputfield" id="operationAddress" type="text" value="%{user.operationAddress}" autocomplete="off"></s:textfield></td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>Operation Address State:&nbsp;&nbsp;</strong></td>
                            <td align="left" class="text1">
                            
                          <s:textfield name="operationState" id="operationState" type="text" class="inputfield" value="%{user.operationState}" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);"></s:textfield></td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>Operation Address City:&nbsp;&nbsp;</strong><strong></strong></td>
                            <td align="left" class="text1">
                            
                           <s:textfield name="operationCity" id="operationCity" type="text" class="inputfield" value="%{user.operationCity}" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);"></s:textfield></td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>Operation Address Pincode:&nbsp;&nbsp;</strong><strong></strong></td>
                            <td align="left" class="text1">
                            <s:textfield name="operationPostalCode" id="operationPostalCode" class="inputfield" type="text" value="%{user.operationPostalCode}" autocomplete="off" onkeypress="javascript:return isNumber (event)"></s:textfield></td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>Date of Establishment</strong>:<strong>&nbsp;&nbsp;</strong><strong></strong></td>
                            <td align="left" class="text1">
                            
                           <s:textfield name="dateOfEstablishment" id="dateOfEstablishment" class="inputfield" type="text" value="%{user.dateOfEstablishment}" autocomplete="off"></s:textfield></td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="top" class="bluetdbg"><strong> CIN:&nbsp;&nbsp;</strong></td>
                            <td align="left" class="text1">
                              
                          <s:textfield name="cin" id="cin" type="text" class="inputfield" value="%{user.cin}" autocomplete="off"></s:textfield>
                            <span class="redsmalltext">Mandatory for Companies</span></td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="top" class="bluetdbg"><strong> PAN</strong>:<strong>&nbsp;&nbsp;</strong><strong></strong></td>
                            <td align="left" class="text1">
                              <s:textfield name="pan" id="pan" class="inputfield" type="text" value="%{user.pan}" autocomplete="off" onkeypress="return IsAlphaNumeric(event);"></s:textfield>
                            <span class="redsmalltext">Mandatory for Companies</span></td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="top" class="bluetdbg"><strong>Name on PAN Card:&nbsp;&nbsp;</strong></td>
                            <td align="left"><s:textfield name="panName" class="inputfield" id="panName" type="text" value="%{user.panName}" autocomplete="off" onkeypress="return lettersSpaceOnly(event, this);"></s:textfield>
                              <span class="redsmalltext">Mandatory for Companies</span>
                            
                            </td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>Expected number of transaction:</strong></td>
                            <td align="left" class="text1">
                          <s:textfield name="noOfTransactions" id="noOfTransactions" class="inputfield" type="text" value="%{user.noOfTransactions}" autocomplete="off" onkeypress="javascript:return isNumber (event)"></s:textfield></td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>Expected amount of transaction:&nbsp;&nbsp;</strong></td>
                            <td align="left" class="text1"><s:textfield name="amountOfTransactions" class="inputfield" id="amountOfTransactions" type="text" value="%{user.amountOfTransactions}" autocomplete="off" onkeypress="javascript:return isNumber (event)"></s:textfield></td>
  </tr>
  <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>Disable/Enable Transaction Email:</strong></td>
                            <td align="left" class="text1"> <s:checkbox name="transactionEmailerFlag" value="%{user.transactionEmailerFlag}" autocomplete="off"/>
                        </td>
  </tr>
                          <tr>
                            <td height="50" align="left" valign="middle" class="bluetdbg"><strong>TransactionEmail:&nbsp;&nbsp;</strong></td>
                            <td align="left" class="text1"><s:textfield name="transactionEmailId" class="inputfield" id="transactionEmailId" type="text" value="%{user.transactionEmailId}" autocomplete="off" ></s:textfield>
							</td>
							
  </tr>
                          <tr>
                            <td valign="bottom" colspan="2" align="center"><table class="table98 padding0">
  <tr>
    <td width="40%"><s:submit  value="Save" class="btn btn-success"> </s:submit></td>
    <td width="58%"></td>
  </tr> 
</table>

              </table>

                </s:div>
        </div>  
        </section>      
        <s:hidden name="token" value="%{#session.customToken}"></s:hidden>     
     </s:form>
    
    </td>
        </tr>       
        <tr>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
        </tr>
        
      </table>
  </td></tr>
          <tr>
            <td align="left" valign="top">&nbsp;</td>
          </tr>
          <tr>
            <td align="left" valign="top">&nbsp;</td>
          </tr>
        </table></div>
     <div class="clear"></div>
     </div>

<script type="text/javascript"> jQuery(document).ready(function ($) {  $('#tabs').tab(); });</script>
  <script src="../js/bootstrap.min.js"></script>
<script>$(document).on('change', '.btn-file :file', function() {
  var input = $(this),
      numFiles = input.get(0).files ? input.get(0).files.length : 1,
      label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
  input.trigger('fileselect', [numFiles, label]);
});

$(document).ready( function() {
    $('.btn-file :file').on('fileselect', function(event, numFiles, label) {
        
        var input = $(this).parents('.input-group').find(':text'),
            log = numFiles > 1 ? numFiles + ' files selected' : label;
        
        if( input.length ) {
            input.val(log);
        } else {
            if( log ) alert(log);
        }
        
    });
});</script>
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

</body>
</html>
