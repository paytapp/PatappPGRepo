
	
	function hideColumn(){
		var _userType = $("[data-id='userType']").val();
		var td = $("#txnResultDataTable").DataTable();
		if(_userType == "SUBUSER"){
			td.columns(14).visible(false);
			td.columns(17).visible(false);
		}
	}

	$(document).ready(function() {
		if($("#gloc").val() == "true"){
		$("[data-id=deliveryStatus]").removeClass("d-none");
		$("[data-id=deliveryStatus] select").selectpicker();
		$("[data-id=deliveryStatus] select").selectpicker('val', "All");
	}

		var _select = "<option value='ALL'>ALL</option>"
		$("[data-id=subMerchant]").find('option:eq(0)').before(_select);
        $("[data-id=subMerchant] option[value=ALL]").attr("selected", "selected");
        

		function getSubMerchant(_this, _url, _object){
		
			var _merchant = _this.target.value;
			var _subMerchantAttr = _this.target.attributes["data-submerchant"].nodeValue;
			var _subUserAttr = _this.target.attributes["data-user"].nodeValue;
			if(_merchant != ""){
				document.querySelector("body").classList.remove("loader--inactive");
				var data = new FormData();
				data.append('payId', _merchant);
				data.append('vendorReportFlag', true);
				var _xhr = new XMLHttpRequest();
				_xhr.open('POST', _url, true);
				_xhr.onload = function(){
					if(_xhr.status === 200){
						var obj = JSON.parse(this.responseText);
						console.log(obj);
						var  _option = "";
						if(_object.isSuperMerchant == true){
							if(obj.superMerchant == true){
								document.querySelector("#"+_subMerchantAttr).innerHTML = "";
								_option += document.querySelector("#"+_subMerchantAttr).innerHTML = "<option value='ALL'>ALL</option>";
								for(var i = 0; i < obj.subMerchantList.length; i++){
									_option += document.querySelector("#"+_subMerchantAttr).innerHTML += "<option value="+obj.subMerchantList[i]["payId"]+">"+obj.subMerchantList[i]["businessName"]+"</option>";
								}
								document.querySelector("[data-target="+_subMerchantAttr+"]").classList.remove("d-none");
								document.querySelector("#"+_subMerchantAttr+" option[value='ALL']").selected = true;
								$("#"+_subMerchantAttr).selectpicker();
								$("#"+_subMerchantAttr).selectpicker('refresh');
							}else{
								document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
								document.querySelector("#"+_subMerchantAttr).value = "";
							}
						}
						if(_object.subUser == true){
							if(obj.subUserList.length > 0){
								
								_option += document.querySelector("#"+_subUserAttr).innerHTML = "<option value='ALL'>ALL</option>";
								for(var i = 0; i < obj.subUserList.length; i++){
									_option += document.querySelector("#"+_subUserAttr).innerHTML += "<option value="+obj.subUserList[i]["payId"]+">"+obj.subUserList[i]["businessName"]+"</option>";
								}
								document.querySelector("[data-target="+_subUserAttr+"]").classList.remove("d-none");
								document.querySelector("#"+_subUserAttr+" option[value='ALL']").selected = true;
								$("#"+_subUserAttr).selectpicker();
								$("#"+_subUserAttr).selectpicker('refresh');
							}else{
								document.querySelector("[data-target="+_subUserAttr+"]").classList.add("d-none");
								document.querySelector("#"+_subUserAttr).value = "";
							}
						}
						if(_object.glocal == true){
							if(obj.glocalFlag == true){
								document.querySelector("[data-id=deliveryStatus]").classList.remove("d-none");
								$("[data-id=deliveryStatus] select").selectpicker('val', 'All');
							}else{
								document.querySelector("[data-id=deliveryStatus]").classList.add("d-none");
							}
						}else{

						}
					}
				}
				_xhr.send(data);
				setTimeout(function(e){
					document.querySelector("body").classList.add("loader--inactive");
				}, 1000);
			}else{
				document.querySelector("[data-target="+_subMerchantAttr+"]").classList.add("d-none");
				document.querySelector("#"+_subMerchantAttr).value = "";
			}
		}

		document.querySelector("#merchant").addEventListener("change", function(e){
			getSubMerchant(e, "getSubMerchantList", {
				isSuperMerchant : true,
				subUser : true
			});
		});

		document.querySelector("#subMerchant").addEventListener("change", function(e){
			getSubMerchant(e, "vendorTypeSubUserListAction", {
				subUser : true
			});
		})
	


		// $("#merchant").on("change", function(e){
		// 	var _merchant = $(this).val();
		// 	if(_merchant != ""){
		// 		$("body").removeClass("loader--inactive");
		// 		$.ajax({
		// 			type: "POST",
		// 			url: "getSubMerchantList",
		// 			data: {"payId": _merchant},
		// 			success: function(data){
        //                 $("#subMerchant").html("");
        //                 console.log(data);
		// 				if(data.superMerchant == true){
		// 					var _option = $("#subMerchant").append("<option value='ALL'>ALL</option>");
		// 					for(var i = 0; i < data.subMerchantList.length; i++){
		// 						_option += $("#subMerchant").append("<option value="+data.subMerchantList[i]["emailId"]+">"+data.subMerchantList[i]["businessName"]+"</option>")
		// 					}
		// 					$("[data-id=submerchant]").removeClass("d-none");
		// 					$("#subMerchant option[value='']").attr("selected", "selected");
		// 					$("#subMerchant").selectpicker();
		// 					$("#subMerchant").selectpicker("refresh");
		// 					setTimeout(function(e){
		// 						$("body").addClass("loader--inactive");
		// 					},500);
		// 				}else{
		// 					setTimeout(function(e){
		// 						$("body").addClass("loader--inactive");
		// 					},500);
		// 					$("[data-id=submerchant]").addClass("d-none");
		// 					$("[data-id=deliveryStatus]").addClass("d-none");
		// 					$("[data-id=deliveryStatus]").val("");
		// 					$("#subMerchant").val("");
		// 				}
					
		// 				if(data.glocalFlag == true){
		// 				$("[data-id=deliveryStatus]").removeClass("d-none");
		// 				$("[data-id=deliveryStatus] select").selectpicker('val', 'All');
		// 				}else{
		// 					$("[data-id=deliveryStatus]").addClass("d-none");
		// 				}
		// 			}
		// 		});
		// 	}else{
		// 		$("[data-id=submerchant]").addClass("d-none");
		// 		$("#subMerchant").val("");
		// 		$("[data-id=deliveryStatus]").addClass("d-none");
		// 		$("[data-id=deliveryStatus]").val("");	
		// 	}
		// })

		$(function() {
			renderTable();
		});

		$("#submit").click(function(env) {
			$("body").removeClass("loader--inactive");
			$("#setSuperMerchant").val('');
			reloadTable();
			// hideColumn();
		});
	});

	function renderTable() {
		var merchantEmailId = document.getElementById("merchant").value;
		var table = new $.fn.dataTable.Api('#txnResultDataTable');

		var transFrom = $.datepicker
				.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());
		if (transFrom == null || transTo == null) {
			alert('Enter date value');
			return false;
		}

		if (transFrom > transTo) {
			alert('From date must be before the to date');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}
		if (transTo - transFrom > 31 * 86400000) {
			alert('No. of days can not be more than 31');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}
		var token = document.getElementsByName("token")[0].value;

		var buttonCommon = {
			exportOptions : {
				format : {
					body : function(data, column, row, node) {
						// Strip $ from salary column to make it numeric
						return column === 0 ? "'" + data : (column === 1 ? "'"
								+ data : data);
					}
				}
			}
		};

		$('#txnResultDataTable').dataTable({
			
			"columnDefs" : [{
					className : "dt-body-right",
					"targets" : [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]
				}],
			dom : 'BTrftlpi',
			buttons : [
				$.extend(true, {}, buttonCommon, {
					extend : 'copyHtml5',
					exportOptions : {
						columns : [ 0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14]
					},
				}),
				$.extend(true, {}, buttonCommon, {
					extend : 'csvHtml5',
					title : 'Product Wise Report',
					exportOptions : {

						columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14]
					},
				}),
				{
					extend : 'pdfHtml5',
					orientation : 'landscape',
					pageSize : 'legal',
					//footer : true,
					title : 'Product Wise Report',
					exportOptions : {
						columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14]
					},
					customize : function(doc) {
						doc.defaultStyle.alignment = 'center';
						doc.styles.tableHeader.alignment = 'center';
					}
				},
				{
					extend : 'print',
					//footer : true,
					title : 'Product Wise Report',
					exportOptions : {
						columns : [ 0, 1, 2, 3, 4, 6, 7, 8,	9, 10, 11, 12, 13, 14]
					}
				},
				{
					extend : 'colvis',
					columns : [ 0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 14]
				} ],

				"ajax" : {

					"url" : "khadiProductReportSearchAction",
					"type" : "POST",
					"data" : function(d) {
						return generatePostData(d);
					}
				},
				"fnDrawCallback" : function() {
					hideColumn();
					$("#submit").removeAttr("disabled");
					$("body").addClass("loader--inactive");
				},
				"searching" : false,
				"ordering" : false,
				"destroy": true,
				"processing" : true,
				"serverSide" : true,
				"paginationType" : "full_numbers",
				"lengthMenu" : [ [ 10, 25, 50 ], [ 10, 25, 50 ] ],
				"order" : [ [ 2, "desc" ] ],
				"columnDefs" : [ {
					"type" : "html-num-fmt",
					"targets" : 4,
					"orderable" : true,
					"targets" : [ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16]
				},
				
				{
					'targets': 0,
					'createdCell':  function (td, cellData, rowData, row, col) {
						$("#setSuperMerchant").val(rowData["subMerchantId"]);
						$("#deliveryStatusFlag").val(rowData["deliveryStatus"]);
					}
				}],

				"columns" : [
						{
							"data" : "transactionId",
							"className" : "txnId my_class text-class",
							
						},
						{
							"data" : "pgRefNum",
							"className" : "payId text-class"

						},
						{
							"data" : "orderId",
							"className" : "text-class"
						},

						{
							"data" : "subMerchantName",
							"className" : "text-class"
						},

						{
							"data" : "subMerchantId",
							"className" : "text-class"
						},

						{
							"data" : "productId",
							"className" : "text-class",
							"width" : "10%"
						},
						{
							"data" : "SKUCode",
							"className" : "orderId text-class"
						},
						
						{
							"data" : "categoryCode",
							"className" : "text-class",
							"width" : "10%"
						},
						{
							"data" : "txnType",
							"className" : "text-center",
							"width" : "10%"
						},
						{
							"data" : "paymentMethods",
							"className" : "text-center",
							"width" : "10%"
						},
						{
							"data" : "mopType",
							"className" : "text-class",
							"width" : "10%"
						},
						{
							"data" : "paymentRegion",
							"className" : "text-class",
							"width" : "10%"
						},
						
						
						{
							"data" : "cardHolderType",
							"className" : "text-class"								
						},
						{
							"data" : "productPrice",
							"className" : "txnType text-class"
						},
						{
							"data" : "totalAmount",
							"className" : "status text-class"
						},
						{
							"data" : "currency",
							"className" : "text-class"

						},{
							"data" : "refundDays",
							"className" : "text-class"
					
						},{
							"data" : null,
							"className" : "text-class",
							"mRender": function(row){
								if(row.showRefundButton == true){
									return '<button class="lpay_button lpay_button-md lpay_button-secondary btnRefund" style="font-size:10px;">Refund</button>';
								}else{
									return 'Refunded';
								}
							}
						},
					]
				});
				$(document).ready(function(e){
					var table = $('#txnResultDataTable').DataTable();
					$('#txnResultDataTable').on('click','.btnRefund',function() {
						var _btn = $(this).text();
						var _parent = $(this).closest("td");
						var _tr = $(this).closest("tr");
						if(_btn !== "Refunded") {
							var columnIndex = table.cell(_parent).index().column;
							var rowIndex = table.cell(_parent).index().row;
							var rowNodes = table.row(rowIndex).node();
							var rowData = table.row(rowIndex).data();
							var _refundAvailable = _tr.find(".txnType").text();
							var txnType1 = rowData.txnType;
							var totalAmount = rowData.totalAmount;
							var _id = rowData.objectId;



							var _pgRefNum =  rowData.pgRefNum;
							var _payId = rowData.payId;

							var orderId1 = rowData.orderId; 					 
							var txnId1 = Number(rowData.transactionId); 

							var refundAvailable = rowData.refundAvailable;
							var refundedAmount = rowData.refundedAmount;

							// $("#refundedAmount").val(refundedAmount);
							$("#refundAvailable").val(_refundAvailable);
							$("#setId").val(_id);
							$("#payId").val(_payId);
							$("#pg-ref").val(_pgRefNum);

							$("body").removeClass("loader--inactive");
							
							$("#manualRefundProcess").submit();
						}
					});
				})
			}

	function reloadTable() {
		var datepick = $.datepicker;
		var transFrom = $.datepicker.parseDate('dd-mm-yy', $('#dateFrom').val());
		var transTo = $.datepicker.parseDate('dd-mm-yy', $('#dateTo').val());

		if (transFrom == null || transTo == null) {
			alert('Enter date value');
			return false;
		}

		if (transFrom > transTo) {
			alert('From date must be before the to date');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}
		if (transTo - transFrom > 31 * 86400000) {
			alert('No. of days can not be more than 31');
			$("body").addClass("loader--inactive");
			$('#dateFrom').focus();
			return false;
		}
		$("#submit").attr("disabled", true);
		var tableObj = $('#txnResultDataTable');
		var table = tableObj.DataTable();
		table.ajax.reload();
	}
	
	function generatePostData(d) {

		var obj = {};
		
		var _getAllInput = document.querySelectorAll("[data-var]");
        _getAllInput.forEach(function(index, element, array){
           var _new =  _getAllInput[element].closest(".col-md-3").classList;
           var _newVal = _new.toString().indexOf("d-none");
           if(_newVal == -1){
               obj[_getAllInput[element].name] = _getAllInput[element].value
		   }
        })

        obj.token = document.getElementsByName("token")[0].value;
        obj.draw = d.draw;
        obj.length = d.length;
        obj.start = d.start;
        obj["struts.token.name"] = "token";

        if(obj.merchant == ""){
            obj.merchant = "ALL"
        }

        if(obj.paymentType == ""){
            obj.paymentType = "ALL"
        }

        if(obj.currency == ""){
            obj.currency = "ALL";
		}
		
		console.log(obj);

		return obj;
	}