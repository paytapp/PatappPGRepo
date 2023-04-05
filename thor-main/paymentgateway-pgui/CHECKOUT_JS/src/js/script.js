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
		if(Window.id("emiBankName").classList.contains("d-none")) {
			_ele.selectedIndex = "0";
		}
	} else if(_elementId === "emiBankName") {
		if(Window.id("emi-detail").classList.contains("d-none")) {
			_ele.selectedIndex = "0";
		}
	} else if(_elementId === "bankList") {
		_ele.selectedIndex = "0";
	}
}

export function multilingual(e) {
	let lang = e.target.value,
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

	// if(Window.pageInfoObj.emiDC || Window.pageInfoObj.emiCC) {
	//     _createPaymentTypeList(Window.pageInfoObj.emiCC, Window.pageInfoObj.emiDC);
	// }
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

export function submitUPIResponseForm(myMap) {
	Window.id('approvedNotification').style.display = "none";
	Window.id("loading").style.display = "none";

	let form = Window.id("upiResponseForm");
	form.innerHTML = "";
	form.action = myMap.RETURN_URL;

	if (myMap.encdata) {
		form.innerHTML += ('<input type="hidden" name="encdata" value="' + myMap.encdata + '">');
	} else {
		for (let key in myMap) {
			if(key !== "RETRY_FLAG") {
				form.innerHTML += ('<input type="hidden" name="' + key + '" value="' + myMap[key] + '">');
			}
		}
	}

	if(myMap["RETRY_FLAG"] === undefined) {
		Window.id("upiResponseForm").submit();
	} else if(myMap["RETRY_FLAG"] === "Y") {
		Window.querySelector(".retry-popup").classList.remove("d-none");
		Window.id("returnMerchantAll").classList.add("d-none");
		Window.id("returnMerchantUPI").classList.remove("d-none");
	}	
}

export function isValidVpaOnFocusOut() {
	let vpaRegex = /[A-Za-z0-9][A-Za-z0-9.-]*@[A-Za-z]{2,}$/;
	let vpaElement = Window.id("vpaCheck");
	let vpaValue = (vpaElement.value).trim();

	if (!vpaValue) {
		Window.id('enterVpa').style.display = "block";
		Window.id('red1').style.display = "none";
		vpaElement.classList.add("redLine");
		return false;
	} else if (!vpaValue.match(vpaRegex)) {
		vpaElement.classList.add("redLine");
		Window.id('red1').style.display = "block";
		Window.id('enterVpa').style.display = "none";
		return false;
	}
	return true;
}



export function myCancelAction(e) {
	e.preventDefault();
	// self.location = "txncancel";
}

export function openWebSeal(e) {
    // Window.open(this.href,
    //     'Panacea Certificate',
    //     'height=500,width=650,scrollbar=yes,status=no,menubar=no,toolbar=no,resizable');

    return false;
}

export function stopWorker() {
	if(Window.oid !== "" && Window.worker !== undefined) {
		Window.worker.terminate();
		Window.worker = undefined;
	}
}

export const startWorker = obj => {
	if(window.Worker) {
		Window.worker = new Worker(`${Window.basePath}/js/WebWorker.min.js`);

		Window.worker.postMessage({
			pgRefNum: obj.requestType === "UPI" ? obj.pgRefNum : obj.oid,
			requestType: obj.requestType,
			encSessionData: Window.pageInfoObj.encSessionData,
			CHECKOUT_JS_FLAG: Window.pageInfoObj.checkOutJsFlag
		});

		Window.worker.onmessage = function(e) {
			if(e.data === "cancel") {
				Window.id("cancel-form").submit();
			} else {
				Window.id("resPgRefNum").value = e.data.PG_REF_NUM;
				Window.id("resReturnUrl").value = e.data.RETURN_URL;
				Window.id("upiResponseSubmitForm").submit();
			}
		};
	}
}

export function toolTipCvvHandler(e, val) {
	let _screenSize = Window.innerWidth;
	if(_screenSize >= 768) {
		e.target.closest(".cvv-info").querySelector(".whatIsCvv").style.display = val;
	}
}

// export function createBankList(banks) {
// 	let bankDataList = [];

// 	banks = banks.split(",");

// 	activateDefaultBank(banks);

// 	for (let i = 0; i < banks.length; i++) {
// 		bankDataList.push('<option value="'+ banks[i] +'">'+ banks[i] +'</option>');
// 	}

// 	let _selectBox = Window.id("bankList");
// 	if(_selectBox !== null) {
// 		_selectBox.innerHTML = bankDataList.join("");
	
// 		let option = document.createElement("option");
// 		option.text = "Select Bank";
// 		option.value = '';
// 		_selectBox.add(option, _selectBox[0]);
// 	}
// }

// export function activateDefaultBank(banks) {
// 	let _parent = Window.id("netBanking");

// 	if(_parent !== null) {
// 		let defaultList = _parent.querySelectorAll(".bankList");
	
// 		for(let i = 0; i < defaultList.length; i++) {
// 			let _listValue = defaultList[i].querySelector("input").value;
			
// 			if(banks.indexOf(_listValue) >= 0) {
// 				defaultList[i].classList.remove("d-none");
// 			}
// 		}
// 	}
// }

// export function _createWalletList(walletList) {
// 	let _walletList = Window.id("wallet-list");

// 	if(_walletList !== null) {
// 		let _walletData = '<div class="row">';
	
// 		for(let i = 0; i < walletList.length; i++) {
// 			_walletData += '<div class="col-8 col-sm-6 col-lg-4 pt-5 pb-20 bankList" id="'+ walletList[i] +'_wallet"><label class="custom-control-label bank_list_label d-flex align-items-center flex-column py-15" for="'+ walletList[i] +'" onclick="selectWallet(this)"><input type="radio" class="custom-control-input" id="'+ walletList[i] +'" value="'+ walletList[i] +'" name="wallet-radio"><span class="nb-icon rounded bg-white"><img src="' + Window.basePath + '/img/'+ walletList[i] +'.png" /></span><span class="nb-text d-block mt-10">'+ Window.walletText[walletList[i]] +'</span></label></div>';
// 		}
	
// 		_walletData += '<div class="col-sm-8 text-right d-none" id="wallet-logged-in"><div class="d-inline-block text-right"><div class="font-size-12 font-size-sm-14"><span class="lang" data-key="loggedIn">You are logged in using</span> <span class="font-size-15 font-size-sm-16 font-size-sm-18 text-grey-dark font-weight-bold" id="wallet-logged-mobile"></span> <span class="lang" data-key="loggedInAlt"></span></div><div class="line-height-15"><a href="#" class="text-underline font-size-10 font-size-sm-12 text-primary lang" data-key="useAnotherMobileAlt" onclick="editWalletNumber(\'edit\', event);">Login using another Mobile Number</a></div></div></div>';
	
// 		_walletData += '</div>';
	
// 		_walletList.innerHTML = _walletData;
// 	}

// }

export function _createPaymentTypeList(emiCC, emiDC) {
	Window.id("emiPaymentType").innerHTML = "";
	// Add credit card option
	if(emiCC) {
		_addSelectOption({text: multilingualText[Window.id("translate").value]["creditCard"], value: "EMICC", element: "emiPaymentType"});
	}

	// Add debit card option
	if(emiDC) {
		_addSelectOption({text: multilingualText[Window.id("translate").value]["debitCard"], value: "EMIDC", element: "emiPaymentType"});
	}
}



// SET TAX DECLARATION
export function setTaxDeclaration(paymentType) {
	if(paymentType === "CC" || paymentType === "DC" || paymentType === "IN" || paymentType === "EM") {
		if(Window.id("common-tax-declaration") !== null) {
			Window.id("common-tax-declaration").innerHTML = Window.taxDeclarationObj[paymentType];
		}
	} else if(paymentType === "SC") {
        let saveCardPaymentType = Window.querySelector(".saveCardDetails.active .payment-type").value;
        Window.id("charge-info").innerHTML = Window.taxDeclarationObj[saveCardPaymentType];
    } else {
		// Window.id(paymentType + "-tax-declaration").innerHTML = Window.taxDeclarationObj[paymentType];
	}
}

// export function upiQRAction(paymentType) {
// 	Window.id("loading").style.display = "block";

// 	let token = document.getElementsByName("customToken")[0].value,
// 		data = new FormData();

// 	data.append('token', token);

// 	let xhr = new XMLHttpRequest();
// 	xhr.open('POST', 'upiQrPay', true);

// 	xhr.onload = function() {
// 		let obj = JSON.parse(this.response);

// 		if(null !== obj) {
// 			Window.oid = obj.oid;
// 		}

// 		if(obj.responseCode === "000" && obj.responseMessage === "SUCCESS") {
// 			if(obj.upiQrCode !== undefined) {
// 				Window.id("upi-qr-img").innerHTML = '<img src="data:image/png;base64,'+ obj.upiQrCode +'" class="img-fluid">';

// 				startWorker({
// 					oid: Window.oid,
// 					requestType: "UPI_QR"
// 				});
// 			}
// 		} else {
// 			alert("Something went wrong!");
// 		}

// 		setTimeout(function() {
// 			Window.id("loading").style.display = "none";
// 		}, 1000);
// 	};

// 	xhr.send(data);
// }

export function editWalletNumber(action, e) {
	if(e !== undefined) {
		e.preventDefault();
	}

	if(action === "reset") {
		Window.querySelector("body").classList.remove("mobikwik--active");			
		Window.id("mobikwik-section").classList.add("d-none");
	}
	
	Window.id("btn-edit-wallet").classList.add("d-none");
	Window.id("wallet-otp-box").classList.add("d-none");
	Window.id("wallet-otp").value = "";

	if(Window.id("wallet-logged-in") !== null) {
		Window.id("wallet-logged-in").classList.add("d-none");
	}
	if(Window.id("wallet-logged-mobile") !== null) {
		Window.id("wallet-logged-mobile").innerHTML = "";
	}
	Window.id("available-balance").classList.add("d-none");
	Window.id("insufficient-fund").classList.add("d-none");
	Window.id("mobikwik-amount").innerHTML = "";

	Window.id("wallet-mobile-box").classList.remove("d-none");
	Window.id("wallet-mobile-number").value = "";
	Window.id("phoneNo").value = "";
	Window.id("wallet-mobile-number").disabled = false;
	Window.id("wallet-mobile-number").focus();

	Window.id("error-wallet-number").innerHTML = "";
	Window.id("error-wallet-number").classList.add("d-none");

	Window.id("btn-resend-otp").classList.add("d-none");
	Window.id("otp-timer").classList.add("d-none");
	Window.id("otp-timer").querySelector("span").innerHTML = "";
	clearInterval(Window.otpInterval);
	// Window.loginpin.reset();

	Window.id("pay-now").classList.add("btn-disabled");
}

export function addCustomScroll() {
	let tabInnerBox = Window.querySelectorAll(".tabbox-inner");

	for(let i = 0; i < tabInnerBox.length; i++) {
		if(tabInnerBox[i].offsetHeight > 410) {
			tabInnerBox[i].classList.add("custom-scroll");
		} else {
			tabInnerBox[i].classList.remove("custom-scroll");
		}
	}
}

export function changeSubmitPosition() {
	let _screenSize = Window.innerWidth,
		btns = Window.querySelectorAll(".submit-btns-tab");

	if(_screenSize <= 991) {
		if(Window.id("submit-btns-desktop").innerHTML !== "") {
			Window.globalBtnData = Window.id("submit-btns-desktop").innerHTML;
		}

		Window.id("submit-btns-desktop").innerHTML = "";
	
		for(let i = 0; i < btns.length; i++) {
			btns[i].innerHTML = "";
		}

		let getActiveTab = Window.querySelector(".tabLi.active").getAttribute("data-id");
		
		Window.id("submit-btns-" + getActiveTab).innerHTML = Window.globalBtnData;
	} else {
		if(Window.id("submit-btns-desktop").innerHTML === "") {
			Window.id("submit-btns-desktop").innerHTML = Window.globalBtnData;
		}

		for(let j = 0; j < btns.length; j++) {
			btns[j].innerHTML = "";
		}
	}

	if(Window.querySelector(".payBtnAmount") !== null) {
		Window.querySelector(".payBtnAmount").innerHTML = Window.id("totalAmount").innerHTML;
	}
}

export function wrapperPosition() {
    let _screenHeight = Window.innerHeight,
        _container = Window.id("container-outer-wrap"),
        _containerHeight = _container.offsetHeight;

    if(_screenHeight > _containerHeight) {
		_container.style.marginTop =  (_screenHeight - _containerHeight) / 2 + "px";
		_container.style.marginBottom = (_screenHeight - _containerHeight) / 2 + "px";
		_container.classList.remove("my-30");
    } else {
        _container.classList.add("my-30");
		_container.removeAttribute("style");
    }
}

// ADD SELECT OPTION
export function _addSelectOption(reffObj) {
	let dataKey = Window.id(reffObj.element).getAttribute("data-key");
	let setFirstOption = function() {
		let option = document.createElement("option");
		option.text = multilingualText[Window.id("translate").value][dataKey];
		option.value = '';
		Window.id(reffObj.element).add(option, Window.id(reffObj.element)[0]);
	}
	let _selectText = Window.id(reffObj.element).options[0];
	if(_selectText === undefined) {
		setFirstOption();
	} else if(_selectText.text !== multilingualText[Window.id("translate").value][dataKey]) {
		setFirstOption();
	}

	let opt = document.createElement("option");
	opt.appendChild(document.createTextNode(reffObj.text));
	opt.value = reffObj.value;
	Window.id(reffObj.element).appendChild(opt);
}

// REMOVE ACTIVE CLASS FROM SAVED VPA
export function removeActiveFromSavedVpa() {
	let vpaLi = document.getElementsByClassName("saveVpaDetails");

	for(let i = 0; i < vpaLi.length; i++) {		
		if(vpaLi[i].classList.contains("active")) {
			vpaLi[i].classList.remove("active");
			vpaLi[i].querySelector(".custom-control-input").checked = false;			
			Window.id("pay-now").classList.add("btn-disabled");
		}
	}

	// Window.id("error-blank-vpa").classList.remove("d-block");
}

export function changeCardInfo(paymentType) {
	if(paymentType === "CC") {
		Window.id("charge-info").innerHTML = Window.taxDeclarationObj[paymentType];
	} else if(paymentType === "DC") {
		Window.id("charge-info").innerHTML = Window.taxDeclarationObj[paymentType];
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
// 		Window.id("loading2").style.display = "block";
//     } else if(action === "hide") {
// 		Window.id("loading2").style.display = "none";
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

