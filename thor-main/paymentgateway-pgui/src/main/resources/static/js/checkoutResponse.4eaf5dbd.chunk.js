(this.webpackJsonpfrontend=this.webpackJsonpfrontend||[]).push([[2,30,31],{11:function(e,t){window.basePath="/pgui",window.querySelector=document.querySelector.bind(document),window.querySelectorAll=document.querySelectorAll.bind(document),window.id=document.getElementById.bind(document),window.byClass=document.getElementsByClassName.bind(document),window.createElement=document.createElement.bind(document),window.worker="",window.oid="",window.upiQrCodeResponse="",window.mqrCodeResponse="",window.worker=void 0,window.walletToCompare="PaytmWallet",window.addMoneyTimer=null},12:function(e,t,n){},13:function(e,t,n){},3:function(e,t,n){"use strict";function o(e,t){if(!(e instanceof t))throw new TypeError("Cannot call a class as a function")}n.d(t,"a",(function(){return o}))},39:function(e,t,n){e.exports=n(63)},4:function(e,t,n){"use strict";function o(e,t){for(var n=0;n<t.length;n++){var o=t[n];o.enumerable=o.enumerable||!1,o.configurable=!0,"value"in o&&(o.writable=!0),Object.defineProperty(e,o.key,o)}}function r(e,t,n){return t&&o(e.prototype,t),n&&o(e,n),e}n.d(t,"a",(function(){return r}))},5:function(e,t,n){"use strict";function o(e){return(o=Object.setPrototypeOf?Object.getPrototypeOf:function(e){return e.__proto__||Object.getPrototypeOf(e)})(e)}function r(e){return(r="function"===typeof Symbol&&"symbol"===typeof Symbol.iterator?function(e){return typeof e}:function(e){return e&&"function"===typeof Symbol&&e.constructor===Symbol&&e!==Symbol.prototype?"symbol":typeof e})(e)}function c(e,t){return!t||"object"!==r(t)&&"function"!==typeof t?function(e){if(void 0===e)throw new ReferenceError("this hasn't been initialised - super() hasn't been called");return e}(e):t}function i(e){var t=function(){if("undefined"===typeof Reflect||!Reflect.construct)return!1;if(Reflect.construct.sham)return!1;if("function"===typeof Proxy)return!0;try{return Date.prototype.toString.call(Reflect.construct(Date,[],(function(){}))),!0}catch(e){return!1}}();return function(){var n,r=o(e);if(t){var i=o(this).constructor;n=Reflect.construct(r,arguments,i)}else n=r.apply(this,arguments);return c(this,n)}}n.d(t,"a",(function(){return i}))},6:function(e,t,n){"use strict";function o(e,t){return(o=Object.setPrototypeOf||function(e,t){return e.__proto__=t,e})(e,t)}function r(e,t){if("function"!==typeof t&&null!==t)throw new TypeError("Super expression must either be null or a function");e.prototype=Object.create(t&&t.prototype,{constructor:{value:e,writable:!0,configurable:!0}}),t&&o(e,t)}n.d(t,"a",(function(){return r}))},63:function(e,t,n){"use strict";n.r(t);var o=n(2),r=n.n(o),c=n(10),i=n.n(c),a=(n(11),n(8)),s=n(3),l=n(4),u=n(6),d=n(5),f=n(7),p=n(9),m=n(0),b=function(e){Object(u.a)(n,e);var t=Object(d.a)(n);function n(){var e;Object(s.a)(this,n);for(var o=arguments.length,r=new Array(o),c=0;c<o;c++)r[c]=arguments[c];return(e=t.call.apply(t,[this].concat(r))).state={data:null},e}return Object(l.a)(n,[{key:"componentDidMount",value:function(){try{var e=window.id("sessionObj").value;void 0!==(e=JSON.parse(e)).RESPONSE_CODE?window.location!==window.parent.location?(e.closeIframe=!0,window.parent.postMessage(e,"*"),window.close()):void 0!==window.opener&&(e.closeIframe=!0,window.opener.postMessage(e,"*"),window.close()):void 0!==e.ENCDATA?window.parent.postMessage(e,"*"):this.setState({data:"error"})}catch(t){this.setState({data:"error"}),console.error(t)}}},{key:"render",value:function(){var e=Object(m.jsx)(f.default,{processing:!1,approvalNotification:!1});return null!==this.state.data&&"error"==this.state.data&&(e=Object(m.jsx)(p.default,{})),Object(m.jsx)(m.Fragment,{children:e})}}]),n}(o.Component);i.a.render(Object(m.jsx)(r.a.StrictMode,{children:Object(m.jsx)(b,{})}),document.getElementById("root")),Object(a.a)()},7:function(e,t,n){"use strict";n.r(t);n(13);var o=n(0);t.default=function(e){return Object(o.jsx)("div",{className:"loader loader2",id:"loading2",children:Object(o.jsxs)("div",{className:"w-100 vh-100 d-flex justify-content-center align-items-center flex-column",children:[Object(o.jsx)("div",{className:"loaderImage",children:Object(o.jsx)("img",{src:window.basePath+"/img/loader.gif",alt:"Loader"})}),e.approvalNotification?Object(o.jsxs)("div",{id:"approvedNotification",className:"approvedNotification text-center",children:[Object(o.jsx)("h3",{className:"lang","data-key":"upiApprovalText",children:"Please approve the payment in your UPI App"}),Object(o.jsx)("p",{className:"lang","data-key":"upiStopRefresh",children:"Do not refresh this page or press back button"})]}):null,e.processing?Object(o.jsx)("div",{id:"loading2Loader",className:"defaultText mt-10 text-center",children:Object(o.jsx)("h3",{className:"lang","data-key":"defaultLoaderText",children:"Please wait while we process your payment..."})}):null]})})}},8:function(e,t,n){"use strict";t.a=function(e){e&&e instanceof Function&&n.e(32).then(n.bind(null,16)).then((function(t){var n=t.getCLS,o=t.getFID,r=t.getFCP,c=t.getLCP,i=t.getTTFB;n(e),o(e),r(e),c(e),i(e)}))}},9:function(e,t,n){"use strict";n.r(t);var o=n(2),r=n.n(o),c=(n(12),n(0));t.default=function(e){return Object(c.jsxs)(r.a.Fragment,{children:[Object(c.jsx)("header",{className:"header-bg py-15 position-fixed top-0 left-0 w-100",children:Object(c.jsx)("div",{className:"container",children:Object(c.jsx)("div",{className:"row",children:Object(c.jsx)("div",{className:"col-12",children:Object(c.jsx)("h1",{className:"m-0",children:Object(c.jsx)("img",{src:window.basePath+"/img/white-logo.png",alt:""})})})})})}),Object(c.jsx)("div",{className:"container container-error-page d-flex align-items-center justify-content-center",children:Object(c.jsx)("div",{className:"row",children:Object(c.jsx)("div",{className:"col-12",children:Object(c.jsxs)("div",{className:"error-content text-center",children:[Object(c.jsx)("h1",{className:"text-black font-weight-medium font-size-55 mb-10",children:"Oops!"}),Object(c.jsx)("p",{className:"text-primary-lightest font-size-30 font-weight-normal",children:"We can't find the page you are looking for."})]})})})})]})}}},[[39,12,0]]]);