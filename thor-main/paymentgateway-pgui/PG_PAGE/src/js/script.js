import { multilingualText } from "./multilingualText";

let setDefaultOption = function(reffObj) {
	let _ele = reffObj.element;
	let _elementId = reffObj.id;
	
	_ele.remove(0);

	let _option = document.createElement("option");
	_option.text = reffObj.optionText;
	_option.value = '';
	_ele.add(_option, _ele[0]);

	if(_elementId === "emiPaymentType") {
		if(window.id("emiBankName").classList.contains("d-none")) {
			_ele.selectedIndex = "0";
		}
	} else if(_elementId === "emiBankName") {
		if(window.id("emi-detail").classList.contains("d-none")) {
			_ele.selectedIndex = "0";
		}
	} else if(_elementId === "bankList") {
		_ele.selectedIndex = "0";
	}
}

export function multilingual(e) {
	let lang = window.id("translate").value,
		langElement = document.getElementsByClassName("lang"),
		selectBank = document.getElementsByClassName("selectBank");

	document.getElementsByTagName("body")[0].setAttribute("id", "lang--" + lang);

	for(let i = 0; i < langElement.length; i++) {
		let dataKey = langElement[i].getAttribute("data-key");        

		langElement[i].innerHTML = multilingualText(lang, dataKey);
	}

	for(let j = 0; j < selectBank.length; j++) {
		let _dataKey = selectBank[j].getAttribute("data-key"),
			elementId = selectBank[j].getAttribute("id");

		setDefaultOption({
			optionText: multilingualText(lang, _dataKey),
			element: selectBank[j],
			id: elementId
		});
	}
}

export function isValidVpaOnFocusOut() {
	let vpaRegex = /[A-Za-z0-9][A-Za-z0-9.-]*@[A-Za-z]{2,}$/;
	let vpaElement = window.id("vpaCheck");
	let vpaValue = (vpaElement.value).trim();

	if (!vpaValue) {
		window.id('enterVpa').style.display = "block";
		window.id('red1').style.display = "none";
		vpaElement.classList.add("redLine");
		return false;
	} else if (!vpaValue.match(vpaRegex)) {
		vpaElement.classList.add("redLine");
		window.id('red1').style.display = "block";
		window.id('enterVpa').style.display = "none";
		return false;
	}
	return true;
}

export function stopWorker() {
	if(window.oid !== "" && window.worker !== undefined) {
		window.worker.terminate();
		window.worker = undefined;
	}
}

export const startWorker = obj => {
	if(window.Worker) {
		window.worker = new Worker(`${window.basePath}/js/WebWorker.min.js`);

		window.worker.postMessage({
			value: obj.requestType === "UPI" ? obj.pgRefNum : obj.oid,
			requestType: obj.requestType
		});

		window.worker.onmessage = function(e) {
			if(e.data === "cancel") {
				window.id("cancel-form").submit();
			} else {
				window.id("resPgRefNum").value = e.data.PG_REF_NUM;
				window.id("resReturnUrl").value = e.data.RETURN_URL;
				window.id("upiResponseSubmitForm").submit();
			}
		};
	}
}

export function inputHandler(e) {
	e.preventDefault();
}

export function isNumberKey(evt) {
	let elementValue = evt.target.value;

	if (!(/^[0-9]+$/.test(elementValue))) {
		evt.target.value = elementValue.replace(/[^0-9]/g, "");
	}
}

export function numOnly(event) {
	let key = event.keyCode, leftKey = 37, rightKey = 39, deleteKey = 46, backspaceKey = 8, tabKey = 9, maxlengthCheck = Number(event.target.getAttribute('maxlength'));

	if (event.key === "!" || event.key === "@" || event.key === "#" || event.key === "$" || event.key === "%" || event.key === "^" || event.key === "&" || event.key === "*" || event.key === "(" || event.key === ")") {
		return false;
	}

	if (maxlengthCheck) {
		if (event.target.value.length === maxlengthCheck) {
			if (key === backspaceKey || key === tabKey || key === leftKey || key === rightKey || key === deleteKey) {
				return true;
			} else {
				return false;
			}
		}
	}

	return ((key >= 48 && key <= 57) || (key >= 96 && key <= 105) || key === backspaceKey || key === tabKey || key === leftKey || key === rightKey || key === deleteKey);
}

export function numberEnterPhone(evt) {
	if (window.matchMedia("(max-width: 680px)")) {
		let elementValue = evt.target.value;
		if (!(/^[0-9]+$/.test(elementValue))) {
			evt.target.value = elementValue.replace(/[^0-9]/g, "");
		}
	}
}

export function toolTipCvvHandler(e, val) {
	let _screenSize = window.innerWidth;
	if(_screenSize >= 768) {
		e.target.closest(".cvv-info").querySelector(".whatIsCvv").style.display = val;
	}
}

// export function _createPaymentTypeList(emiCC, emiDC) {
// 	window.id("emiPaymentType").innerHTML = "";
// 	if(emiCC) {
// 		_addSelectOption({text: multilingualText[window.id("translate").value]["creditCard"], value: "EMICC", element: "emiPaymentType"});
// 	}

// 	if(emiDC) {
// 		_addSelectOption({text: multilingualText[window.id("translate").value]["debitCard"], value: "EMIDC", element: "emiPaymentType"});
// 	}
// }

export function addConvenienceFee(paymentType) {
	if(paymentType !== undefined && paymentType !== null && paymentType !== "") {
		Object.keys(window.surchargeMopType).map(element => {
			if(element === paymentType && element === "CC") {
				if(window.id("mopTypeCCDiv2") !== null && window.id("mopTypeCCDiv2").value !== "") {
					if(window.id("mopTypeCCDiv2").value === "AX") {
						setFee({
							showGST: true,
							surchargeAmt: window.pageInfoObj.surcharge_cc_amex
						});
					} else {
						setFee({
							showGST: true,
							surchargeAmt: window.pageInfoObj[window.surchargeMopType[[paymentType]][[window.id("cardHolderTypeId").value]]]
						});
					}
				} else {
					setFee({
						showGST: true,
						surchargeAmt: window.pageInfoObj.surcharge_cc_consumer
					});
				}
			} else if(element === paymentType && element === "DC") {
				if(window.id("mopTypeCCDiv2") !== null && window.id("mopTypeCCDiv2").value !== "") {
					setFee({
						showGST: false,
						surchargeAmt: window.pageInfoObj[window.surchargeMopType[[paymentType]][[window.id("mopTypeCCDiv2").value]]]
					});
				} else {
					setFee({
						showGST: false,
						surchargeAmt: window.pageInfoObj.surcharge_dc_visa
					});
				}
			} else if(element === paymentType) {
				setFee({
					showGST: false,
					surchargeAmt: window.pageInfoObj[window.surchargeMopType[paymentType]]
				});
			}
		});
	} else {
		setFee({
			showGST: false,
			surchargeAmt: null
		});
	}
}

export function setFee(obj) {
	let isSurcharge = window.pageInfoObj["ENABLE_SURCHARGE"],
		amount = Number(window.pageInfoObj["AMOUNT"]) / 100,
		surchargeAmt = Number(obj.surchargeAmt),
		TOTAL_AMT = (amount + surchargeAmt).toFixed(2),
		gstBlock = window.id("gst-block"),
		tdrBlock = window.id("tdrBLock_head");

		if(window.id("new_head") !== null) {
			window.id("new_head").querySelector(".value-block").innerHTML = TOTAL_AMT;
		}
		
		if(window.id("pay-now") !== null) {
			window.id("pay-now").querySelector(".value-block").innerHTML = TOTAL_AMT;	
		}

	if(isSurcharge === "Y" && obj.showGST && amount > 500) {
		let GST = surchargeAmt - (surchargeAmt * (100/(118))),
			SURCHARGE = surchargeAmt - window.GST;
		
		SURCHARGE = SURCHARGE.toFixed(2);
		GST = GST.toFixed(2);

		gstBlock.classList.remove("d-none");
		gstBlock.classList.add("d-flex");
		gstBlock.querySelector(".value-block").innerHTML = GST;

		tdrBlock.querySelector(".value-block").innerHTML = SURCHARGE;

		adjustOrderSummaryWidth();
	} else {
		let SURCHARGE = surchargeAmt.toFixed(2);

		tdrBlock.querySelector(".value-block").innerHTML = SURCHARGE;

		gstBlock.classList.remove("d-flex");
		gstBlock.classList.add("d-none");
	}
}

export function adjustOrderSummaryWidth() {
	let orderSummaryList = window.id("order-summary").querySelectorAll("li");
	orderSummaryList.forEach(function(that) {
		let elementId = that.getAttribute("id");

		if(elementId !== "customerName") {
			let spanWidth = that.querySelector(".summary-label-text").getBoundingClientRect().width;
			that.querySelector(".summary-label").style.width = "calc(100% - "+ spanWidth +"px)";
		}
	});
}

// // SET TAX DECLARATION
// export function setTaxDeclaration(paymentType) {
// 	if(paymentType === "CC" || paymentType === "DC" || paymentType === "IN" || paymentType === "EM") {
// 		if(window.id("common-tax-declaration") !== null) {
// 			window.id("common-tax-declaration").innerHTML = window.taxDeclarationObj[paymentType];
// 		}
// 	} else if(paymentType === "SC") {
//         let saveCardPaymentType = window.querySelector(".saveCardDetails.active .payment-type").value;
//         window.id("charge-info").innerHTML = window.taxDeclarationObj[saveCardPaymentType];
//     } else {
// 		// window.id(paymentType + "-tax-declaration").innerHTML = window.taxDeclarationObj[paymentType];
// 	}
// }

export function upiQRAction(paymentType) {
	window.id("loading").style.display = "block";

	let token = document.getElementsByName("customToken")[0].value,
		data = new FormData();

	data.append('token', token);

	let xhr = new XMLHttpRequest();
	xhr.open('POST', 'upiQrPay', true);

	xhr.onload = function() {
		let obj = JSON.parse(this.response);

		if(null !== obj) {
			window.oid = obj.oid;
		}

		if(obj.responseCode === "000" && obj.responseMessage === "SUCCESS") {
			if(obj.upiQrCode !== undefined) {
				window.id("upi-qr-img").innerHTML = '<img src="data:image/png;base64,'+ obj.upiQrCode +'" class="img-fluid">';

				startWorker({
					oid: window.oid,
					requestType: "UPI_QR"
				});
			}
		} else {
			alert("Something went wrong!");
		}

		setTimeout(function() {
			window.id("loading").style.display = "none";
		}, 1000);
	};

	xhr.send(data);
}

export function editWalletNumber(action, e) {
	if(e !== undefined) {
		e.preventDefault();
	}

	if(action === "reset") {
		window.querySelector("body").classList.remove("mobikwik--active");			
		window.id("mobikwik-section").classList.add("d-none");
	}
	
	window.id("btn-edit-wallet").classList.add("d-none");
	window.id("wallet-otp-box").classList.add("d-none");
	window.id("wallet-otp").value = "";

	if(window.id("wallet-logged-in") !== null) {
		window.id("wallet-logged-in").classList.add("d-none");
	}
	if(window.id("wallet-logged-mobile") !== null) {
		window.id("wallet-logged-mobile").innerHTML = "";
	}
	window.id("available-balance").classList.add("d-none");
	window.id("insufficient-fund").classList.add("d-none");
	window.id("mobikwik-amount").innerHTML = "";

	window.id("wallet-mobile-box").classList.remove("d-none");
	window.id("wallet-mobile-number").value = "";
	window.id("phoneNo").value = "";
	window.id("wallet-mobile-number").disabled = false;
	window.id("wallet-mobile-number").focus();

	window.id("error-wallet-number").innerHTML = "";
	window.id("error-wallet-number").classList.add("d-none");

	window.id("btn-resend-otp").classList.add("d-none");
	window.id("otp-timer").classList.add("d-none");
	window.id("otp-timer").querySelector("span").innerHTML = "";
	clearInterval(window.otpInterval);
	// window.loginpin.reset();

	window.id("pay-now").classList.add("btn-disabled");
}

export function addCustomScroll() {
	let tabInnerBox = window.querySelectorAll(".tabbox-inner");

	for(let i = 0; i < tabInnerBox.length; i++) {
		if(tabInnerBox[i].offsetHeight > 410) {
			tabInnerBox[i].classList.add("custom-scroll");
		} else {
			tabInnerBox[i].classList.remove("custom-scroll");
		}
	}
}

export function changeSubmitPosition() {
	let _screenSize = window.innerWidth,
		btns = window.querySelectorAll(".submit-btns-tab");

	if(_screenSize <= 991) {
		if(window.id("submit-btns-desktop").innerHTML !== "") {
			window.globalBtnData = window.id("submit-btns-desktop").innerHTML;
		}

		window.id("submit-btns-desktop").innerHTML = "";
	
		for(let i = 0; i < btns.length; i++) {
			btns[i].innerHTML = "";
		}

		let getActiveTab = window.querySelector(".tabLi.active").getAttribute("data-id");
		
		window.id("submit-btns-" + getActiveTab).innerHTML = window.globalBtnData;
	} else {
		if(window.id("submit-btns-desktop").innerHTML === "") {
			window.id("submit-btns-desktop").innerHTML = window.globalBtnData;
		}

		for(let j = 0; j < btns.length; j++) {
			btns[j].innerHTML = "";
		}
	}

	if(window.querySelector(".payBtnAmount") !== null) {
		window.querySelector(".payBtnAmount").innerHTML = window.id("totalAmount").innerHTML;
	}
}

export function wrapperPosition() {
    let _screenHeight = window.innerHeight,
        _container = window.id("container-outer-wrap"),
        _containerHeight = _container.offsetHeight;

    if(_screenHeight > _containerHeight) {
		let $margin = (_screenHeight - _containerHeight) / 2;
		
		_container.removeAttribute("style");

		_container.style.marginTop =  $margin + "px";
		_container.style.marginBottom = $margin + "px";

		_container.classList.remove("my-30");
    } else {
        _container.classList.add("my-30");
		_container.removeAttribute("style");
    }
}

// ADD SELECT OPTION
export function _addSelectOption(reffObj) {
	let dataKey = window.id(reffObj.element).getAttribute("data-key");
	let setFirstOption = function() {
		let option = document.createElement("option");
		option.text = multilingualText[window.id("translate").value][dataKey];
		option.value = '';
		window.id(reffObj.element).add(option, window.id(reffObj.element)[0]);
	}
	let _selectText = window.id(reffObj.element).options[0];
	if(_selectText === undefined) {
		setFirstOption();
	} else if(_selectText.text !== multilingualText[window.id("translate").value][dataKey]) {
		setFirstOption();
	}

	let opt = document.createElement("option");
	opt.appendChild(document.createTextNode(reffObj.text));
	opt.value = reffObj.value;
	window.id(reffObj.element).appendChild(opt);
}

// REMOVE ACTIVE CLASS FROM SAVED VPA
export function removeActiveFromSavedVpa() {
	let vpaLi = document.getElementsByClassName("saveVpaDetails");

	for(let i = 0; i < vpaLi.length; i++) {		
		if(vpaLi[i].classList.contains("active")) {
			vpaLi[i].classList.remove("active");
			vpaLi[i].querySelector(".custom-control-input").checked = false;			
			window.id("pay-now").classList.add("btn-disabled");
		}
	}

	// window.id("error-blank-vpa").classList.remove("d-block");
}

export function changeCardInfo(paymentType) {
	if(paymentType === "CC") {
		window.id("charge-info").innerHTML = window.taxDeclarationObj[paymentType];
	} else if(paymentType === "DC") {
		window.id("charge-info").innerHTML = window.taxDeclarationObj[paymentType];
	}
}

// export function selectOptionByValue(elem, val) {
// 	let opt, i = 0;
// 	while (opt === elem.options[i++]) {
// 		if (opt.value === val) {
// 			opt.selected = true;
// 		}
// 	}
// }

// export function loaderAction(action) {
//     if(action === "show") {
// 		window.id("loading2").style.display = "block";
//     } else if(action === "hide") {
// 		window.id("loading2").style.display = "none";
//     }
// }

// export const ajaxRequest = reffObj => {
// 	let data = new FormData();

// 	for(let key in reffObj.payload) {
// 		data.append(key, reffObj.payload[key]);
// 	}

// 	let xhr = new XMLHttpRequest();
// 	xhr.open("POST", reffObj.actionName, true);

// 	xhr.onload = function() {
// 		if (xhr.status === 200) {
// 			let obj = JSON.parse(this.response);

// 			reffObj.success(obj);
// 		} else {
// 			alert("Try again, Something went wrong!");
// 		}
// 	};

// 	xhr.send(data);
// }