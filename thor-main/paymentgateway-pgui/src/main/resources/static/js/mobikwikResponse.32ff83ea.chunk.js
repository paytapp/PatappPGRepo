(this.webpackJsonpfrontend=this.webpackJsonpfrontend||[]).push([[9,30,31],{1:function(e,t,n){"use strict";n.d(t,"a",(function(){return r}));var a=n(17);function s(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function r(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?s(Object(n),!0).forEach((function(t){Object(a.a)(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):s(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}},11:function(e,t){window.basePath="/pgui",window.querySelector=document.querySelector.bind(document),window.querySelectorAll=document.querySelectorAll.bind(document),window.id=document.getElementById.bind(document),window.byClass=document.getElementsByClassName.bind(document),window.createElement=document.createElement.bind(document),window.worker="",window.oid="",window.upiQrCodeResponse="",window.mqrCodeResponse="",window.worker=void 0,window.walletToCompare="PaytmWallet",window.addMoneyTimer=null},12:function(e,t,n){},13:function(e,t,n){},17:function(e,t,n){"use strict";function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}n.d(t,"a",(function(){return a}))},18:function(e,t,n){"use strict";var a=n(0);t.a=function(){return Object(a.jsxs)("div",{className:"row custom-footer bg-grey-ternary position-relative",children:[Object(a.jsx)("div",{className:"col-sm-6 col-lg-8 payment-accept d-md-flex justify-content-sm-between justify-content-lg-start py-15 py-lg-20",children:Object(a.jsxs)("div",{className:"d-flex justify-content-center align-items-center",children:[Object(a.jsx)("img",{src:"".concat(window.basePath,"/img/visa-logo.png"),alt:""}),Object(a.jsx)("img",{src:"".concat(window.basePath,"/img/mcard.png"),alt:""}),Object(a.jsxs)("span",{className:"pg-icon icon-rupay-logo font-size-20 mr-5",children:[Object(a.jsx)("span",{className:"path1"}),Object(a.jsx)("span",{className:"path2"}),Object(a.jsx)("span",{className:"path3"}),Object(a.jsx)("span",{className:"path4"})]})]})}),Object(a.jsxs)("div",{className:"col-sm-6 col-lg-4 bg-grey-dark-primary d-flex align-items-center py-15 border-radius-br-20 border-radius-bl-20 border-radius-bl-sm-0 border-radius-lg-none justify-content-center",children:[Object(a.jsx)("span",{className:"text-grey-light-primary mtn-30 font-size-12",children:"Powered By"}),Object(a.jsx)("span",{className:"font-family-logo ml-5 mr-5 font-size-18 text-white",children:"Paytapp"}),Object(a.jsx)("span",{className:"text-white",children:"\xa9"})]})]})}},19:function(e,t,n){"use strict";var a=n(0);t.a=function(e){var t={ORDER_ID:"Order ID",PAY_ID:"Pay ID",REG_NUMBER:"Registration No.",PG_REF_NUM:"PG Ref No.",MERCHANT_NAME:"Merchant Name",RESPONSE_DATE_TIME:"Create Date",RESPONSE_MESSAGE:"Response Message",STATUS:"Status",TOTAL_AMOUNT:"Total Amount",ENCDATA:"Encrypted Data",orderid:"Order ID",amount:"Amount"},n=null;return null!==e.data&&(n=Object.keys(t).map((function(n){if(null!==e.data[n]&&""!==e.data[n]&&void 0!==e.data[n]){if("TOTAL_AMOUNT"==n||"amount"==n)return Object(a.jsxs)("li",{className:"d-flex justify-content-between pt-10 font-weight-medium font-size-18",children:[Object(a.jsx)("span",{className:"d-inline-block mwp-40",children:t[n]}),Object(a.jsxs)("span",{children:[Object(a.jsx)("span",{className:"font-weight-medium mr-5",children:"\u20b9"}),Object(a.jsx)("span",{children:"TOTAL_AMOUNT"==n?(Number(e.data[n])/100).toFixed(2):e.data[n]})]})]},n);if("PAY_ID"!==n)return Object(a.jsxs)("li",{className:"d-flex justify-content-between pt-15 font-size-14 line-height-16",children:[Object(a.jsx)("span",{className:"d-inline-block mwp-40",children:t[n]}),Object(a.jsx)("span",{className:"mwp-60 word-wrap text-right",children:e.data[n]})]},n);if(void 0!==e.data.ENCDATA)return Object(a.jsxs)("li",{className:"d-flex justify-content-between pt-15 font-size-14 line-height-16",children:[Object(a.jsx)("span",{className:"d-inline-block mwp-40",children:t[n]}),Object(a.jsx)("span",{className:"mwp-60 word-wrap text-right",children:e.data[n]})]},n)}return null}))),Object(a.jsxs)("div",{className:"row",children:[Object(a.jsx)("div",{className:"col-12 d-flex justify-content-center",children:Object(a.jsxs)("div",{className:"card_box p-15 w-100 ".concat(e.cardBox),children:[Object(a.jsx)("div",{className:"card_box_icon text-center line-height-30 d-flex justify-content-center align-items-center text-white font-size-20 mb-15 mx-auto rounded-circle",style:{backgroundImage:"url(".concat(window.basePath,"/img/").concat(e.cardBox,".png)")}}),Object(a.jsx)("h3",{className:"text-center statusResult text-capitalize mb-10 font-weight-medium font-size-18",children:e.statusResult}),Object(a.jsx)("ul",{className:"list-unstyled mb-0",children:n})]})}),void 0!==e.timer&&e.timer.isTextVisible?Object(a.jsxs)("div",{className:"col-12 text-center font-size-14 font-weight-bold mb-30",children:["You will automatically redirect to Payment Page in ",e.timer.timerText]}):null]})}},20:function(e,t,n){"use strict";var a=n(3),s=n(4),r=n(6),c=n(5),i=n(2),o=n(0),l=function(e){return Object(o.jsxs)("button",{className:"pos_btn btn font-size-14 p-0 ml-5",id:"mailPdf",onClick:function(){e.klass.setState({showLoader:!0});var t={ORDER_ID:e.klass.state.data.ORDER_ID};fetch("".concat(window.basePath,"/jsp/sendMail"),{method:"POST",body:JSON.stringify(t),headers:{"Content-Type":"application/json",Accept:"application/json"}}).then((function(e){return e.json()})).then((function(t){var n,a;n=t.status,a=t.responseMsg,e.klass.setState({showLoader:!1,isDialogOpen:!0,dialogMsg:a,dialogType:n})}))},children:[Object(o.jsx)("i",{className:"fas fa-envelope font-size-18"})," Email"]})},d=function(e){return Object(o.jsxs)("button",{className:"pos_btn btn font-size-14 p-0 mr-5",id:"savePdf",onClick:e.klass.downloadPdfHandler,children:[Object(o.jsx)("i",{className:"fas fa-chevron-circle-down font-size-18"})," Download"]})},u=function(e){Object(r.a)(n,e);var t=Object(c.a)(n);function n(){var e;Object(a.a)(this,n);for(var s=arguments.length,r=new Array(s),c=0;c<s;c++)r[c]=arguments[c];return(e=t.call.apply(t,[this].concat(r))).downloadButtonsHandler=function(){var t=e.props.klass.state.data,n=t.CUST_EMAIL,a=t.RETURN_URL,s=null;return null!==n&&void 0!==n&&""!==n&&(s=Object(o.jsx)(l,{klass:e.props.klass})),"response"!==a.substring(a.lastIndexOf("/")+1)?Object(o.jsxs)("div",{className:"buttonDownLoad justify-content-end",children:[Object(o.jsx)(d,{klass:e.props.klass}),s]}):null},e}return Object(s.a)(n,[{key:"render",value:function(){return Object(o.jsx)("div",{className:"row",children:Object(o.jsxs)("div",{className:"col-12 d-flex align-items-center justify-content-between",children:[Object(o.jsx)("div",{children:Object(o.jsx)("img",{src:"".concat(window.basePath,"/img/logo.png"),alt:"Payment Gateway Solutions Pvt. Ltd."})}),this.downloadButtonsHandler()]})})}}]),n}(i.Component);t.a=u},23:function(e,t,n){},3:function(e,t,n){"use strict";function a(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}n.d(t,"a",(function(){return a}))},4:function(e,t,n){"use strict";function a(e,t){for(var n=0;n<t.length;n++){var a=t[n];a.enumerable=a.enumerable||!1,a.configurable=!0,"value"in a&&(a.writable=!0),Object.defineProperty(e,a.key,a)}}function s(e,t,n){return t&&a(e.prototype,t),n&&a(e,n),e}n.d(t,"a",(function(){return s}))},43:function(e,t,n){e.exports=n(65)},5:function(e,t,n){"use strict";function a(e){return(a=Object.setPrototypeOf?Object.getPrototypeOf:function(e){return e.__proto__||Object.getPrototypeOf(e)})(e)}function s(e){return(s="function"===typeof Symbol&&"symbol"===typeof Symbol.iterator?function(e){return typeof e}:function(e){return e&&"function"===typeof Symbol&&e.constructor===Symbol&&e!==Symbol.prototype?"symbol":typeof e})(e)}function r(e,t){return!t||"object"!==s(t)&&"function"!==typeof t?function(e){if(void 0===e)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return e}(e):t}function c(e){var t=function(){if("undefined"===typeof Reflect||!Reflect.construct)return!1;if(Reflect.construct.sham)return!1;if("function"===typeof Proxy)return!0;try{return Date.prototype.toString.call(Reflect.construct(Date,[],(function(){}))),!0}catch(e){return!1}}();return function(){var n,s=a(e);if(t){var c=a(this).constructor;n=Reflect.construct(s,arguments,c)}else n=s.apply(this,arguments);return r(this,n)}}n.d(t,"a",(function(){return c}))},6:function(e,t,n){"use strict";function a(e,t){return(a=Object.setPrototypeOf||function(e,t){return e.__proto__=t,e})(e,t)}function s(e,t){if("function"!==typeof t&&null!==t)throw new TypeError("Super expression must either be null or a function");e.prototype=Object.create(t&&t.prototype,{constructor:{value:e,writable:!0,configurable:!0}}),t&&a(e,t)}n.d(t,"a",(function(){return s}))},65:function(e,t,n){"use strict";n.r(t);var a=n(2),s=n.n(a),r=n(10),c=n.n(r),i=(n(11),n(8)),o=n(1),l=n(3),d=n(4),u=n(6),f=n(5),j=n(7),b=n(9),p=(n(23),n(18)),m=n(20),h=n(19),O=n(0),x=function(e){Object(u.a)(n,e);var t=Object(f.a)(n);function n(){var e;Object(l.a)(this,n);for(var a=arguments.length,s=new Array(a),r=0;r<a;r++)s[r]=arguments[r];return(e=t.call.apply(t,[this].concat(s))).state={data:null,statusResult:null,cardBox:null,timer:{isTextVisible:!1,timerText:"00:05"},loader:{showLoader:!1,defaultText:!1,approvalNotification:!1}},e.updateStatus=function(t,n){e.setState({statusResult:n}),"0"==t?e.setState({cardBox:"successMsg"}):e.setState({cardBox:"failedMsg"})},e.startTimerHandler=function(t){var n=5;e.setState((function(e){return{timer:Object(o.a)(Object(o.a)({},e.timer),{},{isTextVisible:!0})}}));var a=setInterval((function(){s()}),1e3),s=function(t){var s=Math.floor(n/60),r=n%60;if(r<10&&(r="0"+r),s<10&&(s="0"+s),e.setState((function(e){return{timer:Object(o.a)(Object(o.a)({},e.timer),{},{timerText:s+":"+r})}})),n>0)n-=1;else{clearInterval(a),e.setState((function(e){return{loader:Object(o.a)(Object(o.a)({},e.loader),{},{showLoader:!0}),timer:Object(o.a)(Object(o.a)({},e.timer),{},{timerText:"00:00",isTextVisible:!1})}}));var c={status:!0};window.location!==window.parent.location?window.parent.postMessage(c,"*"):void 0!==window.opener&&null!==window.opener&&window.opener.postMessage(c,"*"),window.close()}}},e}return Object(d.a)(n,[{key:"componentDidMount",value:function(){try{var e=window.id("sessionObj").value;void 0!==(e=JSON.parse(e)).statuscode?(this.setState({data:e}),this.updateStatus(e.statuscode,e.statusmessage),this.startTimerHandler()):this.setState({data:"error"})}catch(t){this.setState({data:"error"}),console.error(t)}}},{key:"render",value:function(){var e=null;this.state.loader.showLoader&&(e=Object(O.jsx)(j.default,{processing:this.state.loader.defaultText,approvalNotification:this.state.loader.approvalNotification}));var t=Object(O.jsx)(j.default,{processing:!1,approvalNotification:!1});return null!==this.state.data&&(t="error"==this.state.data?Object(O.jsx)(b.default,{}):Object(O.jsxs)(O.Fragment,{children:[Object(O.jsx)("div",{className:"container custom-container",id:"response-container",children:Object(O.jsxs)("div",{className:"bg-grey-secondary p-15 pb-0 p-lg-60 pb-lg-0 border-radius-20 box-shadow-primary border-primary",children:[Object(O.jsx)(m.a,{}),Object(O.jsx)(h.a,{cardBox:this.state.cardBox,statusResult:this.state.statusResult,data:this.state.data,timer:this.state.timer}),Object(O.jsx)(p.a,{})]})}),e]})),Object(O.jsx)(O.Fragment,{children:t})}}]),n}(a.Component);c.a.render(Object(O.jsx)(s.a.StrictMode,{children:Object(O.jsx)(x,{})}),document.getElementById("root")),Object(i.a)()},7:function(e,t,n){"use strict";n.r(t);n(13);var a=n(0);t.default=function(e){return Object(a.jsx)("div",{className:"loader loader2",id:"loading2",children:Object(a.jsxs)("div",{className:"w-100 vh-100 d-flex justify-content-center align-items-center flex-column",children:[Object(a.jsx)("div",{className:"loaderImage",children:Object(a.jsx)("img",{src:window.basePath+"/img/loader.gif",alt:"Loader"})}),e.approvalNotification?Object(a.jsxs)("div",{id:"approvedNotification",className:"approvedNotification text-center",children:[Object(a.jsx)("h3",{className:"lang","data-key":"upiApprovalText",children:"Please approve the payment in your UPI App"}),Object(a.jsx)("p",{className:"lang","data-key":"upiStopRefresh",children:"Do not refresh this page or press back button"})]}):null,e.processing?Object(a.jsx)("div",{id:"loading2Loader",className:"defaultText mt-10 text-center",children:Object(a.jsx)("h3",{className:"lang","data-key":"defaultLoaderText",children:"Please wait while we process your payment..."})}):null]})})}},8:function(e,t,n){"use strict";t.a=function(e){e&&e instanceof Function&&n.e(32).then(n.bind(null,16)).then((function(t){var n=t.getCLS,a=t.getFID,s=t.getFCP,r=t.getLCP,c=t.getTTFB;n(e),a(e),s(e),r(e),c(e)}))}},9:function(e,t,n){"use strict";n.r(t);var a=n(2),s=n.n(a),r=(n(12),n(0));t.default=function(e){return Object(r.jsxs)(s.a.Fragment,{children:[Object(r.jsx)("header",{className:"header-bg py-15 position-fixed top-0 left-0 w-100",children:Object(r.jsx)("div",{className:"container",children:Object(r.jsx)("div",{className:"row",children:Object(r.jsx)("div",{className:"col-12",children:Object(r.jsx)("h1",{className:"m-0",children:Object(r.jsx)("img",{src:window.basePath+"/img/white-logo.png",alt:""})})})})})}),Object(r.jsx)("div",{className:"container container-error-page d-flex align-items-center justify-content-center",children:Object(r.jsx)("div",{className:"row",children:Object(r.jsx)("div",{className:"col-12",children:Object(r.jsxs)("div",{className:"error-content text-center",children:[Object(r.jsx)("h1",{className:"text-black font-weight-medium font-size-55 mb-10",children:"Oops!"}),Object(r.jsx)("p",{className:"text-primary-lightest font-size-30 font-weight-normal",children:"We can't find the page you are looking for."})]})})})})]})}}},[[43,19,0]]]);