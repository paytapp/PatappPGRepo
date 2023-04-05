var OffusList = [];

class Offus {
	constructor(merchant, currency, payment_type, mop, transaction_type, acquirer, onUsFlag, cardHolderType, paymentsRegion, slabId, minAmount, maxAmount) {
		this.merchant = merchant;
		this.currency = currency;
		this.paymentType = payment_type;
		this.mopType = mop;
		this.transactionType = transaction_type;
		this.acquirerMap = acquirer;
		this.onUsFlag = onUsFlag;
		this.payId = merchant;
		this.cardHolderType = cardHolderType;
		this.paymentsRegion = paymentsRegion;
		this.slabId = slabId;
		this.minAmount = minAmount;
		this.maxAmount = maxAmount;
	}
}


// var getMinMax = function(id) {
// 	var str = document.querySelector("label[for='"+ id +"']").innerText;
// 	str = str.replace(/\s/g,'');
// 	str = str.split("-");

// 	minAmount = str[0];
// 	maxAmount = str[1];
// }

var getOffUs = function() {
	var ou_row;
	var ou_currency = [];
	var ou_txnType = [];
	var ou_paymentType = [];
	var ou_mopType = [];
	var ou_Acquirer = [];
	var errormessage = "";
	var acquirerMessage = "";
	var CurrentOffusListLength;
	var match;
	var matchNumber = 0;
	var onUsFlag = true;
	var OffusListTemp = [];
	var merchant;
	var payId;
	var cardHolderType = [];
	var paymentsRegion = [];
	var slabId = [];
	var slabValue = [];
	var minAmount;
	var maxAmount;

	if ($('#offus_section input[name="currency"]:checked').length < 1) {
		errormessage = errormessage + "Please choose at least one Currency\n";
	} else {
		$('#offus_section input[name="currency"]:checked').each(function() {
			ou_currency.push($(this).val());
		});
	}

	if ($('#offus_section input[name="txnType"]:checked').length < 1) {
		errormessage = errormessage + "Please choose at least one Transection Type\n";
	} else {
		$('#offus_section input[name="txnType"]:checked').each(function() {
			ou_txnType.push($(this).val());
		});
	}

	if ($('#offus_section input[name="paymentType"]:checked').length < 1) {
		errormessage = errormessage + "Please choose at least one Payment Type\n";
	} else {
		$('#offus_section input[name="paymentType"]:checked').each(function() {
			ou_paymentType.push($(this).val());
		});
	}

	if ($('#offus_section input[name="mopType"]:checked').length < 1) {
		errormessage = errormessage + "Please choose at least one Transection Mop Type\n";
	} else {
		$('#offus_section input[name="mopType"]:checked').each(function() {
			ou_mopType.push($(this).val());
		});
	}

	if ($('.AcquirerList input[name^="Acquirer"]:checked').length < 1) {
		errormessage = errormessage + "Please choose at least one Acquirer\n";
	} else if ($('.AcquirerList input[name^="Acquirer"]:checked').length < $('.AcquirerList > [class^="Acquirer"]').length) {
		acquirerMessage = "Please Choose Acquirer preference or remove unused preference!";
	} else {
		var acq_pref_count = 1;
		$('.AcquirerList input[name^="Acquirer"]:checked').each(function() {
			ou_Acquirer.push(acq_pref_count + "-" + $(this).val());
			// alert(ou_Acquirer);
			acq_pref_count++;
		});
	}

	merchant = $('#offus_merchant').val();

	if($('#offus_section input[name="region"]:checked').length < 1) {
		errormessage = errormessage + "Plese choose at lease one Region\n";
	} else {
		$('#offus_section input[name="region"]:checked').each(function() {
			paymentsRegion.push($(this).val());
		});
	}

	if($('#offus_section input[name="typeCard"]:checked').length < 1) {
		errormessage = errormessage + "Plese choose at lease one Cardholder Type\n";
	} else {
		$('#offus_section input[name="typeCard"]:checked').each(function() {
			cardHolderType.push($(this).val());
		});
	}

	if($('#offus_section input[name="slabId"]:checked').length < 1) {
		errormessage = errormessage + "Plese choose at lease one Amount Slab\n";
	} else {
		$('#offus_section input[name="slabId"]:checked').each(function() {
			slabId.push($(this).val());
			let labelVal = $(this).closest(".wwgrp").find("label").text();
			slabValue.push(labelVal);
		});
	}

	if (errormessage != "") {
		swal({
			title : "",
			text : errormessage
		});
	} else if (acquirerMessage != "") {
		swal({
			title : "",
			text : acquirerMessage
		});
	} else {

		CurrentOffusListLength = OffusList.length;

		for (var ou_currency_key = 0; ou_currency_key < ou_currency.length; ou_currency_key++) {
			for (var ou_paymentType_key = 0; ou_paymentType_key < ou_paymentType.length; ou_paymentType_key++) {
				for (var ou_mopType_key = 0; ou_mopType_key < ou_mopType.length; ou_mopType_key++) {
					for (var ou_txnType_key = 0; ou_txnType_key < ou_txnType.length; ou_txnType_key++) {
						for(let paymentsRegionKey = 0; paymentsRegionKey < paymentsRegion.length; paymentsRegionKey++) {
							for(let cardHolderTypeKey = 0; cardHolderTypeKey < cardHolderType.length; cardHolderTypeKey++) {
								for(let slabIdKey = 0; slabIdKey < slabId.length; slabIdKey++) {
									onUsFlag = false;
									
									let str = slabValue[slabIdKey];
									str = str.replace(/\s/g,'');
									str = str.split("-");
								
									minAmount = str[0];
									maxAmount = str[1];
									
			
									ou_row = new Offus(
										merchant,
										ou_currency[ou_currency_key],
										ou_paymentType[ou_paymentType_key],
										ou_mopType[ou_mopType_key],
										ou_txnType[ou_txnType_key],
										ou_Acquirer.join(', '),
										onUsFlag,
										cardHolderType[cardHolderTypeKey],
										paymentsRegion[paymentsRegionKey],
										slabId[slabIdKey],
										minAmount,
										maxAmount
									);
			
									match = false;
			
									for (var a = 0; a < CurrentOffusListLength; a++) {
										if ((ou_row.merchant === OffusList[a].merchant || ou_row.merchant != "ALL MERCHANTS"
												&& OffusList[a].merchant == "ALL MERCHANTS")
												&& (ou_row.currency === OffusList[a].currency)
												&& (ou_row.paymentType === OffusList[a].paymentType)
												&& (ou_row.mopType === OffusList[a].mopType)
												&& (ou_row.transactionType === OffusList[a].transactionType)
												&& (ou_row.onUsFlag === OffusList[a].onUsFlag)
												&& (ou_row.cardHolderType === OffusList[a].cardHolderType)
												&& (ou_row.paymentsRegion === OffusList[a].paymentsRegion)
												&& (ou_row.slabId === OffusList[a].slabId)
												&& (ou_row.minAmount === OffusList[a].minAmount)
												&& (ou_row.maxAmount === OffusList[a].maxAmount)) {
											match = true;
											matchNumber++;
										}
									}
			
									if (match == false) {
										OffusListTemp.push(ou_row);
									}
								}
							}
						}						
					}
				}
			}
		}

		$('#offus_section input[type="checkbox"]:checked').removeAttr('checked');
	}

	if (matchNumber > 0) {
		swal({
			type : "info",
			title : matchNumber + " Match already exist!",
			type : "info"
		});
	}

	if (OffusListTemp.length > 0) {
		var listData = {
			values : OffusListTemp
		};
		var token = document.getElementsByName("token")[0].value;
		var data1 = "";
		data1 = data1.concat("{", "\"", "listData", "\"", ":", JSON.stringify(OffusListTemp), ",\"", "token", "\":\"", token, "\"", "}");

		$.ajax({
			type : "POST",
			url : "onusoffusRulesSetup",
			timeout : 0,
			dataType : "json",
			contentType : "application/json; charset=utf-8",
			data : data1,
			success : function(data) {
				if (OffusListTemp.length > 0) {
					var response = data.response;
					swal({
						title : response,
						type : "success"
					}, function() {
						window.location.reload();
					});
				}
			},
			error : function(data) {
				window.location.reload();
			}
		});
	}
};

// ///////////////////////////////////////////
// ///////Get On Us Functions Start Here////////
// ///////////////////////////////////////////

var getOnUs = function() {
	var ou_row;
	var ou_currency = [];
	var ou_txnType = [];
	var ou_paymentType = [];
	var ou_mopType = [];
	var ou_Acquirer = [];
	var errormessage = "";
	var CurrentOffusListLength;
	var match;
	var matchNumber = 0;
	var onUsFlag = true;
	var OffusListTemp = [];
	var merchant;
	var cardHolderType = [];
	var paymentsRegion = [];
	var slabId = [];
	var slabValue = [];
	var minAmount;
	var maxAmount;


	if ($('#onus_section input[name="currency"]:checked').length < 1) {
		errormessage = errormessage + "Please choose at least one Currency\n";
	} else {
		$('#onus_section input[name="currency"]:checked').each(function() {
			ou_currency.push($(this).val());
		});
	}

	if ($('#onus_section input[name="txnType"]:checked').length < 1) {
		errormessage = errormessage + "Please choose at least one Transection Type\n";
	} else {
		$('#onus_section input[name="txnType"]:checked').each(function() {
			ou_txnType.push($(this).val());
		});
	}

	if ($('#onus_section input[name="paymentType"]:checked').length < 1) {
		errormessage = errormessage + "Please choose at least one Payment Type\n";
	} else {
		$('#onus_section input[name="paymentType"]:checked').each(function() {
			ou_paymentType.push($(this).val());
		});
	}

	if ($('#onus_section input[name="mopType"]:checked').length < 1) {
		errormessage = errormessage + "Please choose at least one Transection Mop Type\n";
	} else {
		$('#onus_section input[name="mopType"]:checked').each(function() {
			ou_mopType.push($(this).val());
		});
	}

	if (($('#onus_section input[name="acquirer"]:checked').length < 1)) {
		errormessage = errormessage + "Please choose at least one Acquirer\n";
	} else {
		// var acq_pref_count = 1;
		$('#onus_section input[name="acquirer"]:checked').each(function() {
			ou_Acquirer.push("1-" + $(this).val());
			// acq_pref_count++;
		});
	}

	merchant = $('#onus_merchant').val();

	
	if($('#onus_section input[name="region"]:checked').length < 1) {
		errormessage = errormessage + "Plese choose at lease one Region\n";
	} else {
		$('#onus_section input[name="region"]:checked').each(function() {
			paymentsRegion.push($(this).val());
		});
	}

	if($('#onus_section input[name="typeCard"]:checked').length < 1) {
		errormessage = errormessage + "Plese choose at lease one Cardholder Type\n";
	} else {
		$('#onus_section input[name="typeCard"]:checked').each(function() {
			cardHolderType.push($(this).val());
		});
	}

	if($('#onus_section input[name="slabId"]:checked').length < 1) {
		errormessage = errormessage + "Plese choose at lease one Amount Slab\n";
	} else {
		$('#onus_section input[name="slabId"]:checked').each(function() {
			slabId.push($(this).val());
			let labelVal = $(this).closest(".wwgrp").find("label").text();
			slabValue.push(labelVal);
		});
	}

	if (errormessage != "") {
		swal({
			title : "",
			text : errormessage
		});
	} else {
		CurrentOffusListLength = OffusList.length;

		for (var ou_currency_key = 0; ou_currency_key < ou_currency.length; ou_currency_key++) {
			for (var ou_paymentType_key = 0; ou_paymentType_key < ou_paymentType.length; ou_paymentType_key++) {
				for (var ou_mopType_key = 0; ou_mopType_key < ou_mopType.length; ou_mopType_key++) {
					for (var ou_txnType_key = 0; ou_txnType_key < ou_txnType.length; ou_txnType_key++) {
						for (var ou_Acquirer_key = 0; ou_Acquirer_key < ou_Acquirer.length; ou_Acquirer_key++) {
							for(let cardHolderTypeKey = 0; cardHolderTypeKey < cardHolderType.length; cardHolderTypeKey++) {
								for(let paymentsRegionKey = 0; paymentsRegionKey < paymentsRegion.length; paymentsRegionKey++) {
									for(let slabIdKey = 0; slabIdKey < slabId.length; slabIdKey++) {

										let str = slabValue[slabIdKey];
										str = str.replace(/\s/g,'');
										str = str.split("-");
									
										minAmount = str[0];
										maxAmount = str[1];

										ou_row = new Offus(
											merchant,
											ou_currency[ou_currency_key],
											ou_paymentType[ou_paymentType_key],
											ou_mopType[ou_mopType_key],
											ou_txnType[ou_txnType_key],
											ou_Acquirer[ou_Acquirer_key],
											onUsFlag,
											cardHolderType[cardHolderTypeKey],
											paymentsRegion[paymentsRegionKey],
											slabId[slabIdKey],
											minAmount,
											maxAmount
										);
			
										match = false;
			
										for (var a = 0; a < CurrentOffusListLength; a++) {
											if ((ou_row.merchant === OffusList[a].merchant || ou_row.merchant != "ALL MERCHANTS"
													&& OffusList[a].merchant == "ALL MERCHANTS")
													&& (ou_row.currency === OffusList[a].currency)
													&& (ou_row.paymentType === OffusList[a].paymentType)
													&& (ou_row.mopType === OffusList[a].mopType)
													&& (ou_row.transactionType === OffusList[a].transactionType)
													&& (ou_row.acquirerMap === OffusList[a].acquirerMap)
													&& (ou_row.onUsFlag === OffusList[a].onUsFlag)
													&& (ou_row.cardHolderType === OffusList[a].cardHolderType)
													&& (ou_row.paymentsRegion === OffusList[a].paymentsRegion)
													&& (ou_row.slabId === OffusList[a].slabId)
													&& (ou_row.minAmount === OffusList[a].minAmount)
													&& (ou_row.maxAmount === OffusList[a].maxAmount)) {
												match = true;
												matchNumber++;
											}
										}
										if (match == false) {
											OffusListTemp.push(ou_row);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		$('#onus_section input:checked').removeAttr('checked');
	}

	if (matchNumber > 0) {
		swal({
			title : "",
			text : matchNumber + " Match found in records!",
			type : "info"
		});
	}

	if (OffusListTemp.length > 0) {
		var listData = {
			values : OffusListTemp
		};
		var token = document.getElementsByName("token")[0].value;
		var data1 = "";
		data1 = data1.concat("{", "\"", "listData", "\"", ":", JSON.stringify(OffusListTemp), ",\"", "token", "\":\"", token, "\"", "}");

		$.ajax({
			type : "POST",
			url : "onusoffusRulesSetup",
			timeout : 0,
			dataType : "json",
			contentType : "application/json; charset=utf-8",
			data : data1,
			success : function(data) {
				if (OffusListTemp.length > 0) {
					var response = data.response;
					swal({
						title : response,
						type : "success"
					}, function() {
						window.location.reload();
					});
				}
			},
			error : function(data) {
				window.location.reload();
			}
		});
	}

};

// ///////////////////////////////////////////
// ///////Get On Us Functions End Here////////
// ///////////////////////////////////////////

// function removeRow(index){
// $.ajax({
// type : "POST",
// url : "deleteRouterRule",
// data : {
// "id":index
// },
// success : function(data) {
// var response = data.response;
//				
// alert('Rule Deleted!');
// },
// error : function(data) {
// window.location.reload();
// }
// });
// $(this).parent().parent().remove();
// }

$(document).ready(function() {
	$('.card-list-toggle').on('click', function() {
		$(this).toggleClass('active');
		$(this).next('.card-list').slideToggle();
	});

	$("#selectMerchant").on('change', function() {
		document.getElementById("loading").style.display = "block";
		// $("#onUs_default").hide();
		$('.offus_table').empty();
		$('.onus_table').empty();
		var merchantVal = document.getElementById("selectMerchant").value;
		if (merchantVal == ""
			|| merchantVal == "Select Merchant"
			|| merchantVal == null) {
			alert("Please Select Merchant");
			document.getElementById("loading").style.display = "none";
			return false;
		}

		$.ajax({
			type : "POST",
			url : "getRulesList",
			timeout : 0,
			data : {
				"payId" : merchantVal,
				"struts.token.name" : "token",
			},
			success : function(data) {
				document.getElementById("errorOfNoRule").style.display = "none";
				document.getElementById("loading").style.display = "none";
				document.getElementById("onUs_default").classList.remove('active');

				OffusList = data.routerRules;
								
				if (OffusList.length > 0) {
					for (var i = 0; i < OffusList.length; i++) {
						if (OffusList[i].onUsFlag == false) {
							$('.offus_table').show();
							$('.offus_table').append(
								'<tr class="boxtext"><td align="left" valign="middle">'
								+ OffusList[i].merchant
								+ '</td><td align="left" valign="middle">'
								+ OffusList[i].currency
								+ '</td><td align="left" valign="middle">'
								+ OffusList[i].paymentType
								+ '</td><td align="left" valign="middle">'
								+ OffusList[i].mopType
								+ '</td><td align="left" valign="middle">'
								+ OffusList[i].transactionType
								+ '</td><td  align="left" valign="middle">'
								+ OffusList[i].paymentsRegion
								+ '</td><td align="left" valign="middle"> '
								+ OffusList[i].cardHolderType
								+ '</td><td align="left">'+ OffusList[i].minAmount +' - '+ OffusList[i].maxAmount +'</td><td align="left" valign="middle">'+ OffusList[i].acquirerMap + '<div class="AcquirerListTemp_'+ OffusList[i].id
								+ '"></div></td><td align="left" valign="top">'
								+ '<button type="button" class="btn btn-info edit-row" data-id="'
								+ OffusList[i].id
								+ '">&times; Edit</button><button type="button" class="btn btn-danger remove-row" data-id="'
								+ OffusList[i].id
								+ '">&times; Remove</button></td></tr>');
						} else {
							document.getElementById("loading").style.display = "none";

							$('.onus_table').show();
							$('.onus_table').append(
								'<tr class="boxtext"><td align="left" valign="middle">'
								+ OffusList[i].merchant
								+ '</td><td align="left" valign="middle">'
								+ OffusList[i].currency
								+ '</td><td align="left" valign="middle">'
								+ OffusList[i].paymentType
								+ '</td><td align="left" valign="middle">'
								+ OffusList[i].mopType
								+ '</td><td align="left" valign="middle">'
								+ OffusList[i].transactionType
								+ '</td><td align="left" valign="top">'
								+ OffusList[i].paymentsRegion
								+ '</td><td align="left" valign="top">'
								+ OffusList[i].cardHolderType
								+ '</td><td align="left" valign="middle">'
								+ OffusList[i].acquirerMap
								+ '</td><td align="left">'+ OffusList[i].minAmount +' - '+ OffusList[i].maxAmount +'</td><td align="left" valign="top">'
								+ '<button type="button" class="btn btn-info edit-row" data-id="'
								+ OffusList[i].id
								+ '">&times; Edit</button><button type="button" class="btn btn-danger remove-row" data-id="'
								+ OffusList[i].id
								+ '">&times; Remove</button></td></tr>');
						}
					}
				} else {
					document.getElementById("errorOfNoRule").style.display = "block";
				}
			},
			error : function(data) {
				window.location.reload();
			}
		});
	});

	$('.product-spec').on('click', '.remove-row', function(events) {
		var index = $(this).attr('data-id');
		var token = document.getElementsByName("token")[0].value;
		swal(
			{
				title : "Are you sure want to delete this Rule?",
				type : "warning",
				showCancelButton : true,
				confirmButtonColor : "#DD6B55",
				confirmButtonText : "Yes, delete it!",
				closeOnConfirm : false
			},
			function(isConfirm) {
				if (!isConfirm)
					return;
				$.ajax({
					type : "POST",
					url : "deleteRouterRule",
					timeout : 0,
					data : {
						"id" : index,
						"token" : token,
						"struts.token.name" : "token",
					},
					success : function(data) {
						var response = data.response;
						swal(
							{
								title : 'Rule Deleted!',
								type : "success"
							},
							function() {
								$(this).closest('tr').remove();
								window.location.reload();
							}
						);
					},
					error : function(data) {
						window.location.reload();
					}
				});
			}
		);
		// $(this).closest('tr').remove();
	});

	var acquirerCount = 1;
	var checkCount = 0;
	var checkedAcquirer = [];
	var acquirerCopy = $('.AcquirerList').html();
	var acquirerClone;
	var acquirerRemoveBtn = '<button type="button" class="btn btn-danger acquirerRemoveBtn">Remove</button>';
	var cloneIndex = $(".AcquirerList > [class^='Acquirer']").length;
	var AQflag = false;

	$('.AcquirerList + .acquirerCloneBtn').live('click', function() {
		checkedAcquirer.push($('.AcquirerList input[name="Acquirer' + cloneIndex + '"]:checked').val());
		cloneIndex++;
		acquirerClone = acquirerCopy.replace(/1/g, cloneIndex);
		$('.AcquirerList').append(acquirerClone);
		for (var k = 0; k < checkedAcquirer.length; k++) {
			$(".AcquirerList .Acquirer" + cloneIndex + ' input[type="radio"]').each(function() {
				if ($(this).val() == checkedAcquirer[k]) {
					$(this).attr("disabled", "true");
				}
			});
		}

		if (cloneIndex == 2) {
			$(acquirerRemoveBtn).insertAfter('.AcquirerList + .acquirerCloneBtn');
		}
		
		$('.AcquirerList + .acquirerCloneBtn').hide();

		$('.AcquirerList input[name="Acquirer' + cloneIndex + '"]').live('click', function() {
			if ($(this).attr('name') === 'Acquirer' + cloneIndex) {
				$('.AcquirerList + .acquirerCloneBtn').show();
			}
		});
	});

	$('.AcquirerList input[type="radio"]').live('click', function() {
		var indexElem = parseInt($(this).attr('name').replace("Acquirer", "")) - 1;
		if (checkedAcquirer[indexElem] !== undefined) {
			checkedAcquirer[indexElem] = $(this).val();

			while (indexElem < cloneIndex) {
				$(".AcquirerList .Acquirer" + (indexElem + 1) + ' input[type="radio"]').removeAttr("disabled");
				for (var k = 0; k < indexElem; k++) {
					$(".AcquirerList .Acquirer" + (indexElem + 1) + ' input[type="radio"]').each(function() {
						if ($(this).val() == checkedAcquirer[k]) {
							$(this).removeAttr("checked");
							$(this).attr("disabled", "true");
						}
					});
				}
				indexElem++
			}
		}
	});

	$('.AcquirerList ~ .acquirerRemoveBtn').live('click', function() {
		checkedAcquirer.pop();
		if (cloneIndex == 2) {
			$(this).remove();
		}
		if (cloneIndex > 1) {
			$(".AcquirerList .Acquirer" + cloneIndex).remove();
			$('.AcquirerList + .acquirerCloneBtn').show();
			cloneIndex--;
		}
	});

	$('.AcquirerList input[name="Acquirer1"]').live('click', function() {
		$('.AcquirerList + .acquirerCloneBtn').show();
	});

	$('#offus_reset').on('click', function() {
		$('.offus_table').removeClass('disabled');
		$('.offusFormTable input').removeAttr('checked');
		$('#offus_submit').addClass('disabled');
		$('#offus_reset').addClass('disabled');
	});

	$('#onus_reset').on('click', function() {
		$('.onus_table').removeClass('disabled');
		$('.onusFormTable input').removeAttr('checked');
		$('#onus_submit').addClass('disabled');
		$('#onus_reset').addClass('disabled');
	});

	$('#onus_section input').click(function() {
		if ($('#onus_section input:checked').length > 0) {
			$('.onus_table').addClass('disabled');
			$('#onus_reset').removeClass('disabled');
			if (($('#onus_section input[name="currency"]').is(':checked'))
				&& ($('#onus_section input[name="txnType"]').is(':checked'))
				&& ($('#onus_section input[name="paymentType"]').is(':checked'))
				&& ($('#onus_section input[name="mopType"]').is(':checked'))
				&& ($('#onus_section input[name="acquirer"]').is(':checked'))) {
				$('#onus_submit').removeClass('disabled');
			} else {
				$('#onus_submit').addClass('disabled');
			}
		} else {
			$('.onus_table').removeClass('disabled');
			$('#onus_reset').addClass('disabled');
		}
	});

	$('#offus_section input').click(function() {
		if ($('#offus_section input:checked').length > 0) {
			$('.offus_table').addClass('disabled');
			$('#offus_reset').removeClass('disabled');
			if (($('#offus_section input[name="currency"]').is(':checked'))
				&& ($('#offus_section input[name="txnType"]').is(':checked'))
				&& ($('#offus_section input[name="paymentType"]').is(':checked'))
				&& ($('#offus_section input[name="mopType"]').is(':checked'))
				&& ($('.AcquirerList input[name^="Acquirer"]').is(':checked'))) {
				$('#offus_submit').removeClass('disabled');
			} else {
				$('#offus_submit').addClass('disabled');
			}
		} else {
			$('.offus_table').removeClass('disabled');
			$('#offus_reset').addClass('disabled');
		}
	});

	// Offus Edit Funtion Start Here
	var TempCloneIndex = 0;

	$('.product-spec').on('click', '.edit-row', function(events) {
		var index = $(this).attr('data-id');
		var tempAcquirerMap;
		var tempData;
		$('.offusFormTable').hide();
		for (var i = 0; i < OffusList.length; i++) {
			if (OffusList[i].id == index) {
				tempData = OffusList[i];
				tempAcquirerMap = tempData.acquirerMap.split(", ");
				$('<button type="button" class="btn btn-primary acquirerCloneBtn">Add</button>').insertAfter(".AcquirerListTemp_" + index);
				
				if (tempAcquirerMap.length > 2) {
					$(acquirerRemoveBtn).insertAfter('.AcquirerListTemp_' + index + ' + .acquirerCloneBtn');
				}
				for (var j = 0; j < tempAcquirerMap.length; j++) {
					TempCloneIndex++;
					acquirerClone = acquirerCopy.replace(/1/g, TempCloneIndex);
					$('.AcquirerListTemp_' + index).append(acquirerClone);
					$(".AcquirerListTemp_" + index + " .Acquirer" + TempCloneIndex + ' input[type="radio"]').each(function() {
						if ($(this).val() == tempAcquirerMap[j]) {
							$(this).attr("checked", "true");
						}
						for (var k = 0; k < j; k++) {
							if ($(this).val() == tempAcquirerMap[k]) {
								$(this).attr("disabled", "true");
							}
						}
					});
					
					$('.AcquirerListTemp_'+ index + ' input[name="Acquirer' + TempCloneIndex + '"]').live('click', function() {
						if ($(this).attr('name') === '.AcquirerListTemp_' + index + ' Acquirer' + TempCloneIndex) {
							$('.AcquirerListTemp_' + index + ' + .acquirerCloneBtn').show();
						}
					});
				}
			}
		}

		$('.AcquirerListTemp_' + index + ' input[type="radio"]').live('click', function() {
			var indexElem = parseInt($(this).attr('name').replace("Acquirer", "")) - 1;
			if (tempAcquirerMap[indexElem] !== undefined) {
				tempAcquirerMap[indexElem] = $(this).val();
				while (indexElem < TempCloneIndex) {
					$(".AcquirerListTemp_" + index + " .Acquirer" + (indexElem + 1) + ' input[type="radio"]').removeAttr("disabled");
					for (var k = 0; k < indexElem; k++) {
						$(".AcquirerListTemp_" + index + " .Acquirer" + (indexElem + 1) + ' input[type="radio"]').each(function() {
							if ($(this).val() == tempAcquirerMap[k]) {
								$(this).removeAttr("checked");
								$(this).attr("disabled", "true");
							}
						});
					}
					indexElem++
				}
			}
		});

		$('.AcquirerListTemp_' + index + ' + ~ .acquirerRemoveBtn').live('click', function() {
			tempAcquirerMap.pop();
			if (TempCloneIndex == 2) {
				$(this).remove();
			}
			if (TempCloneIndex > 1) {
				$(".AcquirerListTemp_" + index + " .Acquirer" + TempCloneIndex).remove();
				$('.AcquirerListTemp_' + index + '  + .acquirerCloneBtn').show();
				TempCloneIndex--;
			}
		});

		$('.AcquirerListTemp_' + index).parent().append('<button type="button" class="btn btn-primary acquirerCloneBtn" style="display:none;">Add</button>');

		$('.AcquirerListTemp_' + index + ' + .acquirerCloneBtn').live('click', function() {
			tempAcquirerMap.push($('.AcquirerListTemp_' + index + ' input[name="Acquirer' + TempCloneIndex + '"]:checked').val());
			TempCloneIndex++;
			acquirerClone = acquirerCopy.replace(/1/g, TempCloneIndex);
			$('.AcquirerListTemp_' + index).append(acquirerClone);
			for (var k = 0; k < tempAcquirerMap.length; k++) {
				$(".AcquirerListTemp_" + index + " .Acquirer" + TempCloneIndex + ' input[type="radio"]').each(function() {
					if ($(this).val() == tempAcquirerMap[k]) {
						$(this).attr("disabled", "true");
					}
				});
			}

			if (TempCloneIndex == 2) {
				$(acquirerRemoveBtn).insertAfter('.AcquirerListTemp_' + index + ' + .acquirerCloneBtn');
			}
			
			$('.AcquirerListTemp_' + index + ' + .acquirerCloneBtn').hide();

			$('.AcquirerListTemp_' + index + ' input[name="Acquirer' + TempCloneIndex + '"]').live('click', function() {
				if ($(this).attr('name') === 'Acquirer' + TempCloneIndex) {
					$('.AcquirerListTemp_' + index + ' + .acquirerCloneBtn').show();
				}
			});
		});

		$(this).attr('class', 'btn btn-info update-row').html('<i class="fa fa-check"></i> Update');
		$(this).next('.remove-row').attr('class', 'btn btn-warning cancel-row').html('<i class="fa fa-times"></i> Cancel');

		$('.edit-row').hide();

		$('.product-spec').on('click', '.update-row', function(events) {
			tempAcquirerMap = [];
			var tempValues = [];

			if ($('.AcquirerListTemp_' + index + ' input[name^="Acquirer"]:checked').length < TempCloneIndex) {
				alert('Please Choose Acquirer preferences or remove unused Preferences!');
				// swal({
				// title:"",
				// text: 'Please
				// Choose
				// Acquirer
				// preferences
				// or remove
				// unused
				// Preferences!'
				// });
			} else {
				var acq_pref_count = 1;
				$('.AcquirerListTemp_' + index + ' input[name^="Acquirer"]:checked').each(function() {
					tempAcquirerMap.push(acq_pref_count + "-" + $(this).val());
					// alert(tempAcquirerMap);
					acq_pref_count++;
				});

				tempData.acquirerMap = tempAcquirerMap.toString();
				tempValues.push(tempData);
				var listData = {
					values : tempValues
				};
				var data1 = "";
				var token = document.getElementsByName("token")[0].value;
				data1 = data1.concat("{", "\"", "listData", "\"", ":", JSON.stringify(tempValues), ",\"", "token", "\":\"", token, "\"", "}");

				$.ajax({
					type : "POST",
					url : "editRouterRule",
					timeout : 0,
					dataType : "json",
					contentType : "application/json; charset=utf-8",
					data : data1,
					success : function(data) {
						var response = data.response;
						swal(
							{
								title : 'Rule Updated!',
								type : "success"
							},
							function() {
								window.location.reload();
							}
						);
					},
					error : function(data) {
						window.location.reload();
					}
				});
			}								
		});

		$('.product-spec').on('click', '.cancel-row', function(events) {
			window.location.reload();
		});
	});
	// Offus Edit Funtion End Here

	$(".paymentType input[type='checkbox']").on("click", function(e) {
		let _this = $(this);
		let _val = _this.val();
		let parent = _this.closest(".paymentType");

		let isCreditCard		=	parent.find("input[value='CC']").is(":checked");
		let isDebitCard			=	parent.find("input[value='DC']").is(":checked");
		let isNetBanking		=	parent.find("input[value='NB']").is(":checked");
		let isWallet			=	parent.find("input[value='WL']").is(":checked");
		let isDebitCardWithPin	=	parent.find("input[value='DP']").is(":checked");
		let isUPI				=	parent.find("input[value='UP']").is(":checked");
		let isAutoDebit			=	parent.find("input[value='AD']").is(":checked");
		let isPrepaidCard		=	parent.find("input[value='PC']").is(":checked");

		if(isNetBanking && _val !== "NB") {
			e.preventDefault();
			$(this).removeAttr("checked");
		} else if(isWallet && _val !== "WL") {
			e.preventDefault();
			$(this).removeAttr("checked");
		} else if(isUPI && _val !== "UP") {
			e.preventDefault();
			$(this).removeAttr("checked");
		} else if(isAutoDebit && _val !== "AD") {
			e.preventDefault();
			$(this).removeAttr("checked");
		}

		if(isCreditCard || isDebitCard || isDebitCardWithPin || isPrepaidCard) {
			if(isNetBanking || isWallet || isUPI || isAutoDebit) {
				$(this).removeAttr("checked");
			}
		}
	});

	$(".paymentType .checkbox").each(function(e) {
		let dataType = $(this).attr("data-type");
		if(dataType == "AD" || dataType == "DP" || dataType == "PC") {
			$(this).css("display", "none");
		}
	});

});
