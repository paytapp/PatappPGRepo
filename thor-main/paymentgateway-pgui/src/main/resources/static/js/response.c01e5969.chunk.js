(this.webpackJsonpfrontend=this.webpackJsonpfrontend||[]).push([[10,30,31],{11:function(e,t){window.basePath="/pgui",window.querySelector=document.querySelector.bind(document),window.querySelectorAll=document.querySelectorAll.bind(document),window.id=document.getElementById.bind(document),window.byClass=document.getElementsByClassName.bind(document),window.createElement=document.createElement.bind(document),window.worker="",window.oid="",window.upiQrCodeResponse="",window.mqrCodeResponse="",window.worker=void 0,window.walletToCompare="PaytmWallet",window.addMoneyTimer=null},12:function(e,t,n){},13:function(e,t,n){},18:function(e,t,n){"use strict";var a=n(0);t.a=function(){return Object(a.jsxs)("div",{className:"row custom-footer bg-grey-ternary position-relative",children:[Object(a.jsx)("div",{className:"col-sm-6 col-lg-8 payment-accept d-md-flex justify-content-sm-between justify-content-lg-start py-15 py-lg-20",children:Object(a.jsxs)("div",{className:"d-flex justify-content-center align-items-center",children:[Object(a.jsx)("img",{src:"".concat(window.basePath,"/img/visa-logo.png"),alt:""}),Object(a.jsx)("img",{src:"".concat(window.basePath,"/img/mcard.png"),alt:""}),Object(a.jsxs)("span",{className:"pg-icon icon-rupay-logo font-size-20 mr-5",children:[Object(a.jsx)("span",{className:"path1"}),Object(a.jsx)("span",{className:"path2"}),Object(a.jsx)("span",{className:"path3"}),Object(a.jsx)("span",{className:"path4"})]})]})}),Object(a.jsxs)("div",{className:"col-sm-6 col-lg-4 bg-grey-dark-primary d-flex align-items-center py-15 border-radius-br-20 border-radius-bl-20 border-radius-bl-sm-0 border-radius-lg-none justify-content-center",children:[Object(a.jsx)("span",{className:"text-grey-light-primary mtn-30 font-size-12",children:"Powered By"}),Object(a.jsx)("span",{className:"font-family-logo ml-5 mr-5 font-size-18 text-white",children:"Paytapp"}),Object(a.jsx)("span",{className:"text-white",children:"\xa9"})]})]})}},19:function(e,t,n){"use strict";var a=n(0);t.a=function(e){var t={ORDER_ID:"Order ID",PAY_ID:"Pay ID",REG_NUMBER:"Registration No.",PG_REF_NUM:"PG Ref No.",MERCHANT_NAME:"Merchant Name",RESPONSE_DATE_TIME:"Create Date",RESPONSE_MESSAGE:"Response Message",STATUS:"Status",TOTAL_AMOUNT:"Total Amount",ENCDATA:"Encrypted Data",orderid:"Order ID",amount:"Amount"},n=null;return null!==e.data&&(n=Object.keys(t).map((function(n){if(null!==e.data[n]&&""!==e.data[n]&&void 0!==e.data[n]){if("TOTAL_AMOUNT"==n||"amount"==n)return Object(a.jsxs)("li",{className:"d-flex justify-content-between pt-10 font-weight-medium font-size-18",children:[Object(a.jsx)("span",{className:"d-inline-block mwp-40",children:t[n]}),Object(a.jsxs)("span",{children:[Object(a.jsx)("span",{className:"font-weight-medium mr-5",children:"\u20b9"}),Object(a.jsx)("span",{children:"TOTAL_AMOUNT"==n?(Number(e.data[n])/100).toFixed(2):e.data[n]})]})]},n);if("PAY_ID"!==n)return Object(a.jsxs)("li",{className:"d-flex justify-content-between pt-15 font-size-14 line-height-16",children:[Object(a.jsx)("span",{className:"d-inline-block mwp-40",children:t[n]}),Object(a.jsx)("span",{className:"mwp-60 word-wrap text-right",children:e.data[n]})]},n);if(void 0!==e.data.ENCDATA)return Object(a.jsxs)("li",{className:"d-flex justify-content-between pt-15 font-size-14 line-height-16",children:[Object(a.jsx)("span",{className:"d-inline-block mwp-40",children:t[n]}),Object(a.jsx)("span",{className:"mwp-60 word-wrap text-right",children:e.data[n]})]},n)}return null}))),Object(a.jsxs)("div",{className:"row",children:[Object(a.jsx)("div",{className:"col-12 d-flex justify-content-center",children:Object(a.jsxs)("div",{className:"card_box p-15 w-100 ".concat(e.cardBox),children:[Object(a.jsx)("div",{className:"card_box_icon text-center line-height-30 d-flex justify-content-center align-items-center text-white font-size-20 mb-15 mx-auto rounded-circle",style:{backgroundImage:"url(".concat(window.basePath,"/img/").concat(e.cardBox,".png)")}}),Object(a.jsx)("h3",{className:"text-center statusResult text-capitalize mb-10 font-weight-medium font-size-18",children:e.statusResult}),Object(a.jsx)("ul",{className:"list-unstyled mb-0",children:n})]})}),void 0!==e.timer&&e.timer.isTextVisible?Object(a.jsxs)("div",{className:"col-12 text-center font-size-14 font-weight-bold mb-30",children:["You will automatically redirect to Payment Page in ",e.timer.timerText]}):null]})}},20:function(e,t,n){"use strict";var a=n(3),s=n(4),c=n(6),i=n(5),o=n(2),r=n(0),l=function(e){return Object(r.jsxs)("button",{className:"pos_btn btn font-size-14 p-0 ml-5",id:"mailPdf",onClick:function(){e.klass.setState({showLoader:!0});var t={ORDER_ID:e.klass.state.data.ORDER_ID};fetch("".concat(window.basePath,"/jsp/sendMail"),{method:"POST",body:JSON.stringify(t),headers:{"Content-Type":"application/json",Accept:"application/json"}}).then((function(e){return e.json()})).then((function(t){var n,a;n=t.status,a=t.responseMsg,e.klass.setState({showLoader:!1,isDialogOpen:!0,dialogMsg:a,dialogType:n})}))},children:[Object(r.jsx)("i",{className:"fas fa-envelope font-size-18"})," Email"]})},d=function(e){return Object(r.jsxs)("button",{className:"pos_btn btn font-size-14 p-0 mr-5",id:"savePdf",onClick:e.klass.downloadPdfHandler,children:[Object(r.jsx)("i",{className:"fas fa-chevron-circle-down font-size-18"})," Download"]})},u=function(e){Object(c.a)(n,e);var t=Object(i.a)(n);function n(){var e;Object(a.a)(this,n);for(var s=arguments.length,c=new Array(s),i=0;i<s;i++)c[i]=arguments[i];return(e=t.call.apply(t,[this].concat(c))).downloadButtonsHandler=function(){var t=e.props.klass.state.data,n=t.CUST_EMAIL,a=t.RETURN_URL,s=null;return null!==n&&void 0!==n&&""!==n&&(s=Object(r.jsx)(l,{klass:e.props.klass})),"response"!==a.substring(a.lastIndexOf("/")+1)?Object(r.jsxs)("div",{className:"buttonDownLoad justify-content-end",children:[Object(r.jsx)(d,{klass:e.props.klass}),s]}):null},e}return Object(s.a)(n,[{key:"render",value:function(){return Object(r.jsx)("div",{className:"row",children:Object(r.jsxs)("div",{className:"col-12 d-flex align-items-center justify-content-between",children:[Object(r.jsx)("div",{children:Object(r.jsx)("img",{src:"".concat(window.basePath,"/img/logo.png"),alt:"Payment Gateway Solutions Pvt. Ltd."})}),this.downloadButtonsHandler()]})})}}]),n}(o.Component);t.a=u},23:function(e,t,n){},3:function(e,t,n){"use strict";function a(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}n.d(t,"a",(function(){return a}))},37:function(e,t,n){e.exports=n(62)},38:function(e,t,n){},4:function(e,t,n){"use strict";function a(e,t){for(var n=0;n<t.length;n++){var a=t[n];a.enumerable=a.enumerable||!1,a.configurable=!0,"value"in a&&(a.writable=!0),Object.defineProperty(e,a.key,a)}}function s(e,t,n){return t&&a(e.prototype,t),n&&a(e,n),e}n.d(t,"a",(function(){return s}))},5:function(e,t,n){"use strict";function a(e){return(a=Object.setPrototypeOf?Object.getPrototypeOf:function(e){return e.__proto__||Object.getPrototypeOf(e)})(e)}function s(e){return(s="function"===typeof Symbol&&"symbol"===typeof Symbol.iterator?function(e){return typeof e}:function(e){return e&&"function"===typeof Symbol&&e.constructor===Symbol&&e!==Symbol.prototype?"symbol":typeof e})(e)}function c(e,t){return!t||"object"!==s(t)&&"function"!==typeof t?function(e){if(void 0===e)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return e}(e):t}function i(e){var t=function(){if("undefined"===typeof Reflect||!Reflect.construct)return!1;if(Reflect.construct.sham)return!1;if("function"===typeof Proxy)return!0;try{return Date.prototype.toString.call(Reflect.construct(Date,[],(function(){}))),!0}catch(e){return!1}}();return function(){var n,s=a(e);if(t){var i=a(this).constructor;n=Reflect.construct(s,arguments,i)}else n=s.apply(this,arguments);return c(this,n)}}n.d(t,"a",(function(){return i}))},6:function(e,t,n){"use strict";function a(e,t){return(a=Object.setPrototypeOf||function(e,t){return e.__proto__=t,e})(e,t)}function s(e,t){if("function"!==typeof t&&null!==t)throw new TypeError("Super expression must either be null or a function");e.prototype=Object.create(t&&t.prototype,{constructor:{value:e,writable:!0,configurable:!0}}),t&&a(e,t)}n.d(t,"a",(function(){return s}))},62:function(e,t,n){"use strict";n.r(t);var a=n(2),s=n.n(a),c=n(10),i=n.n(c),o=(n(11),n(8)),r=n(3),l=n(4),d=n(6),u=n(5),j=n(7),f=n(9),m=(n(23),n(18)),b=n(20),p=n(19),h=(n(38),n(0)),x=function(e){Object(d.a)(n,e);var t=Object(u.a)(n);function n(e){return Object(r.a)(this,n),t.call(this,e)}return Object(l.a)(n,[{key:"render",value:function(){var e="success"===this.props.dialogType?"successMsg":"failedMsg";return Object(h.jsx)(h.Fragment,{children:Object(h.jsx)("div",{className:"position-fixed top-0 left-0 w-100 h-100 bg-dark-transparent dialog-container",children:Object(h.jsx)("div",{className:"dialog-box border-radius-primary ".concat(e),children:Object(h.jsxs)("div",{className:"position-relative p-30",children:[Object(h.jsx)("button",{className:"bg-none border-none dialog-close-btn font-size-14",onClick:this.props.onClose,children:Object(h.jsx)("i",{className:"pg-icon icon-cancel-cross"})}),Object(h.jsx)("div",{className:"card_box_icon text-center line-height-30 d-flex justify-content-center align-items-center text-white font-size-20 mb-15 mx-auto rounded-circle",style:{backgroundImage:"url(".concat(window.basePath,"/img/").concat(e,".png)")}}),Object(h.jsx)("h4",{className:"text-center statusResult text-capitalize mb-10 font-weight-medium font-size-18",children:this.props.children})]})})})})}}]),n}(a.Component),O=function(e){Object(d.a)(n,e);var t=Object(u.a)(n);function n(e){var a;return Object(r.a)(this,n),(a=t.call(this,e)).fetchData=function(e){if(window.location!==window.parent.location){var t=a.addParam(e);window.parent.postMessage(t,"*"),window.close()}else if(void 0!==window.opener&&null!==window.opener){var n=a.addParam(e);window.opener.postMessage(n,"*"),window.close()}else a.setState({data:e}),void 0===e.ENCDATA&&a.updateStatus(e.STATUS)},a.addParam=function(e){var t=e;return void 0==t.PAYMENT_FlOW&&"ADDANDPAY"!==t.PAYMENT_FlOW&&(t.closeIframe=!0),t},a.updateStatus=function(e){"AUTHENTICATION_FAILED"==e||"Cancelled"==e||"Invalid"==e?(a.setState({statusResult:"Payment Failed"}),a.setState({cardBox:"failedMsg"})):"Captured"==e||"Success"==e?(a.setState({statusResult:"Payment Successful"}),a.setState({cardBox:"successMsg"})):"Pending"==e?(a.setState({statusResult:"Payment Pending"}),a.setState({cardBox:"pendingMsg"})):(a.setState({statusResult:"Payment Failed"}),a.setState({cardBox:"failedMsg"}))},a.dialogCloseHandler=function(){a.setState({isDialogOpen:!1})},a.downloadPdfHandler=function(){a.downloadForm.current.submit()},a.state={data:null,statusResult:null,cardBox:null,isDialogOpen:!1,dialogMsg:null,dialogType:null,showLoader:!1},a.downloadForm=s.a.createRef(),a}return Object(l.a)(n,[{key:"componentDidMount",value:function(){try{var e=window.id("sessionObj").value;void 0!==(e=JSON.parse(e)).RESPONSE_CODE||void 0!==e.ENCDATA?this.fetchData(e):this.setState({data:"error"})}catch(t){this.setState({data:"error"}),console.error(t)}}},{key:"render",value:function(){var e=null;this.state.showLoader&&(e=Object(h.jsx)(j.default,{processing:!1,approvalNotification:!1}));var t=Object(h.jsx)(j.default,{processing:!1,approvalNotification:!1});null!==this.state.data&&(t="error"==this.state.data?Object(h.jsx)(f.default,{}):Object(h.jsxs)(h.Fragment,{children:[Object(h.jsx)("div",{className:"container custom-container",id:"response-container",children:Object(h.jsxs)("div",{className:"bg-grey-secondary p-15 pb-0 p-lg-60 pb-lg-0 border-radius-20 box-shadow-primary border-primary",children:[Object(h.jsx)(b.a,{klass:this}),Object(h.jsx)(p.a,{cardBox:this.state.cardBox,statusResult:this.state.statusResult,data:this.state.data}),Object(h.jsx)(m.a,{})]})}),Object(h.jsx)("form",{action:"downloadPDF",method:"POST",ref:this.downloadForm,children:Object(h.jsx)("input",{type:"hidden",name:"ORDER_ID",value:this.state.data.ORDER_ID})})]}));var n=null;return this.state.isDialogOpen&&(n=Object(h.jsx)(x,{dialogType:this.state.dialogType,onClose:this.dialogCloseHandler,children:this.state.dialogMsg})),Object(h.jsxs)(h.Fragment,{children:[t,n,e]})}}]),n}(a.Component);i.a.render(Object(h.jsx)(s.a.StrictMode,{children:Object(h.jsx)(O,{})}),document.getElementById("root")),Object(o.a)()},7:function(e,t,n){"use strict";n.r(t);n(13);var a=n(0);t.default=function(e){return Object(a.jsx)("div",{className:"loader loader2",id:"loading2",children:Object(a.jsxs)("div",{className:"w-100 vh-100 d-flex justify-content-center align-items-center flex-column",children:[Object(a.jsx)("div",{className:"loaderImage",children:Object(a.jsx)("img",{src:window.basePath+"/img/loader.gif",alt:"Loader"})}),e.approvalNotification?Object(a.jsxs)("div",{id:"approvedNotification",className:"approvedNotification text-center",children:[Object(a.jsx)("h3",{className:"lang","data-key":"upiApprovalText",children:"Please approve the payment in your UPI App"}),Object(a.jsx)("p",{className:"lang","data-key":"upiStopRefresh",children:"Do not refresh this page or press back button"})]}):null,e.processing?Object(a.jsx)("div",{id:"loading2Loader",className:"defaultText mt-10 text-center",children:Object(a.jsx)("h3",{className:"lang","data-key":"defaultLoaderText",children:"Please wait while we process your payment..."})}):null]})})}},8:function(e,t,n){"use strict";t.a=function(e){e&&e instanceof Function&&n.e(32).then(n.bind(null,16)).then((function(t){var n=t.getCLS,a=t.getFID,s=t.getFCP,c=t.getLCP,i=t.getTTFB;n(e),a(e),s(e),c(e),i(e)}))}},9:function(e,t,n){"use strict";n.r(t);var a=n(2),s=n.n(a),c=(n(12),n(0));t.default=function(e){return Object(c.jsxs)(s.a.Fragment,{children:[Object(c.jsx)("header",{className:"header-bg py-15 position-fixed top-0 left-0 w-100",children:Object(c.jsx)("div",{className:"container",children:Object(c.jsx)("div",{className:"row",children:Object(c.jsx)("div",{className:"col-12",children:Object(c.jsx)("h1",{className:"m-0",children:Object(c.jsx)("img",{src:window.basePath+"/img/white-logo.png",alt:""})})})})})}),Object(c.jsx)("div",{className:"container container-error-page d-flex align-items-center justify-content-center",children:Object(c.jsx)("div",{className:"row",children:Object(c.jsx)("div",{className:"col-12",children:Object(c.jsxs)("div",{className:"error-content text-center",children:[Object(c.jsx)("h1",{className:"text-black font-weight-medium font-size-55 mb-10",children:"Oops!"}),Object(c.jsx)("p",{className:"text-primary-lightest font-size-30 font-weight-normal",children:"We can't find the page you are looking for."})]})})})})]})}}},[[37,20,0]]]);