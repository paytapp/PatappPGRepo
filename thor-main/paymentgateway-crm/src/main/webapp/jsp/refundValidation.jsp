<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib prefix="s" uri="/struts-tags"%>
<html>
<head>

<style>

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

		
.uploadButton{
    border: none;
	background-color: #002163;
    border-radius: 5px;
    width: 25%;
    font-size: 18px;
	color:white;
}
.heading{
   text-align: center;
    color: black;
    font-weight: bold;
    font-size: 22px;
}
.txtnew label {
    /* display: inline-block; */
    /* max-width: 100%; */
    display: block;
    text-align: left;
}
.form-control{
	margin-left: 0 !important;
	width: 100% !important;
}
		

</style>

<title>Refund Validation</title>
<link rel="icon" href="../image/favicon-32x32.png">
	<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
	<link href="../css/Jquerydatatable.css" rel="stylesheet" />
	<link rel="stylesheet" href="../css/loader.css">
	<link href="../css/default.css" rel="stylesheet" type="text/css" />
	<link href="../css/jquery-ui.css" rel="stylesheet" />
	<script src="../js/jquery.js"></script>
	<script src="../js/jquery.dataTables.js"></script>
	<script src="../js/jquery-ui.js"></script>
	<script type="text/javascript" src="../js/moment.js"></script>
	<script type="text/javascript" src="../js/daterangepicker.js"></script>
	<link href="../css/loader.css" rel="stylesheet" type="text/css" />
	<script src="../js/jquery.popupoverlay.js"></script>
	<script type="text/javascript" src="../js/dataTables.buttons.js"></script>
	<script type="text/javascript" src="../js/pdfmake.js"></script>
	<link href="../fonts/css/font-awesome.min.css" rel="stylesheet">
	
	
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
	
	
</head>
<body id="mainBody">

	<h2 class="pageHeading">Refund Validation</h2>
	<br>
	<br>
	 <table class="table98 padding0">
        
        <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
        <ul class="nav nav-tabs" style="border-bottom:none;">
        <li><a href="#RefundValidation">Refund Validation</a></li> 
        <%-- <li><a href="#saleDownload">Sale Download</a></li> --%> 		
    </ul>
    </td>
        </tr>
 
  <tr>
          <td align="center">&nbsp;</td>
          <td height="10" align="center">
		
		<!----------------------------FIRST TAB CONTENT------------------------->
        <section id="RefundValidation" class="tab-content hide">  
        <div>
            <br />
            <form id="refundValidation" name="refundValidation" method="post" action="refundValidationTicketing">
             <table class="table98 padding0 profilepage">
                <div style="margin-top:20px;">
		            <div class="form-group col-md-2 col-md-offset-2 txtnew col-sm-3 col-xs-6">
						<label for="merchant" style="margin-left: -20% !important;" >Merchant:</label> 
						<s:if
							test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'  || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
							<s:select name="validationMerchant" class="form-control" id="validationMerchant"
								headerKey="Select Merchant" headerValue="Select Merchant" list="merchantList"
								listKey="payId" listValue="businessName" onchange="handleChange();"   autocomplete="off" 
								style="margin-left: -40% !important;"/>
						</s:if>
						<s:else>
							<s:select name="validationMerchant" class="form-control" id="validationMerchant"
								headerKey="Select Merchant" headerValue="Select Merchant" list="merchantList"
								listKey="payId" listValue="businessName" onchange="handleChange();" autocomplete="off" />
						</s:else>
					 </div>
					 
					 
					 <div class="form-group  col-md-2 col-sm-4 txtnew  col-xs-6">
						<label for="email" style="margin-left: -20% !important;">Currency:</label> 
						<s:select name="validationCurrency" id="validationCurrency" headerValue="ALL"
								headerKey="ALL" list="currencyMap" class="form-control" style="margin-left: -40% !important;" />
					</div>
					
					<div class="form-group  col-md-2 col-sm-4 txtnew  col-xs-6">
						<label for="email" style="margin-left: -20% !important;">Download Type:</label> 
						<s:select name="downloadType" id="downloadType" headerValue="Select"
								headerKey="ALL" list="#{'VERSION 1':'VERSION 1', 'VERSION 2':'VERSION 2', 'VERSION 3':'VERSION 3'}" class="form-control" />
					</div>
					
					<div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
						<label for="dateFrom" style="margin-left: -55px !important; width: 120% !important;">Date From:</label> 
						<s:textfield type="text" readonly="true" id="refundRequestDate"
							name="refundRequestDate" class="form-control" onchange="handleChange();" 
							autocomplete="off" style="margin-left: -28px !important; width: 120% !important;"/>
					</div>
					
					<!-- <div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
						<label for="dateTo">Date To:</label> 
						<s:textfield type="text" readonly="true" id="validationDateTo" name="validationDateTo"
							class="form-control" onchange="handleChange();" autocomplete="off" style="width: 120% !important;"/>
					</div> -->
					<input type="hidden" id="refundValidationButtonIdentifier" name="refundValidationButtonIdentifier" value="">
				</div>
				
				<br>
					<br>
					<br>
					<br>
					<br>
					
					<div>
					  <button class="uploadButton" id="refundValidationDownloadBasic" onClick='submitRefundValidationBasicForm() 'style="width:22% !important;">Download</button>
					  <%-- <button class="uploadButton" id="refundValidationDownloadRRN" onClick='submitRefundValidationRRNForm()' style="width:22% !important;">Download with RRN</button> --%>
					</div>
					<br>
		
		</div>

              </table>
              </form>
        </div>  
        </section>      
        
		
		
		<!----------------------------SECOND TAB CONTENT------------------------->
		
		<section id="saleDownload" class="tab-content hide">  
        <div>
            <br />
            <form id="saleDownload" name="saleDownload" method="post" action="downloadSettlementReportAction">
             <table class="table98 padding0 profilepage">
                <div style="margin-top:20px;">
		            <div class="form-group col-md-2 col-md-offset-2 txtnew col-sm-3 col-xs-6">
						<label for="merchant" style="margin-left: -20% !important;" >Merchant:</label> 
						<s:if
							test="%{#session.USER.UserType.name()=='ADMIN' || #session.USER.UserType.name()=='SUBADMIN'  || #session.USER.UserType.name()=='SUBADMIN' || #session.USER_TYPE.name()=='SUPERADMIN'}">
							<s:select name="merchantPayId" class="form-control" id="merchantPayId"
								headerKey="ALL" headerValue="ALL" list="merchantList"
								listKey="payId" listValue="businessName" onchange="handleChange();"   autocomplete="off" 
								style="margin-left: -40% !important;"/>
						</s:if>
						<s:else>
							<s:select name="merchantPayId" class="form-control" id="merchantPayId"
								headerKey="ALL" headerValue="ALL" list="merchantList"
								listKey="payId" listValue="businessName" onchange="handleChange();" autocomplete="off" />
						</s:else>
					 </div>
					 
					 
					 <div class="form-group  col-md-2 col-sm-4 txtnew  col-xs-6">
						<label for="email" style="margin-left: -20% !important;">Currency:</label> 
						<s:select name="currency" id="currency" headerValue="ALL"
								headerKey="ALL" list="currencyMap" class="form-control" style="margin-left: -40% !important;" />
					</div>
					
					<div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
						<label for="dateFrom" style="margin-left: -55px !important; width: 120% !important;">Sale Date:</label> 
						<s:textfield type="text" readonly="true" id="saleDate"
							name="saleDate" class="form-control" onchange="handleChange();" 
							autocomplete="off" style="margin-left: -28px !important; width: 120% !important;"/>
					</div>
					
					<%-- <div class="form-group  col-md-2 col-sm-3 txtnew col-xs-6">
						<label for="dateTo">Date To:</label> 
						<s:textfield type="text" readonly="true" id="saleDateTo" name="saleDateTo"
							class="form-control" onchange="handleChange();" autocomplete="off" style="width: 120% !important;"/>
					</div> --%>
					
				</div>
				<br>
					<br>
					<br>
					<br>
					<br>
					
					<div>
					  <button class="uploadButton">Download</button>
					</div>
					<br>
	         	</div>

              </table>
              </form>
        </div>  
        </section>  
		
		
		
    
      </td>
    </table>
<script language="javascript" type="text/javascript">
	function submitDeltaRefundBasicForm() {
	    document.getElementById("deltaRefundButtonIdentifier").value = "BASIC";
	       $("#deltaRefund").submit();
	}
	
    function submitDeltaRefundRRNForm() {
	    document.getElementById("deltaRefundButtonIdentifier").value = "RRN";
	       $("#deltaRefund").submit();
    } 
    
    function submitRefundValidationBasicForm() {
	    document.getElementById("refundValidationButtonIdentifier").value = "BASIC";
	       $("#refundValidationAction").submit();
	}
    
    function submitRefundValidationRRNForm() {
	    document.getElementById("refundValidationButtonIdentifier").value = "RRN";
	       $("#refundValidationAction").submit();
    } 
</script>	
<script type="text/javascript">
	 function handleChange() {
     }
	$(document).ready(function() {
		
		$(function() {
			$("#refundRequestDate").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			<%-- $("#validationDateTo").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			}); --%>
		});
		$(function() {
			var today = new Date();
			<%--$('#validationDateTo').val($.datepicker.formatDate('dd-mm-yy', today)); --%>
			$('#refundRequestDate').val($.datepicker.formatDate('dd-mm-yy', today));
			

		});
	});
	
			
</script>
	
<script type="text/javascript">
	 function handleChange() {
     }
	$(document).ready(function() {
		
		$(function() {
			$("#deltaDateFrom").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			$("#deltaDateTo").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
		});
		$(function() {
			var today = new Date();
			$('#deltaDateTo').val($.datepicker.formatDate('dd-mm-yy', today));
			$('#deltaDateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
			

		});
	});
	
			
</script>

<script type="text/javascript">
	 function handleChange() {
     }
	$(document).ready(function() {
		
		$(function() {
			$("#refundDateFrom").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			$("#refundDateTo").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
		});
		$(function() {
			var today = new Date();
			$('#refundDateTo').val($.datepicker.formatDate('dd-mm-yy', today));
			$('#refundDateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
			

		});
	});
	
			
</script>

<script type="text/javascript">
	    function handleChange() {
			
     }

	$(document).ready(function() {
		
		$(function() {
			$("#dateFrom").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			$("#dateTo").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
		});
		$(function() {
			var today = new Date();
			$('#dateTo').val($.datepicker.formatDate('dd-mm-yy', today));
			$('#dateFrom').val($.datepicker.formatDate('dd-mm-yy', today));
			

		});
	});
	
			
</script>




<script type="text/javascript">
	    function handleChange() {
			
     }

	$(document).ready(function() {
		
		$(function() {
			$("#saleDate").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			});
			/* $("#saleDateTo").datepicker({
				prevText : "click for previous months",
				nextText : "click for next months",
				showOtherMonths : true,
				dateFormat : 'dd-mm-yy',
				selectOtherMonths : false,
				maxDate : new Date()
			}); */
		});
		$(function() {
			var today = new Date();
			/* $('#saleDateTo').val($.datepicker.formatDate('dd-mm-yy', today)); */
			$('#saleDate').val($.datepicker.formatDate('dd-mm-yy', today));
			

		});
	});
	
			
</script>

<script type="text/javascript">
		$("#form").submit(function(e) {
		  e.preventDefault();
		  var checkUploadFileExtension =  $('input[type=file]').val().replace(/C:\\fakepath\\/i, '').split('.').pop();

		  if(checkUploadFileExtension === 'txt') {
			$('#success').empty().text('File Uploaded Successfully');
			$('#errors').empty().text('');
             return false;
		  } else {
			$('#errors').empty().text('Please Choose Any Text File');
			$('#success').empty().text('');
             return false;
		  }
		});
</script>

<script type="text/javascript">
		$("#form2").submit(function(e) {
		  e.preventDefault();
		  var checkUploadFileExtension =  $('input[type=file]').val().replace(/C:\\fakepath\\/i, '').split('.').pop();

		  if(checkUploadFileExtension === 'txt') {
			$('#success').empty().text('File Uploaded Successfully');
			$('#errors').empty().text('');
             return false;
		  } else {
			$('#errors').empty().text('Please Choose Any Text File');
			$('#success').empty().text('');
             return false;
		  }
		});
</script>

</body>
</html>