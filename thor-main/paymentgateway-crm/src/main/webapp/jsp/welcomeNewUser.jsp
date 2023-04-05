<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Dashboard</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link rel="stylesheet" type="text/css" href="../css/default.css" />
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td align="center" valign="top"><table width="100%" border="0" align="center" cellpadding="0" cellspacing="0" class="formbox">
  <tr>
    <td align="left" class="boxheading" height="40">Dashboard</td>
  </tr>
  <tr>
    <td align="center" height="20">&nbsp;</td>
  </tr>
  <tr>
    <td align="center"><table width="98%" border="0" align="center" cellpadding="0" cellspacing="0">
      <tr>
        <td width="20%" align="center" valign="top"><div class="welcomebox"><table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td height="30" class="boxheadinggrey">Total Success Transactions</td>
          </tr>
          <tr>
            <td align="left" valign="middle"><div class="welcomebiggertextwht" style="background-color:#0b4f88"><s:property value="%{statistics.totalSuccess}"/></div>
            </td>
          </tr>
        </table></div></td>
        <td width="20%" align="center" valign="top"><div class="welcomebox"><table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td height="30" class="boxheadinggrey">Total Failed Transactions</td>
          </tr>
          <tr>
            <td align="left" valign="middle"><div class="welcomebiggertextwht" style="background-color:#d9534f"><s:property value="%{statistics.totalFailed}"/></div>
            </td>
          </tr>
        </table></div></td>
        <td width="20%" align="center" valign="top"><div class="welcomebox"><table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td height="30" class="boxheadinggrey">Total Payments</td>
          </tr>
          <tr>
            <td align="left" valign="middle"><div class="welcomebiggertextwht" style="background-color:#f0ad4e"><s:property value="%{statistics.totalPayment}"/></div>
            </td>
          </tr>
        </table></div></td>
        <td width="20%" align="center" valign="top"><div class="welcomebox"><table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td height="30" class="boxheadinggrey">Total Refunds</td>
          </tr>
          <tr>
            <td align="left" valign="middle"><div class="welcomebiggertextwht" style="background-color:#5bc0de"><s:property value="%{statistics.totalRefund}"/></div>
            </td>
          </tr>
        </table></div></td>
        <td width="20%" align="center" valign="top"><div class="welcomebox"><table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td height="30" class="boxheadinggrey">Total Volume</td>
          </tr>
          <tr>
            <td align="left" valign="middle"><div class="welcomebiggertextwht" style="background-color:#5cb85c"><s:property value="%{statistics.totalVolume}"/></div>
            </td>
          </tr>
        </table></div></td>
      </tr>
      <tr>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
      </tr>
  <!--     <tr>
        <td align="center" colspan="5"><div class="welcomebox" style="width:98%;"><table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td height="30" class="boxheadinggreybig">&nbsp; Successful Transaction</td>
          </tr>
          <tr>
            <td align="left" valign="middle"><div class="welcomebiggertextwht"> <img src="../image/welcome.jpg"></div>
            </td>
          </tr> 
        </table></div></td>
      </tr>-->
      <tr>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
        <td>&nbsp;</td>
      </tr>
    </table></td>
  </tr>
  <tr>
    <td colspan="2" align="left" class="blkfont"><s:actionmessage/></td>
  </tr>
  <tr>
    <td align="center" valign="top"><table width="98%" border="0" cellpadding="5" cellspacing="0">
      <tr>
        <td width="25%" align="center"><s:a action='merchantProfile' class='btn-standart'>Profile</s:a></td>
       
        <td width="25%" align="center"><s:a action='passwordChange' class='btn-standart'>Change Password</s:a>  

             	</td>
       
      </tr>
    </table></td>
  </tr>
</table></td>
  </tr>
</table>
<div style="position:absolute; top:0px; margin:0 auto;"></div>
</body>
</html>