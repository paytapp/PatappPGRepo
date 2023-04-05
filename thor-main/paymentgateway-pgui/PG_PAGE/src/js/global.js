// GLOBAL VARIABLES
window.basePath = process.env.PUBLIC_URL;

window.querySelector = document.querySelector.bind(document);
window.querySelectorAll = document.querySelectorAll.bind(document);
window.id = document.getElementById.bind(document);
window.byClass = document.getElementsByClassName.bind(document);
window.createElement = document.createElement.bind(document);
window.worker = "";
window.oid = "";
window.upiQrCodeResponse = "";
window.mqrCodeResponse = "";
window.worker = undefined;
window.walletToCompare = "PaytmWallet";
// window.walletToCompare = "";

window.addMoneyTimer = null;