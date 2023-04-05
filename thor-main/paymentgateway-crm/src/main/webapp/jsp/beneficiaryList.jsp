<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Beneficiary list</title>
<link rel="icon" href="../image/favicon-32x32.png">
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<script src="../js/jquery.minshowpop.js"></script>
<script src="../js/jquery.formshowpop.js"></script>
<script src="../js/commanValidate.js"></script>
<link href="../css/default.css" rel="stylesheet" type="text/css" />
<link rel="stylesheet" type="text/css" media="all" href="../css/daterangepicker-bs3.css" />
<link href="../css/Jquerydatatableview.css" rel="stylesheet" />
<link href="../css/jquery-ui.css" rel="stylesheet" />
<link href="../css/Jquerydatatable.css" rel="stylesheet" />
<script src="../js/jquery.min.js" type="text/javascript"></script>
<script src="../js/moment.js" type="text/javascript"></script>
<script src="../js/daterangepicker.js" type="text/javascript"></script>
<script src="../js/jquery.dataTables.js"></script>
<script src="../js/jquery-ui.js"></script>
<script src="../js/commanValidate.js"></script>
<script src="../js/jquery.popupoverlay.js"></script>
<script src="../js/dataTables.buttons.js" type="text/javascript"></script>
<script src="../js/pdfmake.js" type="text/javascript"></script>

<script src="../js/jszip.min.js" type="text/javascript"></script>
<script src="../js/vfs_fonts.js" type="text/javascript"></script>
<script src="../js/buttons.colVis.min.js" type="text/javascript"></script>

<link href="../css/select2.min.css" rel="stylesheet" />
<script src="../js/jquery.select2.js" type="text/javascript"></script>

<script language="JavaScript">	
$(document).ready( function () {

});
	
</script>
<script type="text/javascript">
	$(document).ready(function() {

	  $('#acquirer').change(function(event){
		  reloadTable();
		
		   
	   });
	   
		$(function() {			
			renderTable();
		});


	});
		
	function editBeneficiary(data){
		
			var answer = confirm("Are you sure you want to edit acquirer details?");
		if (answer != true) {
			
			return false;
		} else {
			
			var id = data.parentElement.parentElement.cells[0].innerText;
			document.getElementById("param").value = "EDIT";
			document.getElementById("frmId").value = id;
			document.getElementById("frmEditBeneficiary").submit();		
			}
	  
	}

	function removeBeneficiary(data){
		
			var answer = confirm("Are you sure you want to remove acquirer details?");
		if (answer != true) {
			
			return false;
		} else {
			var id = data.parentElement.parentElement.cells[0].innerText;
			document.getElementById("param").value = "EDIT";
			document.getElementById("frmId").value = id;
			document.getElementById("frmEditBeneficiary").submit();	
			}

	}
	
	function renderTable() {
		 var acquirer = document.getElementById("acquirer").value;
		var table = new $.fn.dataTable.Api('#beneficiaryListResultDataTable');
		var token = document.getElementsByName("token")[0].value;

		
		 var buttonCommon = {
        exportOptions: {
            format: {
                body: function ( data, column, row, node ) {
                    // Strip $ from salary column to make it numeric
                    return column === 0 ? "'"+data : (column === 1 ? "'" + data: data);
                }
            }
        }
    };
	
		$('#beneficiaryListResultDataTable').dataTable(
						{
							"footerCallback" : function(row, data, start, end, display) {
								var api = this.api(), data;

								
							},
							"columnDefs": [{ 
								className: "dt-body-right",
								"targets": [0, 1, 2, 3, 4, 5, 6]
							}],
								dom : 'BTrftlpi',
								buttons : [
										$.extend( true, {}, buttonCommon, {
											extend: 'copyHtml5',											
											exportOptions : {											
												columns : [0, 1, 2, 3, 4, 5, 6]
											},
										} ),
									$.extend( true, {}, buttonCommon, {
											extend: 'csvHtml5',
											title : 'Beneficiary List',
											exportOptions : {
												
												columns : [0, 1, 2, 3, 4, 5, 6]
											},
										} ),
									{
										extend : 'pdfHtml5',
										orientation : 'landscape',
										pageSize: 'legal',
										//footer : true,
										title : 'Beneficiary List',
										exportOptions : {
											columns: [0, 1, 2, 3, 4, 5, 6]
										},
										customize: function (doc) {
										    doc.defaultStyle.alignment = 'center';
					     					doc.styles.tableHeader.alignment = 'center';
										  }
									},
									{
										extend : 'print',
										//footer : true,
										title : 'Beneficiary List',
										exportOptions : {
											columns : [0, 1, 2, 3, 4, 5, 6]
										}
									},
									{
										extend : 'colvis',
										columns : [0, 1, 2, 3, 4, 5, 6]
									} ],

							"ajax" :{
								
								"url" : "beneficiarySearchAction",
								"type" : "POST",
								"data": function (d){
									return generatePostData(d);
								}
							},
							"fnDrawCallback" : function() {

									 $("#submit").removeAttr("disabled");
							},
							 "searching": false,
							 "ordering": false,
							 "processing": true,
						        "serverSide": false,
								"paging":true,
								"pageLength": 10,
						        "paginationType": "full_numbers", 
						        "lengthMenu": [[10,15,20,25,-1], [10,15,20,25,'All']],
								"order" : [ [ 2, "desc" ] ],
						       
						        "columnDefs": [
						            {
						            "type": "html-num-fmt", 
						            "targets": 4,
						            "orderable": true, 
						            "targets": [0,1,2,3,4,5,6]
						            }
						        ], 

 
							"columns" : [ {
								"data" : "id",
								"className" : "text-class"
							},{
								"data" : "custId",
								"className" : "text-class"
							},  {
								"data" : "srcAccountNo",
								"className" : "text-class"
								
							},{
								"data" : "beneficiaryCd",
								"className" : "text-class"
							}, {
								"data" : "beneName",
								"className" : "text-class"
							}, {
								"data" : "beneAccountNo",
								"className" : "text-class"
							}, {
								"data" : "ifscCode",
								"className" : "text-class"
							},{
								"data" : "paymentType",
								"className" : "text-class"
							},  {
								"data" : "beneType",
								"className" : "text-class"
							}, {
								"data" : "status",
								"className" : "text-class"
							}
							]
						});
						
		
			
		
	}

	function reloadTable() {
		$("#submit").attr("disabled", true);
		var tableObj = $('#beneficiaryListResultDataTable');
		var table = tableObj.DataTable();
		table.ajax.reload();
	}

	function generatePostData(d) {
		var token = document.getElementsByName("token")[0].value;
		var acquirer = document.getElementById("acquirer").value;
		if(acquirer==''){
			acquirer='ALL'
		}
		
		var obj = {
			acquirer : acquirer,
			draw : d.draw,
			length :d.length,
			start : d.start, 
			token : token,
			"struts.token.name" : "token",
		};

		return obj;
	}

</script>
<style type="text/css">.error-text{color:#a94442;font-weight:bold;background-color:#f2dede;list-style-type:none;text-align:center;list-style-type: none;margin-top:10px;
}.error-text li { list-style-type:none; }
#response{color:green;}
.errorMessage{
  display: none;
}
.errorInpt{
      font: 400 11px arial ;
      color: red;
      display: none;
      margin-left: 7px;
}
.fixHeight{
  height: 64px;
}
.adduT{
  margin-bottom: 0 !important;
}
.btnSbmt{
  padding: 5px 10px !important;
    margin-right: 26px !important;
}
.actionMessage {
    border: 1px solid transparent;
    border-radius: 0 !important;
    width: 100% !important;
    margin: 0 !important;

}
</style>
<style type="text/css">
.cust {width: 24%!important; margin:0 5px !important; /*font: bold 10px arial !important;*/}
.samefnew{
	width: 24%!important;
    margin: 0 5px !important;
    /*font: bold 10px arial !important;*/
}
.btn {padding: 3px 7px!important; font-size: 12px!important; }
.samefnew-btn{
    width: 15%;
    float: left;
    font: bold 11px arial;
    color: #333;
    line-height: 22px;
    margin-left: 5px;
}
/*tr td.my_class{color:#000 !important; cursor: default !important; text-decoration: none;}*/
tr td.my_class{
	cursor: pointer;
}
tr td.my_class:hover{
	cursor: pointer !important;
}

tr th.my_class:hover{
	color: #fff !important;
}

.cust .form-control, .samefnew .form-control{
	margin:0px !important;
	width: 100%;
}
.select2-container{
	width: 100% !important;
}
.clearfix:after{
	display: block;
	visibility: hidden;
	line-height: 0;
	height: 0;
	clear: both;
	content: '.';
}
#popup{
	position: fixed;
	top:0px;
	left: 0px;
	background: rgba(0,0,0,0.7);
	width: 100%;
	height: 100%;
	z-index:999; 
	display: none;
}
.innerpopupDv{
	    width: 600px;
    margin: 80px auto;
    background: #fff;
    padding: 3px 10px;
    border-radius: 10px;
}
.btn-custom {
    margin-top: 5px;
    height: 27px;
    border: 1px solid #5e68ab;
    background: #5e68ab;
    padding: 5px;
    font: bold 12px Tahoma;
    color: #fff;
    cursor: pointer;
    border-radius: 5px;
}
#loader-wrapper .loader-section.section-left, #loader-wrapper .loader-section.section-right{
	background: rgba(225,225,225,0.6) !important;
	width: 50% !important;
}
.invoicetable{
	float: none;
}
.innerpopupDv h2{
	    font-size: 12px;
    padding: 5px;
}
.text-class{
	text-align: center !important;
}
.odd{
	background-color: #e6e6ff !important;
}
 
</style>
</head>
<body>
		<table width="100%" border="0" cellspacing="0" cellpadding="0" class="txnf">
		  <tr>
			<td align="left"><h2>Beneficiaries List</h2></td>
		  </tr>
		 
				  <s:if test="%{responseObject.responseCode=='000'}">
						<tr>
							<td align="left" valign="top"><div id="saveMessage">
								<s:actionmessage class="success success-text" />
							  </div>
							</td>
						</tr>
				  
				  </s:if>
				<s:else><div class="error-text"><s:actionmessage/></div></s:else>

								<tr>
									<td align="left">
										<div class="container">
											<div class="form-group col-md-3 txtnew col-sm-3 col-xs-4">
											
												<s:select headerKey="ALL" headerValue="Select Nodal Acquirer" class="form-control"
												list="@com.paymentgateway.commons.util.AcquirerTypeNodal@values()"
												listValue="name" listKey="code" name="acquirer"
												id="acquirer" autocomplete="off" value=""/>
											</div>
										</div>
									</td>
								</tr>
								
					<tr>
						<td align="left" valign="top">&nbsp;</td>
					</tr>
		</table>

<div id = "beneficiaryListDiv" style="overflow:scroll !important;">
	<table id="mainTable" width="100%" border="0" align="center"
		cellpadding="0" cellspacing="0" class="txnf">
		
		<tr>
			<td colspan="5" align="left"><h2>&nbsp;</h2></td>
		</tr>
		<tr>
			<td align="left" style="padding: 10px;">
				<div class="scrollD">
					<table id="beneficiaryListResultDataTable" class="" cellspacing="0"
						width="100%">
						<thead>
							<tr class="boxheadingsmall">
							    <th style='text-align: center;text-decoration:none!important;'>Id</th>
								<th style='text-align: center;text-decoration:none!important;'>Cust Id</th>
								<th style='text-align: center'>Nodal Account No</th>
								<th style='text-align: center'>Beneficiary Cd</th>
								<th style='text-align: center'>Bene Name</th>
								<th style='text-align: center'>Bene Account No</th>
								<th style='text-align: center'>IFSC Code</th>
								<th style='text-align: center'>Payment Type</th>								
								<th style='text-align: center'>Bene Type</th>								
								<th style='text-align: center'>Status</th>
								
							</tr>
						</thead>
						<tfoot>
							<tr class="boxheadingsmall">
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>
								<th></th>				
								<th></th>
								<th></th>
									
							</tr>
						</tfoot>
					</table>
				</div>
			</td>
		</tr>

	</table>
  </div>

  <s:form action="editBeneficiaryAction" id="frmEditBeneficiary">
  		<s:hidden name="token" value="%{#session.customToken}"></s:hidden>
		<s:hidden name="frmId" id="frmId" value=""></s:hidden>
		<s:hidden name="param" id="param" value=""></s:hidden>
	</s:form>
</body>
</html>