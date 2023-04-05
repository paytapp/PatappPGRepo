<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags" %>
<link href="../css/default.css" type="text/css" rel="stylesheet" />
<link href="../css/style.css" type="text/css" rel="stylesheet" />
<%-- <script src="../js/jquery.min.js"></script>
 --%><script>

$(document).ready(function(){			
	$('#login-trigger').click(function(){
					$(this).next('#login-content').slideToggle();
					$(this).toggleClass('active');					
					
					if ($(this).hasClass('active')) {
						  $(this).find('span').html('&#x25B2;')
						  $("#login-content").show();
					}else {
						  $(this).find('span').html('&#x25BC;')
					}					
				})					
		  });	
/* $(document).click(function(event) {

	if(event.toElement == "login-trigger"){
		alert("trigger click");
	}
    if ( !$(event.target).hasClass('active')) {
         $("#login-content").hide();
       $('#login-trigger').find('span').html('&#x25BC;');
       $('#login-trigger').toggleClass('active');
    }
}); */
	</script>
	
<table class="headerstrps">
  <tr>
    <td width="2%" align="left">&nbsp;</td>
    <td width="14%" height="60" align="left" valign="top"><s:a action='home'><img src="../image/lpay.png" alt="" /></s:a></td>
    <td width="55%" align="left" valign="top"><table width="98%" align="right" border="0" cellspacing="0" cellpadding="0">
       
        <tr>
          <td width="36%" valign="top" align="left" class="whitefont"><strong>Last Login:</strong>
            <s:date name= "#session.LAST_LOGIN.timeStump"  format="dd/MMM/yyyy  HH:MM:SS" /></td>
          <td width="36%" valign="top" align="left" class="whitefont"><strong>Last Login IP:</strong> 
            <s:property value= "#session.LAST_LOGIN.ip"/></td>
          <td width="36%" valign="top" align="left" class="whitefont"><strong>Last Login Status:</strong> 
             
            <s:if test="#session.LAST_LOGIN.status">
                 	SUCCESS
			</s:if>
            <s:else>
                	FAIL
            </s:else>     
        </tr>
    </table></td>    
    <td width="" valign="top" align="right"><br /><div class="onclick-menu"><s:property value="%{#session.USER.businessName}" /> <span>&#x25BC;</span>
            <ul class="onclick-menu-content">
        <s:a action="logout"><li class="roundhover"><span class="lgouticon">&nbsp;</span>Log out</li></s:a>
    </ul>
</div></td>
<td width="1%" align="left">&nbsp;</td>    
  </tr>
</table>
