/*!
 Buttons for DataTables 1.1.0
 Â©2015 SpryMedia Ltd - datatables.net/license
*/
//src="https://cdn.datatables.net/buttons/1.1.0/js/dataTables.buttons.min.js"
//src="https://cdn.datatables.net/buttons/1.1.0/js/buttons.html5.min.js"
//src="http://cdn.datatables.net/buttons/1.1.0/js/buttons.print.min.js"
//src="https://cdn.datatables.net/buttons/1.1.0/js/buttons.colVis.min.js"

(function(e){"function"===typeof define&&define.amd?define(["jquery","datatables.net"],function(n){return e(n,window,document)}):"object"===typeof exports?module.exports=function(n,o){n||(n=window);if(!o||!o.fn.dataTable)o=require("datatables.net")(n,o).$;return e(o,n,n.document)}:e(jQuery,window,document)})(function(e,n,o,m){var j=e.fn.dataTable,s=0,t=0,k=j.ext.buttons,l=function(a,b){!0===b&&(b={});e.isArray(b)&&(b={buttons:b});this.c=e.extend(!0,{},l.defaults,b);b.buttons&&(this.c.buttons=b.buttons);
this.s={dt:new j.Api(a),buttons:[],subButtons:[],listenKeys:"",namespace:"dtb"+s++};this.dom={container:e("<"+this.c.dom.container.tag+"/>").addClass(this.c.dom.container.className)};this._constructor()};e.extend(l.prototype,{action:function(a,b){var c=this._indexToButton(a).conf;if(b===m)return c.action;c.action=b;return this},active:function(a,b){this._indexToButton(a).node.toggleClass(this.c.dom.button.active,b===m?!0:b);return this},add:function(a,b){if("string"===typeof a&&-1!==a.indexOf("-")){var c=
a.split("-");this.c.buttons[1*c[0]].buttons.splice(1*c[1],0,b)}else this.c.buttons.splice(1*a,0,b);this.dom.container.empty();this._buildButtons(this.c.buttons);return this},container:function(){return this.dom.container},disable:function(a){this._indexToButton(a).node.addClass(this.c.dom.button.disabled);return this},destroy:function(){e("body").off("keyup."+this.s.namespace);var a=this.s.buttons,b=this.s.subButtons,c,d,f;c=0;for(a=a.length;c<a;c++){this.removePrep(c);d=0;for(f=b[c].length;d<f;d++)this.removePrep(c+
"-"+d)}this.removeCommit();this.dom.container.remove();b=this.s.dt.settings()[0];c=0;for(a=b.length;c<a;c++)if(b.inst===this){b.splice(c,1);break}return this},enable:function(a,b){if(!1===b)return this.disable(a);this._indexToButton(a).node.removeClass(this.c.dom.button.disabled);return this},name:function(){return this.c.name},node:function(a){return this._indexToButton(a).node},removeCommit:function(){var a=this.s.buttons,b=this.s.subButtons,c,d;for(c=a.length-1;0<=c;c--)null===a[c]&&(a.splice(c,
1),b.splice(c,1),this.c.buttons.splice(c,1));c=0;for(a=b.length;c<a;c++)for(d=b[c].length-1;0<=d;d--)null===b[c][d]&&(b[c].splice(d,1),this.c.buttons[c].buttons.splice(d,1));return this},removePrep:function(a){var b,c=this.s.dt;if("number"===typeof a||-1===a.indexOf("-"))b=this.s.buttons[1*a],b.conf.destroy&&b.conf.destroy.call(c.button(a),c,b,b.conf),b.node.remove(),this._removeKey(b.conf),this.s.buttons[1*a]=null;else{var d=a.split("-");b=this.s.subButtons[1*d[0]][1*d[1]];b.conf.destroy&&b.conf.destroy.call(c.button(a),
c,b,b.conf);b.node.remove();this._removeKey(b.conf);this.s.subButtons[1*d[0]][1*d[1]]=null}return this},text:function(a,b){var c=this._indexToButton(a),d=this.c.dom.collection.buttonLiner,d="string"===typeof a&&-1!==a.indexOf("-")&&d&&d.tag?d.tag:this.c.dom.buttonLiner.tag,e=this.s.dt,g=function(a){return"function"===typeof a?a(e,c.node,c.conf):a};if(b===m)return g(c.conf.text);c.conf.text=b;d?c.node.children(d).html(g(b)):c.node.html(g(b));return this},toIndex:function(a){var b,c,d,e;d=this.s.buttons;
var g=this.s.subButtons;b=0;for(c=d.length;b<c;b++)if(d[b].node[0]===a)return b+"";b=0;for(c=g.length;b<c;b++){d=0;for(e=g[b].length;d<e;d++)if(g[b][d].node[0]===a)return b+"-"+d}},_constructor:function(){var a=this,b=this.s.dt,c=b.settings()[0];c._buttons||(c._buttons=[]);c._buttons.push({inst:this,name:this.c.name});this._buildButtons(this.c.buttons);b.on("destroy",function(){a.destroy()});e("body").on("keyup."+this.s.namespace,function(b){if(!o.activeElement||o.activeElement===o.body){var c=String.fromCharCode(b.keyCode).toLowerCase();
a.s.listenKeys.toLowerCase().indexOf(c)!==-1&&a._keypress(c,b)}})},_addKey:function(a){a.key&&(this.s.listenKeys+=e.isPlainObject(a.key)?a.key.key:a.key)},_buildButtons:function(a,b,c){var d=this.s.dt;b||(b=this.dom.container,this.s.buttons=[],this.s.subButtons=[]);for(var f=0,g=a.length;f<g;f++){var h=this._resolveExtends(a[f]);if(h)if(e.isArray(h))this._buildButtons(h,b,c);else{var i=this._buildButton(h,c!==m?!0:!1);if(i){var q=i.node;b.append(i.inserter);c===m?(this.s.buttons.push({node:q,conf:h,
inserter:i.inserter}),this.s.subButtons.push([])):this.s.subButtons[c].push({node:q,conf:h,inserter:i.inserter});h.buttons&&(i=this.c.dom.collection,h._collection=e("<"+i.tag+"/>").addClass(i.className),this._buildButtons(h.buttons,h._collection,f));h.init&&h.init.call(d.button(q),d,q,h)}}}},_buildButton:function(a,b){var c=this.c.dom.button,d=this.c.dom.buttonLiner,f=this.c.dom.collection,g=this.s.dt,h=function(b){return"function"===typeof b?b(g,i,a):b};b&&f.button&&(c=f.button);b&&f.buttonLiner&&
(d=f.buttonLiner);if(a.available&&!a.available(g,a))return!1;var i=e("<"+c.tag+"/>").addClass(c.className).attr("tabindex",this.s.dt.settings()[0].iTabIndex).attr("aria-controls",this.s.dt.table().node().id).on("click.dtb",function(b){b.preventDefault();!i.hasClass(c.disabled)&&a.action&&a.action.call(g.button(i),b,g,i,a);i.blur()}).on("keyup.dtb",function(b){b.keyCode===13&&!i.hasClass(c.disabled)&&a.action&&a.action.call(g.button(i),b,g,i,a)});d.tag?i.append(e("<"+d.tag+"/>").html(h(a.text)).addClass(d.className)):
i.html(h(a.text));!1===a.enabled&&i.addClass(c.disabled);a.className&&i.addClass(a.className);a.titleAttr&&i.attr("title",a.titleAttr);a.namespace||(a.namespace=".dt-button-"+t++);d=(d=this.c.dom.buttonContainer)?e("<"+d.tag+"/>").addClass(d.className).append(i):i;this._addKey(a);return{node:i,inserter:d}},_indexToButton:function(a){if("number"===typeof a||-1===a.indexOf("-"))return this.s.buttons[1*a];a=a.split("-");return this.s.subButtons[1*a[0]][1*a[1]]},_keypress:function(a,b){var c,d,f,g;f=
this.s.buttons;var h=this.s.subButtons,i=function(c,d){if(c.key)if(c.key===a)d.click();else if(e.isPlainObject(c.key)&&c.key.key===a&&(!c.key.shiftKey||b.shiftKey))if(!c.key.altKey||b.altKey)if(!c.key.ctrlKey||b.ctrlKey)(!c.key.metaKey||b.metaKey)&&d.click()};c=0;for(d=f.length;c<d;c++)i(f[c].conf,f[c].node);c=0;for(d=h.length;c<d;c++){f=0;for(g=h[c].length;f<g;f++)i(h[c][f].conf,h[c][f].node)}},_removeKey:function(a){if(a.key){var b=e.isPlainObject(a.key)?a.key.key:a.key,a=this.s.listenKeys.split(""),
b=e.inArray(b,a);a.splice(b,1);this.s.listenKeys=a.join("")}},_resolveExtends:function(a){for(var b=this.s.dt,c,d,f=function(c){for(var d=0;!e.isPlainObject(c)&&!e.isArray(c);){if(c===m)return;if("function"===typeof c){if(c=c(b,a),!c)return!1}else if("string"===typeof c){if(!k[c])throw"Unknown button type: "+c;c=k[c]}d++;if(30<d)throw"Buttons: Too many iterations";}return e.isArray(c)?c:e.extend({},c)},a=f(a);a&&a.extend;){if(!k[a.extend])throw"Cannot extend unknown button type: "+a.extend;var g=
f(k[a.extend]);if(e.isArray(g))return g;if(!g)return!1;c=g.className;a=e.extend({},g,a);c&&a.className!==c&&(a.className=c+" "+a.className);var h=a.postfixButtons;if(h){a.buttons||(a.buttons=[]);c=0;for(d=h.length;c<d;c++)a.buttons.push(h[c]);a.postfixButtons=null}if(h=a.prefixButtons){a.buttons||(a.buttons=[]);c=0;for(d=h.length;c<d;c++)a.buttons.splice(c,0,h[c]);a.prefixButtons=null}a.extend=g.extend}return a}});l.background=function(a,b,c){c===m&&(c=400);a?e("<div/>").addClass(b).css("display",
"none").appendTo("body").fadeIn(c):e("body > div."+b).fadeOut(c,function(){e(this).remove()})};l.instanceSelector=function(a,b){if(!a)return e.map(b,function(a){return a.inst});var c=[],d=e.map(b,function(a){return a.name}),f=function(a){if(e.isArray(a))for(var h=0,i=a.length;h<i;h++)f(a[h]);else"string"===typeof a?-1!==a.indexOf(",")?f(a.split(",")):(a=e.inArray(e.trim(a),d),-1!==a&&c.push(b[a].inst)):"number"===typeof a&&c.push(b[a].inst)};f(a);return c};l.buttonSelector=function(a,b){for(var c=
[],d=function(a,b){var f,g,j=[];e.each(b.s.buttons,function(a,b){null!==b&&j.push({node:b.node[0],name:b.name})});e.each(b.s.subButtons,function(a,b){e.each(b,function(a,b){null!==b&&j.push({node:b.node[0],name:b.name})})});f=e.map(j,function(a){return a.node});if(e.isArray(a)||a instanceof e){f=0;for(g=a.length;f<g;f++)d(a[f],b)}else if(null===a||a===m||"*"===a){f=0;for(g=j.length;f<g;f++)c.push({inst:b,idx:b.toIndex(j[f].node)})}else if("number"===typeof a)c.push({inst:b,idx:a});else if("string"===
typeof a)if(-1!==a.indexOf(",")){var k=a.split(",");f=0;for(g=k.length;f<g;f++)d(e.trim(k[f]),b)}else if(a.match(/^\d+(\-\d+)?$/))c.push({inst:b,idx:a});else if(-1!==a.indexOf(":name")){k=a.replace(":name","");f=0;for(g=j.length;f<g;f++)j[f].name===k&&c.push({inst:b,idx:b.toIndex(j[f].node)})}else e(f).filter(a).each(function(){c.push({inst:b,idx:b.toIndex(this)})});else"object"===typeof a&&a.nodeName&&(g=e.inArray(a,f),-1!==g&&c.push({inst:b,idx:b.toIndex(f[g])}))},f=0,g=a.length;f<g;f++)d(b,a[f]);
return c};l.defaults={buttons:["copy","excel","csv","pdf","print"],name:"main",tabIndex:0,dom:{container:{tag:"div",className:"dt-buttons"},collection:{tag:"div",className:"dt-button-collection"},button:{tag:"a",className:"dt-button",active:"active",disabled:"disabled"},buttonLiner:{tag:"span",className:""}}};l.version="1.1.0";e.extend(k,{collection:{text:function(a){return a.i18n("buttons.collection","Collection")},className:"buttons-collection",action:function(a,b,c,d){var a=c.offset(),b=e(b.table().container()),
f=!1;e("div.dt-button-background").length&&(f=e("div.dt-button-collection").offset(),e(o).trigger("click.dtb-collection"));d._collection.addClass(d.collectionLayout).css("display","none").appendTo("body").fadeIn(d.fade);var g=d._collection.css("position");f&&"absolute"===g?d._collection.css({top:f.top+5,left:f.left+5}):"absolute"===g?(d._collection.css({top:a.top+c.outerHeight(),left:a.left}),c=a.left+d._collection.outerWidth(),b=b.offset().left+b.width(),c>b&&d._collection.css("left",a.left-(c-b))):
(a=d._collection.height()/2,a>e(n).height()/2&&(a=e(n).height()/2),d._collection.css("marginTop",-1*a));d.background&&l.background(!0,d.backgroundClassName,d.fade);setTimeout(function(){e("div.dt-button-background").on("click.dtb-collection",function(){});e("body").on("click.dtb-collection",function(a){if(!e(a.target).parents().andSelf().filter(d._collection).length){d._collection.fadeOut(d.fade,function(){d._collection.detach()});e("div.dt-button-background").off("click.dtb-collection");l.background(false,
d.backgroundClassName,d.fade);e("body").off("click.dtb-collection")}})},10)},background:!0,collectionLayout:"",backgroundClassName:"dt-button-background",fade:400},copy:function(a,b){if(k.copyHtml5)return"copyHtml5";if(k.copyFlash&&k.copyFlash.available(a,b))return"copyFlash"},csv:function(a,b){if(k.csvHtml5&&k.csvHtml5.available(a,b))return"csvHtml5";if(k.csvFlash&&k.csvFlash.available(a,b))return"csvFlash"},excel:function(a,b){if(k.excelHtml5&&k.excelHtml5.available(a,b))return"excelHtml5";if(k.excelFlash&&
k.excelFlash.available(a,b))return"excelFlash"},pdf:function(a,b){if(k.pdfHtml5&&k.pdfHtml5.available(a,b))return"pdfHtml5";if(k.pdfFlash&&k.pdfFlash.available(a,b))return"pdfFlash"},pageLength:function(a){var a=a.settings()[0].aLengthMenu,b=e.isArray(a[0])?a[0]:a,c=e.isArray(a[0])?a[1]:a,d=function(a){return a.i18n("buttons.pageLength",{"-1":"Show all rows",_:"Show %d rows"},a.page.len())};return{extend:"collection",text:d,className:"buttons-page-length",buttons:e.map(b,function(a,b){return{text:c[b],
action:function(b,c){c.page.len(a).draw()},init:function(b,c,d){var e=this,c=function(){e.active(b.page.len()===a)};b.on("length.dt"+d.namespace,c);c()},destroy:function(a,b,c){a.off("length.dt"+c.namespace)}}}),init:function(a,b,c){var e=this;a.on("length.dt"+c.namespace,function(){e.text(d(a))})},destroy:function(a,b,c){a.off("length.dt"+c.namespace)}}}});j.Api.register("buttons()",function(a,b){b===m&&(b=a,a=m);return this.iterator(!0,"table",function(c){if(c._buttons)return l.buttonSelector(l.instanceSelector(a,
c._buttons),b)},!0)});j.Api.register("button()",function(a,b){var c=this.buttons(a,b);1<c.length&&c.splice(1,c.length);return c});j.Api.register(["buttons().active()","button().active()"],function(a){return this.each(function(b){b.inst.active(b.idx,a)})});j.Api.registerPlural("buttons().action()","button().action()",function(a){return a===m?this.map(function(a){return a.inst.action(a.idx)}):this.each(function(b){b.inst.action(b.idx,a)})});j.Api.register(["buttons().enable()","button().enable()"],
function(a){return this.each(function(b){b.inst.enable(b.idx,a)})});j.Api.register(["buttons().disable()","button().disable()"],function(){return this.each(function(a){a.inst.disable(a.idx)})});j.Api.registerPlural("buttons().nodes()","button().node()",function(){var a=e();e(this.each(function(b){a=a.add(b.inst.node(b.idx))}));return a});j.Api.registerPlural("buttons().text()","button().text()",function(a){return a===m?this.map(function(a){return a.inst.text(a.idx)}):this.each(function(b){b.inst.text(b.idx,
a)})});j.Api.registerPlural("buttons().trigger()","button().trigger()",function(){return this.each(function(a){a.inst.node(a.idx).trigger("click")})});j.Api.registerPlural("buttons().containers()","buttons().container()",function(){var a=e();e(this.each(function(b){a=a.add(b.inst.container())}));return a});j.Api.register("button().add()",function(a,b){1===this.length&&this[0].inst.add(a,b);return this.button(a)});j.Api.register("buttons().destroy()",function(){this.pluck("inst").unique().each(function(a){a.destroy()});
return this});j.Api.registerPlural("buttons().remove()","buttons().remove()",function(){this.each(function(a){a.inst.removePrep(a.idx)});this.pluck("inst").unique().each(function(a){a.removeCommit()});return this});var p;j.Api.register("buttons.info()",function(a,b,c){var d=this;if(!1===a)return e("#datatables_buttons_info").fadeOut(function(){e(this).remove()}),clearTimeout(p),p=null,this;p&&clearTimeout(p);e("#datatables_buttons_info").length&&e("#datatables_buttons_info").remove();e('<div id="datatables_buttons_info" class="dt-button-info"/>').html(a?
"<h2>"+a+"</h2>":"").append(e("<div/>")["string"===typeof b?"html":"append"](b)).css("display","none").appendTo("body").fadeIn();c!==m&&0!==c&&(p=setTimeout(function(){d.buttons.info(!1)},c));return this});j.Api.register("buttons.exportData()",function(a){if(this.context.length){for(var b=new j.Api(this.context[0]),c=e.extend(!0,{},{rows:null,columns:"",modifier:{search:"applied",order:"applied"},orthogonal:"display",stripHtml:!0,stripNewlines:!0,decodeEntities:!0,trim:!0,format:{header:function(a){return d(a)},
footer:function(a){return d(a)},body:function(a){return d(a)}}},a),d=function(a){if("string"!==typeof a)return a;c.stripHtml&&(a=a.replace(/<.*?>/g,""));c.trim&&(a=a.replace(/^\s+|\s+$/g,""));c.stripNewlines&&(a=a.replace(/\n/g," "));c.decodeEntities&&(r.innerHTML=a,a=r.value);return a},a=b.columns(c.columns).indexes().map(function(a){return c.format.header(b.column(a).header().innerHTML,a)}).toArray(),f=b.table().footer()?b.columns(c.columns).indexes().map(function(a){var d=b.column(a).footer();
return c.format.footer(d?d.innerHTML:"",a)}).toArray():null,g=b.rows(c.rows,c.modifier).indexes().toArray(),g=b.cells(g,c.columns).render(c.orthogonal).toArray(),h=a.length,i=0<h?g.length/h:0,k=Array(i),l=0,m=0;m<i;m++){for(var o=Array(h),n=0;n<h;n++)o[n]=c.format.body(g[l],n,m),l++;k[m]=o}return{header:a,footer:f,body:k}}});var r=e("<textarea/>")[0];e.fn.dataTable.Buttons=l;e.fn.DataTable.Buttons=l;e(o).on("init.dt plugin-init.dt",function(a,b){if("dt"===a.namespace){var c=b.oInit.buttons||j.defaults.buttons;
c&&!b._buttons&&(new l(b,c)).container()}});j.ext.feature.push({fnInit:function(a){var a=new j.Api(a),b=a.init().buttons||j.defaults.buttons;return(new l(a,b)).container()},cFeature:"B"});return l});


(function(g){"function"===typeof define&&define.amd?define(["jquery","datatables.net","datatables.net-buttons"],function(d){return g(d,window,document)}):"object"===typeof exports?module.exports=function(d,f){d||(d=window);if(!f||!f.fn.dataTable)f=require("datatables.net")(d,f).$;f.fn.dataTable.Buttons||require("datatables.net-buttons")(d,f);return g(f,d,d.document)}:g(jQuery,window,document)})(function(g,d,f,k){var l=g.fn.dataTable,j;if("undefined"!==typeof navigator&&/MSIE [1-9]\./.test(navigator.userAgent))j=
	void 0;else{var v=d.document,o=v.createElementNS("http://www.w3.org/1999/xhtml","a"),D="download"in o,p=d.webkitRequestFileSystem,w=d.requestFileSystem||p||d.mozRequestFileSystem,E=function(a){(d.setImmediate||d.setTimeout)(function(){throw a;},0)},q=0,r=function(a){var b=function(){"string"===typeof a?(d.URL||d.webkitURL||d).revokeObjectURL(a):a.remove()};d.chrome?b():setTimeout(b,500)},s=function(a,b,e){for(var b=[].concat(b),c=b.length;c--;){var d=a["on"+b[c]];if("function"===typeof d)try{d.call(a,
	e||a)}catch(h){E(h)}}},y=function(a){return/^\s*(?:text\/\S*|application\/xml|\S*\/\S*\+xml)\s*;.*charset\s*=\s*utf-8/i.test(a.type)?new Blob(["ï»¿",a],{type:a.type}):a},A=function(a,b){var a=y(a),e=this,c=a.type,x=!1,h,g,z=function(){s(e,["writestart","progress","write","writeend"])},f=function(){if(x||!h)h=(d.URL||d.webkitURL||d).createObjectURL(a);g?g.location.href=h:d.open(h,"_blank")===k&&"undefined"!==typeof safari&&(d.location.href=h);e.readyState=e.DONE;z();r(h)},n=function(a){return function(){if(e.readyState!==
	e.DONE)return a.apply(this,arguments)}},i={create:!0,exclusive:!1},j;e.readyState=e.INIT;b||(b="download");if(D)h=(d.URL||d.webkitURL||d).createObjectURL(a),o.href=h,o.download=b,c=v.createEvent("MouseEvents"),c.initMouseEvent("click",!0,!1,d,0,0,0,0,0,!1,!1,!1,!1,0,null),o.dispatchEvent(c),e.readyState=e.DONE,z(),r(h);else{d.chrome&&(c&&"application/octet-stream"!==c)&&(j=a.slice||a.webkitSlice,a=j.call(a,0,a.size,"application/octet-stream"),x=!0);p&&"download"!==b&&(b+=".download");if("application/octet-stream"===
	c||p)g=d;w?(q+=a.size,w(d.TEMPORARY,q,n(function(c){c.root.getDirectory("saved",i,n(function(c){var d=function(){c.getFile(b,i,n(function(b){b.createWriter(n(function(c){c.onwriteend=function(a){g.location.href=b.toURL();e.readyState=e.DONE;s(e,"writeend",a);r(b)};c.onerror=function(){var a=c.error;a.code!==a.ABORT_ERR&&f()};["writestart","progress","write","abort"].forEach(function(a){c["on"+a]=e["on"+a]});c.write(a);e.abort=function(){c.abort();e.readyState=e.DONE};e.readyState=e.WRITING}),f)}),
	f)};c.getFile(b,{create:false},n(function(a){a.remove();d()}),n(function(a){a.code===a.NOT_FOUND_ERR?d():f()}))}),f)}),f)):f()}},i=A.prototype;"undefined"!==typeof navigator&&navigator.msSaveOrOpenBlob?j=function(a,b){return navigator.msSaveOrOpenBlob(y(a),b)}:(i.abort=function(){this.readyState=this.DONE;s(this,"abort")},i.readyState=i.INIT=0,i.WRITING=1,i.DONE=2,i.error=i.onwritestart=i.onprogress=i.onwrite=i.onabort=i.onerror=i.onwriteend=null,j=function(a,b){return new A(a,b)})}var t=function(a,
	b){var e="*"===a.filename&&"*"!==a.title&&a.title!==k?a.title:a.filename;-1!==e.indexOf("*")&&(e=e.replace("*",g("title").text()));e=e.replace(/[^a-zA-Z0-9_\u00A1-\uFFFF\.,\-_ !\(\)]/g,"");return b===k||!0===b?e+a.extension:e},F=function(a){a=a.title;return-1!==a.indexOf("*")?a.replace("*",g("title").text()):a},u=function(a){return a.newline?a.newline:navigator.userAgent.match(/Windows/)?"\r\n":"\n"},B=function(a,b){for(var e=u(b),c=a.buttons.exportData(b.exportOptions),d=b.fieldBoundary,h=b.fieldSeparator,
	f=RegExp(d,"g"),g=b.escapeChar!==k?b.escapeChar:"\\",i=function(a){for(var b="",c=0,e=a.length;c<e;c++)0<c&&(b+=h),b+=d?d+(""+a[c]).replace(f,g+d)+d:a[c];return b},n=b.header?i(c.header)+e:"",j=b.footer?e+i(c.footer):"",l=[],m=0,o=c.body.length;m<o;m++)l.push(i(c.body[m]));return{str:n+l.join(e)+j,rows:l.length}},C=function(){return-1!==navigator.userAgent.indexOf("Safari")&&-1===navigator.userAgent.indexOf("Chrome")&&-1===navigator.userAgent.indexOf("Opera")},m={"_rels/.rels":'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">\t<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>',
	"xl/_rels/workbook.xml.rels":'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">\t<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/></Relationships>',"[Content_Types].xml":'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">\t<Default Extension="xml" ContentType="application/xml"/>\t<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>\t<Default Extension="jpeg" ContentType="image/jpeg"/>\t<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>\t<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/></Types>',
	"xl/workbook.xml":'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">\t<fileVersion appName="xl" lastEdited="5" lowestEdited="5" rupBuild="24816"/>\t<workbookPr showInkAnnotation="0" autoCompressPictures="0"/>\t<bookViews>\t\t<workbookView xWindow="0" yWindow="0" windowWidth="25600" windowHeight="19020" tabRatio="500"/>\t</bookViews>\t<sheets>\t\t<sheet name="Sheet1" sheetId="1" r:id="rId1"/>\t</sheets></workbook>',
	"xl/worksheets/sheet1.xml":'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006" mc:Ignorable="x14ac" xmlns:x14ac="http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac">\t<sheetData>\t\t__DATA__\t</sheetData></worksheet>'};l.ext.buttons.copyHtml5={className:"buttons-copy buttons-html5",
	text:function(a){return a.i18n("buttons.copy","Copy")},action:function(a,b,d,c){a=B(b,c);c=a.str;d=g("<div/>").css({height:1,width:1,overflow:"hidden",position:"fixed",top:0,left:0});c=g("<textarea readonly/>").val(c).appendTo(d);if(f.queryCommandSupported("copy")){d.appendTo("body");c[0].focus();c[0].select();try{f.execCommand("copy");d.remove();b.buttons.info(b.i18n("buttons.copyTitle","Copy to clipboard"),b.i18n("buttons.copySuccess",{1:"Copied one row to clipboard",_:"Copied %d rows to clipboard"},
	a.rows),2E3);return}catch(i){}}a=g("<span>"+b.i18n("buttons.copyKeys","Press <i>ctrl</i> or <i>âŒ˜</i> + <i>C</i> to copy the table data<br>to your system clipboard.<br><br>To cancel, click this message or press escape.")+"</span>").append(d);b.buttons.info(b.i18n("buttons.copyTitle","Copy to clipboard"),a,0);c[0].focus();c[0].select();var h=g(a).closest(".dt-button-info"),j=function(){h.off("click.buttons-copy");g(f).off(".buttons-copy");b.buttons.info(!1)};h.on("click.buttons-copy",j);g(f).on("keydown.buttons-copy",
	function(a){27===a.keyCode&&j()}).on("copy.buttons-copy cut.buttons-copy",function(){j()})},exportOptions:{},fieldSeparator:"\t",fieldBoundary:"",header:!0,footer:!1};l.ext.buttons.csvHtml5={className:"buttons-csv buttons-html5",available:function(){return d.FileReader!==k&&d.Blob},text:function(a){return a.i18n("buttons.csv","CSV")},action:function(a,b,d,c){u(c);a=B(b,c).str;b=c.charset;!1!==b?(b||(b=f.characterSet||f.charset),b&&(b=";charset="+b)):b="";j(new Blob([a],{type:"text/csv"+b}),t(c))},
	filename:"*",extension:".csv",exportOptions:{},fieldSeparator:",",fieldBoundary:'"',escapeChar:'"',charset:null,header:!0,footer:!1};l.ext.buttons.excelHtml5={className:"buttons-excel buttons-html5",available:function(){return d.FileReader!==k&&d.JSZip!==k&&!C()},text:function(a){return a.i18n("buttons.excel","Excel")},action:function(a,b,e,c){a="";b=b.buttons.exportData(c.exportOptions);e=function(a){for(var b=[],c=0,d=a.length;c<d;c++){if(null===a[c]||a[c]===k)a[c]="";b.push("number"===typeof a[c]||
	a[c].match&&a[c].match(/^-?[0-9\.]+$/)&&"0"!==a[c].charAt(0)?'<c t="n"><v>'+a[c]+"</v></c>":'<c t="inlineStr"><is><t>'+(!a[c].replace?a[c]:a[c].replace(/&(?!amp;)/g,"&amp;").replace(/[\x00-\x1F\x7F-\x9F]/g,""))+"</t></is></c>")}return"<row>"+b.join("")+"</row>"};c.header&&(a+=e(b.header));for(var f=0,h=b.body.length;f<h;f++)a+=e(b.body[f]);c.footer&&(a+=e(b.footer));var b=new d.JSZip,e=b.folder("_rels"),f=b.folder("xl"),h=b.folder("xl/_rels"),g=b.folder("xl/worksheets");b.file("[Content_Types].xml",
	m["[Content_Types].xml"]);e.file(".rels",m["_rels/.rels"]);f.file("workbook.xml",m["xl/workbook.xml"]);h.file("workbook.xml.rels",m["xl/_rels/workbook.xml.rels"]);g.file("sheet1.xml",m["xl/worksheets/sheet1.xml"].replace("__DATA__",a));j(b.generate({type:"blob"}),t(c))},filename:"*",extension:".xlsx",exportOptions:{},header:!0,footer:!1};l.ext.buttons.pdfHtml5={className:"buttons-pdf buttons-html5",available:function(){return d.FileReader!==k&&d.pdfMake},text:function(a){return a.i18n("buttons.pdf",
	"PDF")},action:function(a,b,e,c){u(c);a=b.buttons.exportData(c.exportOptions);b=[];c.header&&b.push(g.map(a.header,function(a){return{text:"string"===typeof a?a:a+"",style:"tableHeader"}}));for(var f=0,e=a.body.length;f<e;f++)b.push(g.map(a.body[f],function(a){return{text:"string"===typeof a?a:a+"",style:f%2?"tableBodyEven":"tableBodyOdd"}}));c.footer&&b.push(g.map(a.footer,function(a){return{text:"string"===typeof a?a:a+"",style:"tableFooter"}}));a={pageSize:c.pageSize,pageOrientation:c.orientation,
	content:[{table:{headerRows:1,body:b},layout:"noBorders"}],styles:{tableHeader:{bold:!0,fontSize:11,color:"white",fillColor:"#2d4154",alignment:"center"},tableBodyEven:{},tableBodyOdd:{fillColor:"#f3f3f3"},tableFooter:{bold:!0,fontSize:11,color:"white",fillColor:"#2d4154"},title:{alignment:"center",fontSize:15},message:{}},defaultStyle:{fontSize:10}};c.message&&a.content.unshift({text:c.message,style:"message",margin:[0,0,0,12]});c.title&&a.content.unshift({text:F(c,!1),style:"title",margin:[0,0,
	0,12]});c.customize&&c.customize(a);a=d.pdfMake.createPdf(a);"open"===c.download&&!C()?a.open():a.getBuffer(function(a){a=new Blob([a],{type:"application/pdf"});j(a,t(c))})},title:"*",filename:"*",extension:".pdf",exportOptions:{},orientation:"portrait",pageSize:"A4",header:!0,footer:!1,message:null,customize:null,download:"download"};return l.Buttons});

(function(d){"function"===typeof define&&define.amd?define(["jquery","datatables.net","datatables.net-buttons"],function(c){return d(c,window,document)}):"object"===typeof exports?module.exports=function(c,a){c||(c=window);if(!a||!a.fn.dataTable)a=require("datatables.net")(c,a).$;a.fn.dataTable.Buttons||require("datatables.net-buttons")(c,a);return d(a,c,c.document)}:d(jQuery,window,document)})(function(d,c,a){var h=d.fn.dataTable,f=a.createElement("a");h.ext.buttons.print={className:"buttons-print",
		text:function(d){return d.i18n("buttons.print","Print")},action:function(a,b,i,e){a=b.buttons.exportData(e.exportOptions);i=function(a,b){for(var d="<tr>",c=0,e=a.length;c<e;c++)d+="<"+b+">"+a[c]+"</"+b+">";return d+"</tr>"};b='<table class="'+b.table().node().className+'">';e.header&&(b+="<thead>"+i(a.header,"th")+"</thead>");for(var b=b+"<tbody>",j=0,h=a.body.length;j<h;j++)b+=i(a.body[j],"td");b+="</tbody>";e.footer&&(b+="<thead>"+i(a.footer,"th")+"</thead>");var g=c.open("",""),a=e.title.replace("*",
		d("title").text());g.document.close();var k="<title>"+a+"</title>";d("style, link").each(function(){var a=k,b=d(this).clone()[0],c;"link"===b.nodeName.toLowerCase()&&(f.href=b.href,c=f.host,-1===c.indexOf("/")&&0!==f.pathname.indexOf("/")&&(c+="/"),b.href=f.protocol+"//"+c+f.pathname+f.search);k=a+b.outerHTML});d(g.document.head).html(k);d(g.document.body).html("<h1>"+a+"</h1><div>"+e.message+"</div>"+b);e.customize&&e.customize(g);setTimeout(function(){e.autoPrint&&(g.print(),g.close())},250)},title:"*",
		message:"",exportOptions:{},header:!0,footer:!1,autoPrint:!0,customize:null};return h.Buttons});

(function(g){"function"===typeof define&&define.amd?define(["jquery","datatables.net","datatables.net-buttons"],function(d){return g(d,window,document)}):"object"===typeof exports?module.exports=function(d,e){d||(d=window);if(!e||!e.fn.dataTable)e=require("datatables.net")(d,e).$;e.fn.dataTable.Buttons||require("datatables.net-buttons")(d,e);return g(e,d,d.document)}:g(jQuery,window,document)})(function(g,d,e,h){d=g.fn.dataTable;g.extend(d.ext.buttons,{colvis:function(a,b){return{extend:"collection",
	text:function(c){return c.i18n("buttons.colvis","Customize Columns")},className:"buttons-colvis",buttons:[{extend:"columnsToggle",columns:b.columns}]}},columnsToggle:function(a,b){return a.columns(b.columns).indexes().map(function(c){return{extend:"columnToggle",columns:c}}).toArray()},columnToggle:function(a,b){return{extend:"columnVisibility",columns:b.columns}},columnsVisibility:function(a,b){return a.columns(b.columns).indexes().map(function(c){return{extend:"columnVisibility",columns:c,visibility:b.visibility}}).toArray()},
	columnVisibility:{columns:h,text:function(a,b,c){return c._columnText(a,c.columns)},className:"buttons-columnVisibility",action:function(a,b,c,f){a=b.columns(f.columns);b=a.visible();a.visible(f.visibility!==h?f.visibility:!(b.length&&b[0]))},init:function(a,b,c){var f=this,b=a.column(c.columns);a.on("column-visibility.dt"+c.namespace,function(a,b,d,e){d===c.columns&&f.active(e)}).on("column-reorder.dt"+c.namespace,function(b,d,e){1===a.columns(c.columns).count()&&("number"===typeof c.columns&&(c.columns=
	e.mapping[c.columns]),b=a.column(c.columns),f.text(c._columnText(a,c.columns)),f.active(b.visible()))});this.active(b.visible())},destroy:function(a,b,c){a.off("column-visibility.dt"+c.namespace).off("column-reorder.dt"+c.namespace)},_columnText:function(a,b){var c=a.column(b).index();return a.settings()[0].aoColumns[c].sTitle.replace(/\n/g," ").replace(/<.*?>/g,"").replace(/^\s+|\s+$/g,"")}},colvisRestore:{className:"buttons-colvisRestore",text:function(a){return a.i18n("buttons.colvisRestore","Restore visibility")},
	init:function(a,b,c){c._visOriginal=a.columns().indexes().map(function(b){return a.column(b).visible()}).toArray()},action:function(a,b,c,d){b.columns().every(function(a){a=b.colReorder&&b.colReorder.transpose?b.colReorder.transpose(a,"toOriginal"):a;this.visible(d._visOriginal[a])})}},colvisGroup:{className:"buttons-colvisGroup",action:function(a,b,c,d){b.columns(d.show).visible(!0);b.columns(d.hide).visible(!1)},show:[],hide:[]}});return d.Buttons});
