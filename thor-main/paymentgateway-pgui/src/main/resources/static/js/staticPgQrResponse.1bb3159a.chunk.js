(this.webpackJsonpfrontend=this.webpackJsonpfrontend||[]).push([[24,30,31],{11:function(e,t){window.basePath="/pgui",window.querySelector=document.querySelector.bind(document),window.querySelectorAll=document.querySelectorAll.bind(document),window.id=document.getElementById.bind(document),window.byClass=document.getElementsByClassName.bind(document),window.createElement=document.createElement.bind(document),window.worker="",window.oid="",window.upiQrCodeResponse="",window.mqrCodeResponse="",window.worker=void 0,window.walletToCompare="PaytmWallet",window.addMoneyTimer=null},12:function(e,t,n){},13:function(e,t,n){},3:function(e,t,n){"use strict";function a(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}n.d(t,"a",(function(){return a}))},4:function(e,t,n){"use strict";function a(e,t){for(var n=0;n<t.length;n++){var a=t[n];a.enumerable=a.enumerable||!1,a.configurable=!0,"value"in a&&(a.writable=!0),Object.defineProperty(e,a.key,a)}}function r(e,t,n){return t&&a(e.prototype,t),n&&a(e,n),e}n.d(t,"a",(function(){return r}))},5:function(e,t,n){"use strict";function a(e){return(a=Object.setPrototypeOf?Object.getPrototypeOf:function(e){return e.__proto__||Object.getPrototypeOf(e)})(e)}function r(e){return(r="function"===typeof Symbol&&"symbol"===typeof Symbol.iterator?function(e){return typeof e}:function(e){return e&&"function"===typeof Symbol&&e.constructor===Symbol&&e!==Symbol.prototype?"symbol":typeof e})(e)}function i(e,t){return!t||"object"!==r(t)&&"function"!==typeof t?function(e){if(void 0===e)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return e}(e):t}function o(e){var t=function(){if("undefined"===typeof Reflect||!Reflect.construct)return!1;if(Reflect.construct.sham)return!1;if("function"===typeof Proxy)return!0;try{return Date.prototype.toString.call(Reflect.construct(Date,[],(function(){}))),!0}catch(e){return!1}}();return function(){var n,r=a(e);if(t){var o=a(this).constructor;n=Reflect.construct(r,arguments,o)}else n=r.apply(this,arguments);return i(this,n)}}n.d(t,"a",(function(){return o}))},52:function(e,t,n){e.exports=n(70)},53:function(e,t,n){},6:function(e,t,n){"use strict";function a(e,t){return(a=Object.setPrototypeOf||function(e,t){return e.__proto__=t,e})(e,t)}function r(e,t){if("function"!==typeof t&&null!==t)throw new TypeError("Super expression must either be null or a function");e.prototype=Object.create(t&&t.prototype,{constructor:{value:e,writable:!0,configurable:!0}}),t&&a(e,t)}n.d(t,"a",(function(){return r}))},7:function(e,t,n){"use strict";n.r(t);n(13);var a=n(0);t.default=function(e){return Object(a.jsx)("div",{className:"loader loader2",id:"loading2",children:Object(a.jsxs)("div",{className:"w-100 vh-100 d-flex justify-content-center align-items-center flex-column",children:[Object(a.jsx)("div",{className:"loaderImage",children:Object(a.jsx)("img",{src:window.basePath+"/img/loader.gif",alt:"Loader"})}),e.approvalNotification?Object(a.jsxs)("div",{id:"approvedNotification",className:"approvedNotification text-center",children:[Object(a.jsx)("h3",{className:"lang","data-key":"upiApprovalText",children:"Please approve the payment in your UPI App"}),Object(a.jsx)("p",{className:"lang","data-key":"upiStopRefresh",children:"Do not refresh this page or press back button"})]}):null,e.processing?Object(a.jsx)("div",{id:"loading2Loader",className:"defaultText mt-10 text-center",children:Object(a.jsx)("h3",{className:"lang","data-key":"defaultLoaderText",children:"Please wait while we process your payment..."})}):null]})})}},70:function(e,t,n){"use strict";n.r(t);var a=n(2),r=n.n(a),i=n(10),o=n.n(i),s=(n(11),n(3)),l=n(4),c=n(6),d=n(5),u=n(7),m=n(9),f=(n(53),n(0)),p=function(e){Object(c.a)(n,e);var t=Object(d.a)(n);function n(){var e;Object(s.a)(this,n);for(var a=arguments.length,r=new Array(a),i=0;i<a;i++)r[i]=arguments[i];return(e=t.call.apply(t,[this].concat(r))).state={data:null,showLoader:!1,orderId:null,isCustomerInfo:!1},e.generateOrderId=function(t){e.setState({orderId:t.slice(0,2).toUpperCase()+String((new Date).getTime())})},e.roundOf=function(e){var t=e.target.value;if(-1!=t.indexOf(".")){var n=Number(t);e.target.value=n.toFixed(2)}},e.isAlphabetOnly=function(e){" "===e.target.value?e.target.value="":e.target.value=e.target.value.replace(/[^A-Za-z ]/g,"")},e.isNumberWithDots=function(e){e.target.value=e.target.value.replace(/[^0-9.]/g,"")},e.isNumberOnly=function(e){e.target.value=e.target.value.replace(/[^0-9]/g,"")},e.isSpecialCharacterOnly=function(e){e.target.value=e.target.value.replace(/[^A-Za-z0-9!#$%&'*/=?^_+-`{|}~]/g,"")},e.validateMobile=function(t,n){var a=document.getElementById(n);return null===a||(""!==a.value&&10!==a.value.length?(e.displayError(t,a),!1):(e.removeError(t,a),!0))},e.isValidEmail=function(e){return!!e.value.match(/^[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[_A-Za-z0-9-]+)$/)},e.validateEmail=function(t,n){var a=document.getElementById(n);return null===a||(""!==a.value?e.isValidEmail(a)?(e.removeError(t,a),!0):(e.displayError(t,a),!1):(e.removeError(t,a),!0))},e.displayError=function(e,t){t.closest("div").classList.add("hasError")},e.removeError=function(e,t){t.closest("div").classList.remove("hasError")},e.isValidAmount=function(e){""!==e.target.value&&(e.target.value=Number(e.target.value).toFixed(2))},e.validateAmount=function(t,n){var a=document.getElementById(n),r=a.value;if(""!==r&&" "!==r){1==r.length&&-1!=r.indexOf("0")&&(a.value=r.slice(0,r.length-1));var i=r.match(/[.]/g);if(null!==i)i.length>1&&(a.value=r.slice(0,r.length-1)),r.slice(r.indexOf(".")).length>3&&(a.value=r.slice(0,r.length-1));return e.removeError(t,a),!0}return e.displayError(t,a),!1},e.validateField=function(t){var n=e.validateAmount(t,"amount");return e.state.isCustomerInfo&&(n=(n=n&&e.validateEmail(t,"customerEmail"))&&e.validateMobile(t,"customerMobile")),n},e.payButtonHandler=function(t){t.preventDefault(),e.validateField(t)&&(e.setState({showLoader:!0}),document.getElementById("amountInput").value=document.getElementById("amount").value,e.state.isCustomerInfo&&(document.getElementById("customer-name").value=document.getElementById("customerName").value,document.getElementById("customer-email").value=document.getElementById("customerEmail").value,document.getElementById("customer-mobile").value=document.getElementById("customerMobile").value),document.getElementById("redirectToPaymentAction").submit())},e.customerInfoHandler=function(t){e.setState({isCustomerInfo:!e.state.isCustomerInfo})},e}return Object(l.a)(n,[{key:"componentDidMount",value:function(){try{var e=window.id("sessionObj").value;void 0!==(e=JSON.parse(e)).payId?(this.generateOrderId(e.businessName),this.setState({data:e})):this.setState({data:"error"})}catch(t){this.setState({data:"error"}),console.error(t)}}},{key:"render",value:function(){var e=this;this.state.showLoader&&u.default;var t=Object(f.jsx)(u.default,{processing:!1,approvalNotification:!1});if(null!==this.state.data)if("error"===this.state.data)t=Object(f.jsx)(m.default,{});else{var n=Object(f.jsx)("img",{src:"".concat(window.basePath,"/img/logo.png"),alt:"Payment Gateway"});""!==this.state.data.merchantLogo&&null!==this.state.data.merchantLogo&&void 0!==this.state.data.merchantLogo&&(n=Object(f.jsx)("img",{src:this.state.data.merchantLogo,alt:"Payment Gateway",height:"70"}));var a=null;this.state.isCustomerInfo&&(a=Object(f.jsxs)(f.Fragment,{children:[Object(f.jsxs)("div",{className:"p-0 position-relative",children:[Object(f.jsx)("label",{className:"placeHolderText w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang field-title","data-key":"orderId",children:"Name"}),Object(f.jsx)("input",{autoComplete:"new-password",className:"pField inputField amount_field form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none",type:"text",name:"customerName",id:"customerName",onInput:this.isAlphabetOnly,onCopy:function(e){e.preventDefault()},onPaste:function(e){e.preventDefault()},onDrop:function(e){e.preventDefault()}})]}),Object(f.jsxs)("div",{className:"p-0 position-relative",children:[Object(f.jsx)("label",{className:"placeHolderText w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang field-title","data-key":"orderId",children:"Email"}),Object(f.jsx)("input",{autoComplete:"new-password",className:"pField inputField amount_field form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none",type:"text",name:"customerEmail",id:"customerEmail",onInput:this.isSpecialCharacterOnly,onBlur:function(t){return e.validateEmail(t,"customerEmail")},onCopy:function(e){e.preventDefault()},onPaste:function(e){e.preventDefault()},onDrop:function(e){e.preventDefault()}}),Object(f.jsx)("p",{className:"error",children:"Please enter valid Email ID."})]}),Object(f.jsxs)("div",{className:"p-0 position-relative",children:[Object(f.jsx)("label",{className:"placeHolderText w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang field-title","data-key":"orderId",children:"Mobile"}),Object(f.jsx)("input",{autoComplete:"new-password",className:"pField inputField amount_field form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none",type:"text",name:"customerMobile",id:"customerMobile",maxLength:10,inputMode:"numeric",onInput:this.isNumberOnly,onBlur:function(t){return e.validateMobile(t,"customerMobile")},onCopy:function(e){e.preventDefault()},onPaste:function(e){e.preventDefault()},onDrop:function(e){e.preventDefault()}}),Object(f.jsx)("p",{className:"error",children:"Please enter valid mobile number."})]})]})),t=Object(f.jsxs)(f.Fragment,{children:[Object(f.jsx)("section",{className:"response_container",children:Object(f.jsxs)("div",{className:"response_wrapper d-flex justify-content-between flex-column p-20 pb-0",children:[Object(f.jsxs)("div",{className:"response_header px-15",children:[this.state.data.payId!==this.state.data.PAYTENSE_PAY_ID?Object(f.jsx)("div",{className:"response_logo justify-content-center pt-15",children:n}):null,Object(f.jsxs)("div",{className:"response_text text-center mt-20",children:[Object(f.jsx)("div",{className:"response_merchant font-size-24",children:this.state.data.businessName}),Object(f.jsxs)("div",{className:"response_orderId font-size-14 mt-10",children:["Order ID : ",Object(f.jsx)("span",{className:"font-weight-bold",id:"ORDER_ID",children:this.state.orderId})]})]})]}),Object(f.jsxs)("div",{className:"response_body px-15",children:[Object(f.jsxs)("div",{className:"p-0 position-relative",children:[Object(f.jsxs)("label",{className:"placeHolderText w-100 text-grey-light font-size-12 line-height-15 d-inline-flex mb-5 lang field-title","data-key":"orderId",children:["Amount ",Object(f.jsx)("span",{className:"text-red ml-5",children:"*"})]}),Object(f.jsx)("input",{autoComplete:"new-password",className:"pField inputField amount_field form-control font-size-14 font-size-sm-16 text-grey-dark font-weight-medium p-8 border outline-none",type:"text",name:"amount",id:"amount",onBlur:function(t){e.roundOf(t),e.isValidAmount(t)},onInput:function(t){e.isNumberWithDots(t),e.validateAmount(t,"amount"),e.removeError(t,document.getElementById("amount"))},onCopy:function(e){e.preventDefault()},maxLength:"12",inputMode:"numeric",onPaste:function(e){e.preventDefault()},onDrop:function(e){e.preventDefault()}}),Object(f.jsx)("p",{className:"error",children:"Please enter amount"})]}),a,Object(f.jsx)("div",{className:"w-100 text-right",children:Object(f.jsxs)("button",{className:"btn-additional-info",onClick:this.customerInfoHandler,children:[this.state.isCustomerInfo?"-":"+"," Additional Info"]})})]}),Object(f.jsxs)("div",{className:"response_footer mt-20",children:[Object(f.jsx)("div",{className:"response_button text-center ".concat(this.state.data.payId===this.state.data.PAYTENSE_PAY_ID?"mb-20":null),children:Object(f.jsx)("button",{id:"payButton",className:"response_pay",onClick:this.payButtonHandler,children:"Pay"})}),this.state.data.payId!==this.state.data.PAYTENSE_PAY_ID?Object(f.jsx)("div",{id:"footer",children:Object(f.jsxs)("div",{className:"mt-20 col-lg-12 bg-grey-dark-primary d-flex align-items-center pt-15 pb-5 border-radius-bl-md-0 justify-content-center",id:"footer-poweredby",children:[Object(f.jsx)("span",{className:"text-grey-light-primary mtn-30 font-size-12",children:"Powered By"}),Object(f.jsx)("span",{className:"font-family-logo ml-5 mr-5 font-size-18 text-white",children:"Payment Gateway"}),Object(f.jsx)("span",{className:"text-white",children:"\xa9"})]})}):null]})]})}),Object(f.jsxs)("form",{action:"redirectToPaymentAction",id:"redirectToPaymentAction",method:"POST",children:[Object(f.jsx)("input",{type:"hidden",value:this.state.data.payId,name:"payId",id:"payId"}),Object(f.jsx)("input",{type:"hidden",value:this.state.orderId,name:"orderId",id:"orderId"}),Object(f.jsx)("input",{type:"hidden",value:"",name:"amount",id:"amountInput"}),Object(f.jsx)("input",{type:"hidden",value:"",name:"name",id:"customer-name"}),Object(f.jsx)("input",{type:"hidden",value:"",name:"email",id:"customer-email"}),Object(f.jsx)("input",{type:"hidden",value:"",name:"mobile",id:"customer-mobile"})]})]})}return Object(f.jsx)(f.Fragment,{children:t})}}]),n}(a.Component),h=n(8);o.a.render(Object(f.jsx)(r.a.StrictMode,{children:Object(f.jsx)(p,{})}),document.getElementById("root")),Object(h.a)()},8:function(e,t,n){"use strict";t.a=function(e){e&&e instanceof Function&&n.e(32).then(n.bind(null,16)).then((function(t){var n=t.getCLS,a=t.getFID,r=t.getFCP,i=t.getLCP,o=t.getTTFB;n(e),a(e),r(e),i(e),o(e)}))}},9:function(e,t,n){"use strict";n.r(t);var a=n(2),r=n.n(a),i=(n(12),n(0));t.default=function(e){return Object(i.jsxs)(r.a.Fragment,{children:[Object(i.jsx)("header",{className:"header-bg py-15 position-fixed top-0 left-0 w-100",children:Object(i.jsx)("div",{className:"container",children:Object(i.jsx)("div",{className:"row",children:Object(i.jsx)("div",{className:"col-12",children:Object(i.jsx)("h1",{className:"m-0",children:Object(i.jsx)("img",{src:window.basePath+"/img/white-logo.png",alt:""})})})})})}),Object(i.jsx)("div",{className:"container container-error-page d-flex align-items-center justify-content-center",children:Object(i.jsx)("div",{className:"row",children:Object(i.jsx)("div",{className:"col-12",children:Object(i.jsxs)("div",{className:"error-content text-center",children:[Object(i.jsx)("h1",{className:"text-black font-weight-medium font-size-55 mb-10",children:"Oops!"}),Object(i.jsx)("p",{className:"text-primary-lightest font-size-30 font-weight-normal",children:"We can't find the page you are looking for."})]})})})})]})}}},[[52,21,0]]]);