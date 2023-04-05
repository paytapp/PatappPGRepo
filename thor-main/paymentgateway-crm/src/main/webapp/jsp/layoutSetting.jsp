<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
<title>Layout Setting</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script src="../js/jscolor.min.js"></script>
<script src="../js/jquery.js"></script>
</head>
<body>

<s:form id="form1" name="form1" method="post" action="paymentPageLayout" enctype="multipart/form-data" >
<table width="100%" align="left" cellpadding="0" cellspacing="0" class="txnf">
		<tr>
			<td align="left"><h2>Preferences</h2></td>	
							<td width="8%" align="left" valign="middle"><div class="txtnew" style="padding:0 8px 0 0">
			<s:submit
												id="btnDefault" name="btnDefault" class="btn btn-success btn-sm"
												value="Set Default Theme" theme="simple" action="SetDefaultPaymentPage"/>
												 </div></td>		
													<td width="8%" align="left" valign="middle"><div class="txtnew" style="padding:0 8px 0 0">
													<s:url id="openPreview" namespace="/" action="jsp/previewPaymentPageSetting"></s:url>
													<s:a href="%{openPreview}" class="btn btn-sm btn-link" target="_blank">Show Preview</s:a></div></td>						
		</tr>
		<tr><td colspan="3" >
		<s:actionmessage class="success success-text" />
		</td>
		</tr>
		<tr>
  <td align="center" colspan="3" valign="top"><div class="adduT1">
      <div class="readp1">
        <table class="table98 product-specbigstripes" style="height:500px;">
          <tr>
            <td height="30" align="left" valign="middle" class="greytdbg borderleftradius"><strong>Page Title:</strong><br />
            <span class="categories">(The title of the payment page.)</span></td>
            <td align="left" class="greytdbg borderrightradius"><s:textfield  name="pageTittle" type="text" class="form-control" theme="simple" autocomplete="off" value="%{dynamicPaymentPage.pageTittle}"></s:textfield></td>
          </tr>
          <tr>
            <td width="60%" height="25" align="left"><strong>Your Logo:</strong><br />
             <span class="categories">(This logo will be displayed on Payment pages. Maximum size of the logo is 128 kb.)</span></td>
            <td width="40%" align="center">
               
                <span class="input-group-btn">
                <s:textfield name="logo" type="text" class="inputfieldsmall" theme="simple" readonly="true" autocomplete="off"></s:textfield>
                &nbsp;&nbsp;<span class="file-input btn btn-success btn-file btn-small"> <span class="glyphicon glyphicon-folder-open"></span>&nbsp;&nbsp;Choose file
                  <s:file name="userLogo" type="file"/>
                
                </span>
            
                </span>
                <s:if test="%{dynamicPaymentPage.userLogo=='TRUE'}" >
                   <a href="../image/userlogo/<s:property value="%{dynamicPaymentPage.payId}" />.png"  target="_blank">logo</a>
                  </s:if>
                
            </td>
            </tr>          
    
               <tr>
            <td height="30" align="left"><strong>Background Color:</strong><br />
            <span class="categories">(The background color for your payment page.)</span></td>
            <td align="left">
            <s:textfield name="backgroundColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.backgroundColor}"></s:textfield>
         
            </td>
          </tr>
          <tr>
            <td height="30" align="left" class="greytdbg"><strong>Text Style:</strong><br />
            <span class="categories">(The style of texts in your payment page.)</span></td>
            <td class="greytdbg" align="left">
            <s:select name="textStyle"
								id="textStyle" 
									list="@com.paymentgateway.commons.util.DynamicPaymentPageFont@values()" 
									listKey="name" listValue="name" class="form-control" autocomplete="off"/>
             </td>
          </tr>
          <tr>
            <td height="30" align="left"><strong>Text Color:</strong><br />
            <span class="categories">(The color of texts in your payment page.)</span></td>
            <td align="left"> <s:textfield name="textColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.textColor}"></s:textfield></td>
          </tr>
          <tr>
            <td height="30" align="left"  class="greytdbg"><strong>Hyperlink Color:</strong><br />
            <span class="categories">(The color of hyperlinks in your payment page.)</span></td>
            <td align="left"  class="greytdbg"> <s:textfield name="hyperlinkColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.hyperlinkColor}"></s:textfield></td>
          </tr>
          <tr>
            <td height="30" align="left"><strong>Box Background Color:</strong><br />
            <span class="categories">(The background color for boxes.)</span><br/><br/><br/></td>
            <td align="left"> <s:textfield name="boxBackgroundColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.boxBackgroundColor}"></s:textfield></td>
          </tr>
        </table>
        </div>
      
      <div class="readp1">
        <table class="table98 product-specbigstripes" style="height:500px;">
          <tr>
            <td width="60%" height="25" align="left" class="greytdbg borderleftradius"><strong>Top Bar Color:</strong><br />
            <span class="categories">(The background color for tab in your payment page.)</span></td>
            <td width="40%" align="left" class="greytdbg borderrightradius"> <s:textfield name="topBarColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.topBarColor}"></s:textfield></td>
          </tr>
          <tr>
            <td width="60%" height="25" align="left"><strong>Tab Background Color:</strong><br />
            <span class="categories">(The background color for tab in your payment page.)</span></td>
            <td width="40%" align="left"> <s:textfield name="tabBackgroundColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.tabBackgroundColor}"></s:textfield></td>
          </tr>
        
          <tr>
            <td height="30" align="left" class="greytdbg"><strong>Tab Text Color:</strong><br />
            <span class="categories">(The color of tab text in your payment page.)</span></td>
            <td align="left" class="greytdbg"> <s:textfield name="tabTextColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.tabTextColor}"></s:textfield></td>
          </tr>
          <tr>
            <td height="30" align="left"><strong>Active Tab Color:</strong><br />
            <span class="categories">(The background color for active tab in your payment page.)</span></td>
            <td align="left"> <s:textfield name="activeTabColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.activeTabColor}"></s:textfield></td>
          </tr>
          <tr>
            <td height="30" align="left" class="greytdbg"><strong>Active Tab Text Color:</strong><br />
            <span class="categories">(The color of active tab texts in your payment page.)</span></td>
            <td align="left" class="greytdbg"> <s:textfield name="activeTabTextColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.activeTabTextColor}"></s:textfield></td>
          </tr>
          <tr>
            <td height="30" align="left"><strong>Button Background Color:</strong><br />
            <span class="categories">(The background color for buttons.)</span></td>
            <td align="left"> <s:textfield name="buttonBackgoundColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.buttonBackgoundColor}"></s:textfield></td>
          </tr>
          <tr>
            <td height="25" align="left" class="greytdbg"><strong>Button Text Color:</strong><br />
            <span class="categories">(The color of button texts.)</span></td>
            <td align="left" class="greytdbg"> <s:textfield name="buttonTextColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.buttonTextColor}"></s:textfield></td>
          </tr>
          <tr>
            <td height="30" align="left"><strong>Border Color:</strong><br />
            <span class="categories">(The color of all borders in your payment page.)</span></td>
            <td align="left"> <s:textfield name="borderColor" type="text" class="jscolor form-control" theme="simple" value="%{dynamicPaymentPage.borderColor}"></s:textfield></td>
          </tr>
         
          </table>
      </div>
    </div>
    <div class="adduT1">
    <table width="100%" align="center">
     <tr>
												<td align="right" height="50"  valign="middle">
												<s:submit
												id="btnSave" name="btnSave" class="btn btn-success btn-md"
												value="Save" theme="simple" /> <input type="button" value="Cancel" class="btn btn-success btn-md" onClick="window.location.reload()">
												</td>
          </tr>
    </table></div>
    <br />
    <br />    
    </td>
</tr>
	</table>	
 <s:hidden name="token" value="%{#session.customToken}"></s:hidden>
</s:form>
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
</body>
</html>