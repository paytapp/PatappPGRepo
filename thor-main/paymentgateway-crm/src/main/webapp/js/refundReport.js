function handleSingleCheckBoxClick(rowNode, rowData){
		var checkBox = rowNode.cells[0].children[0];
		if(checkBox.checked){
				checkedRows.push(rowData);
		}
		var oldCheckedRows  = checkedRows;
		checkedRows = [];
		for(index in oldCheckedRows){
			var row = oldCheckedRows[index];
			if(row.transactionId!=checkBox.id){
				checkedRows.push(row);
			}else if(checkBox.checked){
				checkedRows.push(row);
			}
		}
		//if the current checkbox was only element
		if(checkedRows.length == 0){
			$("#selectAllCheckBox").attr('checked', false);
		}
	}

	function handleSelectALLClick(selectAllElement){
		//clear array
		checkedRows = [];
		var table = $('#refundReportDataTable').DataTable();
		var currentRows = table.rows({ page: 'current' });
		for(index in currentRows[0]){
			var row = currentRows.nodes()[index];
			if(null==row.cells){
				continue;
			}
			var childNodes = row.cells[0].children;
			var checkBox = childNodes[0];
			if(!checkBox.disabled && selectAllElement.checked){
				checkBox.checked = true;
				checkedRows.push(currentRows.data()[index])
			}else if(!checkBox.disabled && !selectAllElement.checked){
				checkBox.checked = false;
			}
		}
	}

	function processAll(){
		$.ajax({
			type : "POST",
			url : "settleAll",
			contentType: "application/json; charset=utf-8",
			dataType: "json",
			data : getProcessAllPostData(),
			success : function(data, status) {
				var response = ((data["Invalid request"] != null) ? (data["Invalid request"].response[0]) : (data.response));
				if(null!=response){
					alert(response);			
				}
				$('#selectAllCheckBox').attr('checked', false);
				handleSelectALLClick( $("#selectAllCheckBox"));
				//refresh table
				reloadTable(true);
			},
			error : function(status) {
				alert("Error processing the settlements");
			}
		})		
	}

	function getProcessAllPostData(){
		//checkedRows
		var settlementArray = [];
		var token  = document.getElementsByName("token")[0].value;
		for(index in checkedRows){
			var rowElement = checkedRows[index];
			var paymentType = rowElement.paymentMethod;
			var res = paymentType.split("-");
			var mopType = res[1].trim();
			var payment_method = res[0].trim();
			var txnType = "REFUND";
			var settlementObj = {
					payId: rowElement.payId,
					merchant:"",
					txnId: rowElement.transactionId,
					orderId: rowElement.orderId,
					customerEmail: decodeVal(rowElement.customerEmail),
					paymentMethod: decodeVal(payment_method),
					mop: decodeVal(mopType),
					currencyCode: rowElement.currencyCode,
					txnAmount: decodeVal(rowElement.refundedAmount),
					refund: decodeVal(rowElement.refundedAmount),
					tdr: decodeVal(rowElement.tdr),
					serviceTax: decodeVal(rowElement.serviceTax),
					netAmount: decodeVal(rowElement.refundedAmount),
					txnDate:decodeVal(rowElement.refundDate),
					merchantFixCharge: decodeVal(rowElement.merchantFixCharge),
					merchant: decodeVal(rowElement.businessName),
					txnType : decodeVal(txnType),
			};
			settlementArray.push(settlementObj);
		}
		return '{' + '"settlementList"'+':' + JSON.stringify(settlementArray) +',' + '"token":"'+ token +'"}';
	}

	function uncheckAllCheckBoxes(tableObj){
		$('#selectAllCheckBox').attr('checked', false);
		var inputElements = tableObj.getElementsByTagName('input');
		var index;
		for(index=0;index < inputElements.length; index++){
			var element = inputElements[index];
			if(element.type.toUpperCase() =='CHECKBOX'){
				element.checked = false;
			}
		}
	}

	function decodeVal(value) {
		var txt = document.createElement("textarea");
		txt.innerHTML = value;
		return txt.value;
	}

	function decodeDiv() {
		var divArray = document.getElementsByTagName('div');
		for (var i = 0; i < divArray.length; ++i) {
			var div = divArray[i];
			if (div.id.indexOf('param-') > -1) {
				var val = div.innerHTML;
				div.innerHTML = decodeVal(val);
			}
		}
	}
