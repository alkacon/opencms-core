(function(){/*! jQuery v1.7.1 jquery.com | jquery.org/license */
(function(a,b){function cy(a){return f.isWindow(a)?a:a.nodeType===9?a.defaultView||a.parentWindow:!1}function cv(a){if(!ck[a]){var b=c.body,d=f("<"+a+">").appendTo(b),e=d.css("display");d.remove();if(e==="none"||e===""){cl||(cl=c.createElement("iframe"),cl.frameBorder=cl.width=cl.height=0),b.appendChild(cl);if(!cm||!cl.createElement)cm=(cl.contentWindow||cl.contentDocument).document,cm.write((c.compatMode==="CSS1Compat"?"<!doctype html>":"")+"<html><body>"),cm.close();d=cm.createElement(a),cm.body.appendChild(d),e=f.css(d,"display"),b.removeChild(cl)}ck[a]=e}return ck[a]}function cu(a,b){var c={};f.each(cq.concat.apply([],cq.slice(0,b)),function(){c[this]=a});return c}function ct(){cr=b}function cs(){setTimeout(ct,0);return cr=f.now()}function cj(){try{return new a.ActiveXObject("Microsoft.XMLHTTP")}catch(b){}}function ci(){try{return new a.XMLHttpRequest}catch(b){}}function cc(a,c){a.dataFilter&&(c=a.dataFilter(c,a.dataType));var d=a.dataTypes,e={},g,h,i=d.length,j,k=d[0],l,m,n,o,p;for(g=1;g<i;g++){if(g===1)for(h in a.converters)typeof h=="string"&&(e[h.toLowerCase()]=a.converters[h]);l=k,k=d[g];if(k==="*")k=l;else if(l!=="*"&&l!==k){m=l+" "+k,n=e[m]||e["* "+k];if(!n){p=b;for(o in e){j=o.split(" ");if(j[0]===l||j[0]==="*"){p=e[j[1]+" "+k];if(p){o=e[o],o===!0?n=p:p===!0&&(n=o);break}}}}!n&&!p&&f.error("No conversion from "+m.replace(" "," to ")),n!==!0&&(c=n?n(c):p(o(c)))}}return c}function cb(a,c,d){var e=a.contents,f=a.dataTypes,g=a.responseFields,h,i,j,k;for(i in g)i in d&&(c[g[i]]=d[i]);while(f[0]==="*")f.shift(),h===b&&(h=a.mimeType||c.getResponseHeader("content-type"));if(h)for(i in e)if(e[i]&&e[i].test(h)){f.unshift(i);break}if(f[0]in d)j=f[0];else{for(i in d){if(!f[0]||a.converters[i+" "+f[0]]){j=i;break}k||(k=i)}j=j||k}if(j){j!==f[0]&&f.unshift(j);return d[j]}}function ca(a,b,c,d){if(f.isArray(b))f.each(b,function(b,e){c||bE.test(a)?d(a,e):ca(a+"["+(typeof e=="object"||f.isArray(e)?b:"")+"]",e,c,d)});else if(!c&&b!=null&&typeof b=="object")for(var e in b)ca(a+"["+e+"]",b[e],c,d);else d(a,b)}function b_(a,c){var d,e,g=f.ajaxSettings.flatOptions||{};for(d in c)c[d]!==b&&((g[d]?a:e||(e={}))[d]=c[d]);e&&f.extend(!0,a,e)}function b$(a,c,d,e,f,g){f=f||c.dataTypes[0],g=g||{},g[f]=!0;var h=a[f],i=0,j=h?h.length:0,k=a===bT,l;for(;i<j&&(k||!l);i++)l=h[i](c,d,e),typeof l=="string"&&(!k||g[l]?l=b:(c.dataTypes.unshift(l),l=b$(a,c,d,e,l,g)));(k||!l)&&!g["*"]&&(l=b$(a,c,d,e,"*",g));return l}function bZ(a){return function(b,c){typeof b!="string"&&(c=b,b="*");if(f.isFunction(c)){var d=b.toLowerCase().split(bP),e=0,g=d.length,h,i,j;for(;e<g;e++)h=d[e],j=/^\+/.test(h),j&&(h=h.substr(1)||"*"),i=a[h]=a[h]||[],i[j?"unshift":"push"](c)}}}function bC(a,b,c){var d=b==="width"?a.offsetWidth:a.offsetHeight,e=b==="width"?bx:by,g=0,h=e.length;if(d>0){if(c!=="border")for(;g<h;g++)c||(d-=parseFloat(f.css(a,"padding"+e[g]))||0),c==="margin"?d+=parseFloat(f.css(a,c+e[g]))||0:d-=parseFloat(f.css(a,"border"+e[g]+"Width"))||0;return d+"px"}d=bz(a,b,b);if(d<0||d==null)d=a.style[b]||0;d=parseFloat(d)||0;if(c)for(;g<h;g++)d+=parseFloat(f.css(a,"padding"+e[g]))||0,c!=="padding"&&(d+=parseFloat(f.css(a,"border"+e[g]+"Width"))||0),c==="margin"&&(d+=parseFloat(f.css(a,c+e[g]))||0);return d+"px"}function bp(a,b){b.src?f.ajax({url:b.src,async:!1,dataType:"script"}):f.globalEval((b.text||b.textContent||b.innerHTML||"").replace(bf,"/*$0*/")),b.parentNode&&b.parentNode.removeChild(b)}function bo(a){var b=c.createElement("div");bh.appendChild(b),b.innerHTML=a.outerHTML;return b.firstChild}function bn(a){var b=(a.nodeName||"").toLowerCase();b==="input"?bm(a):b!=="script"&&typeof a.getElementsByTagName!="undefined"&&f.grep(a.getElementsByTagName("input"),bm)}function bm(a){if(a.type==="checkbox"||a.type==="radio")a.defaultChecked=a.checked}function bl(a){return typeof a.getElementsByTagName!="undefined"?a.getElementsByTagName("*"):typeof a.querySelectorAll!="undefined"?a.querySelectorAll("*"):[]}function bk(a,b){var c;if(b.nodeType===1){b.clearAttributes&&b.clearAttributes(),b.mergeAttributes&&b.mergeAttributes(a),c=b.nodeName.toLowerCase();if(c==="object")b.outerHTML=a.outerHTML;else if(c!=="input"||a.type!=="checkbox"&&a.type!=="radio"){if(c==="option")b.selected=a.defaultSelected;else if(c==="input"||c==="textarea")b.defaultValue=a.defaultValue}else a.checked&&(b.defaultChecked=b.checked=a.checked),b.value!==a.value&&(b.value=a.value);b.removeAttribute(f.expando)}}function bj(a,b){if(b.nodeType===1&&!!f.hasData(a)){var c,d,e,g=f._data(a),h=f._data(b,g),i=g.events;if(i){delete h.handle,h.events={};for(c in i)for(d=0,e=i[c].length;d<e;d++)f.event.add(b,c+(i[c][d].namespace?".":"")+i[c][d].namespace,i[c][d],i[c][d].data)}h.data&&(h.data=f.extend({},h.data))}}function bi(a,b){return f.nodeName(a,"table")?a.getElementsByTagName("tbody")[0]||a.appendChild(a.ownerDocument.createElement("tbody")):a}function U(a){var b=V.split("|"),c=a.createDocumentFragment();if(c.createElement)while(b.length)c.createElement(b.pop());return c}function T(a,b,c){b=b||0;if(f.isFunction(b))return f.grep(a,function(a,d){var e=!!b.call(a,d,a);return e===c});if(b.nodeType)return f.grep(a,function(a,d){return a===b===c});if(typeof b=="string"){var d=f.grep(a,function(a){return a.nodeType===1});if(O.test(b))return f.filter(b,d,!c);b=f.filter(b,d)}return f.grep(a,function(a,d){return f.inArray(a,b)>=0===c})}function S(a){return!a||!a.parentNode||a.parentNode.nodeType===11}function K(){return!0}function J(){return!1}function n(a,b,c){var d=b+"defer",e=b+"queue",g=b+"mark",h=f._data(a,d);h&&(c==="queue"||!f._data(a,e))&&(c==="mark"||!f._data(a,g))&&setTimeout(function(){!f._data(a,e)&&!f._data(a,g)&&(f.removeData(a,d,!0),h.fire())},0)}function m(a){for(var b in a){if(b==="data"&&f.isEmptyObject(a[b]))continue;if(b!=="toJSON")return!1}return!0}function l(a,c,d){if(d===b&&a.nodeType===1){var e="data-"+c.replace(k,"-$1").toLowerCase();d=a.getAttribute(e);if(typeof d=="string"){try{d=d==="true"?!0:d==="false"?!1:d==="null"?null:f.isNumeric(d)?parseFloat(d):j.test(d)?f.parseJSON(d):d}catch(g){}f.data(a,c,d)}else d=b}return d}function h(a){var b=g[a]={},c,d;a=a.split(/\s+/);for(c=0,d=a.length;c<d;c++)b[a[c]]=!0;return b}var c=a.document,d=a.navigator,e=a.location,f=function(){function J(){if(!e.isReady){try{c.documentElement.doScroll("left")}catch(a){setTimeout(J,1);return}e.ready()}}var e=function(a,b){return new e.fn.init(a,b,h)},f=a.jQuery,g=a.$,h,i=/^(?:[^#<]*(<[\w\W]+>)[^>]*$|#([\w\-]*)$)/,j=/\S/,k=/^\s+/,l=/\s+$/,m=/^<(\w+)\s*\/?>(?:<\/\1>)?$/,n=/^[\],:{}\s]*$/,o=/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,p=/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,q=/(?:^|:|,)(?:\s*\[)+/g,r=/(webkit)[ \/]([\w.]+)/,s=/(opera)(?:.*version)?[ \/]([\w.]+)/,t=/(msie) ([\w.]+)/,u=/(mozilla)(?:.*? rv:([\w.]+))?/,v=/-([a-z]|[0-9])/ig,w=/^-ms-/,x=function(a,b){return(b+"").toUpperCase()},y=d.userAgent,z,A,B,C=Object.prototype.toString,D=Object.prototype.hasOwnProperty,E=Array.prototype.push,F=Array.prototype.slice,G=String.prototype.trim,H=Array.prototype.indexOf,I={};e.fn=e.prototype={constructor:e,init:function(a,d,f){var g,h,j,k;if(!a)return this;if(a.nodeType){this.context=this[0]=a,this.length=1;return this}if(a==="body"&&!d&&c.body){this.context=c,this[0]=c.body,this.selector=a,this.length=1;return this}if(typeof a=="string"){a.charAt(0)!=="<"||a.charAt(a.length-1)!==">"||a.length<3?g=i.exec(a):g=[null,a,null];if(g&&(g[1]||!d)){if(g[1]){d=d instanceof e?d[0]:d,k=d?d.ownerDocument||d:c,j=m.exec(a),j?e.isPlainObject(d)?(a=[c.createElement(j[1])],e.fn.attr.call(a,d,!0)):a=[k.createElement(j[1])]:(j=e.buildFragment([g[1]],[k]),a=(j.cacheable?e.clone(j.fragment):j.fragment).childNodes);return e.merge(this,a)}h=c.getElementById(g[2]);if(h&&h.parentNode){if(h.id!==g[2])return f.find(a);this.length=1,this[0]=h}this.context=c,this.selector=a;return this}return!d||d.jquery?(d||f).find(a):this.constructor(d).find(a)}if(e.isFunction(a))return f.ready(a);a.selector!==b&&(this.selector=a.selector,this.context=a.context);return e.makeArray(a,this)},selector:"",jquery:"1.7.1",length:0,size:function(){return this.length},toArray:function(){return F.call(this,0)},get:function(a){return a==null?this.toArray():a<0?this[this.length+a]:this[a]},pushStack:function(a,b,c){var d=this.constructor();e.isArray(a)?E.apply(d,a):e.merge(d,a),d.prevObject=this,d.context=this.context,b==="find"?d.selector=this.selector+(this.selector?" ":"")+c:b&&(d.selector=this.selector+"."+b+"("+c+")");return d},each:function(a,b){return e.each(this,a,b)},ready:function(a){e.bindReady(),A.add(a);return this},eq:function(a){a=+a;return a===-1?this.slice(a):this.slice(a,a+1)},first:function(){return this.eq(0)},last:function(){return this.eq(-1)},slice:function(){return this.pushStack(F.apply(this,arguments),"slice",F.call(arguments).join(","))},map:function(a){return this.pushStack(e.map(this,function(b,c){return a.call(b,c,b)}))},end:function(){return this.prevObject||this.constructor(null)},push:E,sort:[].sort,splice:[].splice},e.fn.init.prototype=e.fn,e.extend=e.fn.extend=function(){var a,c,d,f,g,h,i=arguments[0]||{},j=1,k=arguments.length,l=!1;typeof i=="boolean"&&(l=i,i=arguments[1]||{},j=2),typeof i!="object"&&!e.isFunction(i)&&(i={}),k===j&&(i=this,--j);for(;j<k;j++)if((a=arguments[j])!=null)for(c in a){d=i[c],f=a[c];if(i===f)continue;l&&f&&(e.isPlainObject(f)||(g=e.isArray(f)))?(g?(g=!1,h=d&&e.isArray(d)?d:[]):h=d&&e.isPlainObject(d)?d:{},i[c]=e.extend(l,h,f)):f!==b&&(i[c]=f)}return i},e.extend({noConflict:function(b){a.$===e&&(a.$=g),b&&a.jQuery===e&&(a.jQuery=f);return e},isReady:!1,readyWait:1,holdReady:function(a){a?e.readyWait++:e.ready(!0)},ready:function(a){if(a===!0&&!--e.readyWait||a!==!0&&!e.isReady){if(!c.body)return setTimeout(e.ready,1);e.isReady=!0;if(a!==!0&&--e.readyWait>0)return;A.fireWith(c,[e]),e.fn.trigger&&e(c).trigger("ready").off("ready")}},bindReady:function(){if(!A){A=e.Callbacks("once memory");if(c.readyState==="complete")return setTimeout(e.ready,1);if(c.addEventListener)c.addEventListener("DOMContentLoaded",B,!1),a.addEventListener("load",e.ready,!1);else if(c.attachEvent){c.attachEvent("onreadystatechange",B),a.attachEvent("onload",e.ready);var b=!1;try{b=a.frameElement==null}catch(d){}c.documentElement.doScroll&&b&&J()}}},isFunction:function(a){return e.type(a)==="function"},isArray:Array.isArray||function(a){return e.type(a)==="array"},isWindow:function(a){return a&&typeof a=="object"&&"setInterval"in a},isNumeric:function(a){return!isNaN(parseFloat(a))&&isFinite(a)},type:function(a){return a==null?String(a):I[C.call(a)]||"object"},isPlainObject:function(a){if(!a||e.type(a)!=="object"||a.nodeType||e.isWindow(a))return!1;try{if(a.constructor&&!D.call(a,"constructor")&&!D.call(a.constructor.prototype,"isPrototypeOf"))return!1}catch(c){return!1}var d;for(d in a);return d===b||D.call(a,d)},isEmptyObject:function(a){for(var b in a)return!1;return!0},error:function(a){throw new Error(a)},parseJSON:function(b){if(typeof b!="string"||!b)return null;b=e.trim(b);if(a.JSON&&a.JSON.parse)return a.JSON.parse(b);if(n.test(b.replace(o,"@").replace(p,"]").replace(q,"")))return(new Function("return "+b))();e.error("Invalid JSON: "+b)},parseXML:function(c){var d,f;try{a.DOMParser?(f=new DOMParser,d=f.parseFromString(c,"text/xml")):(d=new ActiveXObject("Microsoft.XMLDOM"),d.async="false",d.loadXML(c))}catch(g){d=b}(!d||!d.documentElement||d.getElementsByTagName("parsererror").length)&&e.error("Invalid XML: "+c);return d},noop:function(){},globalEval:function(b){b&&j.test(b)&&(a.execScript||function(b){a.eval.call(a,b)})(b)},camelCase:function(a){return a.replace(w,"ms-").replace(v,x)},nodeName:function(a,b){return a.nodeName&&a.nodeName.toUpperCase()===b.toUpperCase()},each:function(a,c,d){var f,g=0,h=a.length,i=h===b||e.isFunction(a);if(d){if(i){for(f in a)if(c.apply(a[f],d)===!1)break}else for(;g<h;)if(c.apply(a[g++],d)===!1)break}else if(i){for(f in a)if(c.call(a[f],f,a[f])===!1)break}else for(;g<h;)if(c.call(a[g],g,a[g++])===!1)break;return a},trim:G?function(a){return a==null?"":G.call(a)}:function(a){return a==null?"":(a+"").replace(k,"").replace(l,"")},makeArray:function(a,b){var c=b||[];if(a!=null){var d=e.type(a);a.length==null||d==="string"||d==="function"||d==="regexp"||e.isWindow(a)?E.call(c,a):e.merge(c,a)}return c},inArray:function(a,b,c){var d;if(b){if(H)return H.call(b,a,c);d=b.length,c=c?c<0?Math.max(0,d+c):c:0;for(;c<d;c++)if(c in b&&b[c]===a)return c}return-1},merge:function(a,c){var d=a.length,e=0;if(typeof c.length=="number")for(var f=c.length;e<f;e++)a[d++]=c[e];else while(c[e]!==b)a[d++]=c[e++];a.length=d;return a},grep:function(a,b,c){var d=[],e;c=!!c;for(var f=0,g=a.length;f<g;f++)e=!!b(a[f],f),c!==e&&d.push(a[f]);return d},map:function(a,c,d){var f,g,h=[],i=0,j=a.length,k=a instanceof e||j!==b&&typeof j=="number"&&(j>0&&a[0]&&a[j-1]||j===0||e.isArray(a));if(k)for(;i<j;i++)f=c(a[i],i,d),f!=null&&(h[h.length]=f);else for(g in a)f=c(a[g],g,d),f!=null&&(h[h.length]=f);return h.concat.apply([],h)},guid:1,proxy:function(a,c){if(typeof c=="string"){var d=a[c];c=a,a=d}if(!e.isFunction(a))return b;var f=F.call(arguments,2),g=function(){return a.apply(c,f.concat(F.call(arguments)))};g.guid=a.guid=a.guid||g.guid||e.guid++;return g},access:function(a,c,d,f,g,h){var i=a.length;if(typeof c=="object"){for(var j in c)e.access(a,j,c[j],f,g,d);return a}if(d!==b){f=!h&&f&&e.isFunction(d);for(var k=0;k<i;k++)g(a[k],c,f?d.call(a[k],k,g(a[k],c)):d,h);return a}return i?g(a[0],c):b},now:function(){return(new Date).getTime()},uaMatch:function(a){a=a.toLowerCase();var b=r.exec(a)||s.exec(a)||t.exec(a)||a.indexOf("compatible")<0&&u.exec(a)||[];return{browser:b[1]||"",version:b[2]||"0"}},sub:function(){function a(b,c){return new a.fn.init(b,c)}e.extend(!0,a,this),a.superclass=this,a.fn=a.prototype=this(),a.fn.constructor=a,a.sub=this.sub,a.fn.init=function(d,f){f&&f instanceof e&&!(f instanceof a)&&(f=a(f));return e.fn.init.call(this,d,f,b)},a.fn.init.prototype=a.fn;var b=a(c);return a},browser:{}}),e.each("Boolean Number String Function Array Date RegExp Object".split(" "),function(a,b){I["[object "+b+"]"]=b.toLowerCase()}),z=e.uaMatch(y),z.browser&&(e.browser[z.browser]=!0,e.browser.version=z.version),e.browser.webkit&&(e.browser.safari=!0),j.test(" ")&&(k=/^[\s\xA0]+/,l=/[\s\xA0]+$/),h=e(c),c.addEventListener?B=function(){c.removeEventListener("DOMContentLoaded",B,!1),e.ready()}:c.attachEvent&&(B=function(){c.readyState==="complete"&&(c.detachEvent("onreadystatechange",B),e.ready())});return e}(),g={};f.Callbacks=function(a){a=a?g[a]||h(a):{};var c=[],d=[],e,i,j,k,l,m=function(b){var d,e,g,h,i;for(d=0,e=b.length;d<e;d++)g=b[d],h=f.type(g),h==="array"?m(g):h==="function"&&(!a.unique||!o.has(g))&&c.push(g)},n=function(b,f){f=f||[],e=!a.memory||[b,f],i=!0,l=j||0,j=0,k=c.length;for(;c&&l<k;l++)if(c[l].apply(b,f)===!1&&a.stopOnFalse){e=!0;break}i=!1,c&&(a.once?e===!0?o.disable():c=[]:d&&d.length&&(e=d.shift(),o.fireWith(e[0],e[1])))},o={add:function(){if(c){var a=c.length;m(arguments),i?k=c.length:e&&e!==!0&&(j=a,n(e[0],e[1]))}return this},remove:function(){if(c){var b=arguments,d=0,e=b.length;for(;d<e;d++)for(var f=0;f<c.length;f++)if(b[d]===c[f]){i&&f<=k&&(k--,f<=l&&l--),c.splice(f--,1);if(a.unique)break}}return this},has:function(a){if(c){var b=0,d=c.length;for(;b<d;b++)if(a===c[b])return!0}return!1},empty:function(){c=[];return this},disable:function(){c=d=e=b;return this},disabled:function(){return!c},lock:function(){d=b,(!e||e===!0)&&o.disable();return this},locked:function(){return!d},fireWith:function(b,c){d&&(i?a.once||d.push([b,c]):(!a.once||!e)&&n(b,c));return this},fire:function(){o.fireWith(this,arguments);return this},fired:function(){return!!e}};return o};var i=[].slice;f.extend({Deferred:function(a){var b=f.Callbacks("once memory"),c=f.Callbacks("once memory"),d=f.Callbacks("memory"),e="pending",g={resolve:b,reject:c,notify:d},h={done:b.add,fail:c.add,progress:d.add,state:function(){return e},isResolved:b.fired,isRejected:c.fired,then:function(a,b,c){i.done(a).fail(b).progress(c);return this},always:function(){i.done.apply(i,arguments).fail.apply(i,arguments);return this},pipe:function(a,b,c){return f.Deferred(function(d){f.each({done:[a,"resolve"],fail:[b,"reject"],progress:[c,"notify"]},function(a,b){var c=b[0],e=b[1],g;f.isFunction(c)?i[a](function(){g=c.apply(this,arguments),g&&f.isFunction(g.promise)?g.promise().then(d.resolve,d.reject,d.notify):d[e+"With"](this===i?d:this,[g])}):i[a](d[e])})}).promise()},promise:function(a){if(a==null)a=h;else for(var b in h)a[b]=h[b];return a}},i=h.promise({}),j;for(j in g)i[j]=g[j].fire,i[j+"With"]=g[j].fireWith;i.done(function(){e="resolved"},c.disable,d.lock).fail(function(){e="rejected"},b.disable,d.lock),a&&a.call(i,i);return i},when:function(a){function m(a){return function(b){e[a]=arguments.length>1?i.call(arguments,0):b,j.notifyWith(k,e)}}function l(a){return function(c){b[a]=arguments.length>1?i.call(arguments,0):c,--g||j.resolveWith(j,b)}}var b=i.call(arguments,0),c=0,d=b.length,e=Array(d),g=d,h=d,j=d<=1&&a&&f.isFunction(a.promise)?a:f.Deferred(),k=j.promise();if(d>1){for(;c<d;c++)b[c]&&b[c].promise&&f.isFunction(b[c].promise)?b[c].promise().then(l(c),j.reject,m(c)):--g;g||j.resolveWith(j,b)}else j!==a&&j.resolveWith(j,d?[a]:[]);return k}}),f.support=function(){var b,d,e,g,h,i,j,k,l,m,n,o,p,q=c.createElement("div"),r=c.documentElement;q.setAttribute("className","t"),q.innerHTML="   <link/><table></table><a href='/a' style='top:1px;float:left;opacity:.55;'>a</a><input type='checkbox'/>",d=q.getElementsByTagName("*"),e=q.getElementsByTagName("a")[0];if(!d||!d.length||!e)return{};g=c.createElement("select"),h=g.appendChild(c.createElement("option")),i=q.getElementsByTagName("input")[0],b={leadingWhitespace:q.firstChild.nodeType===3,tbody:!q.getElementsByTagName("tbody").length,htmlSerialize:!!q.getElementsByTagName("link").length,style:/top/.test(e.getAttribute("style")),hrefNormalized:e.getAttribute("href")==="/a",opacity:/^0.55/.test(e.style.opacity),cssFloat:!!e.style.cssFloat,checkOn:i.value==="on",optSelected:h.selected,getSetAttribute:q.className!=="t",enctype:!!c.createElement("form").enctype,html5Clone:c.createElement("nav").cloneNode(!0).outerHTML!=="<:nav></:nav>",submitBubbles:!0,changeBubbles:!0,focusinBubbles:!1,deleteExpando:!0,noCloneEvent:!0,inlineBlockNeedsLayout:!1,shrinkWrapBlocks:!1,reliableMarginRight:!0},i.checked=!0,b.noCloneChecked=i.cloneNode(!0).checked,g.disabled=!0,b.optDisabled=!h.disabled;try{delete q.test}catch(s){b.deleteExpando=!1}!q.addEventListener&&q.attachEvent&&q.fireEvent&&(q.attachEvent("onclick",function(){b.noCloneEvent=!1}),q.cloneNode(!0).fireEvent("onclick")),i=c.createElement("input"),i.value="t",i.setAttribute("type","radio"),b.radioValue=i.value==="t",i.setAttribute("checked","checked"),q.appendChild(i),k=c.createDocumentFragment(),k.appendChild(q.lastChild),b.checkClone=k.cloneNode(!0).cloneNode(!0).lastChild.checked,b.appendChecked=i.checked,k.removeChild(i),k.appendChild(q),q.innerHTML="",a.getComputedStyle&&(j=c.createElement("div"),j.style.width="0",j.style.marginRight="0",q.style.width="2px",q.appendChild(j),b.reliableMarginRight=(parseInt((a.getComputedStyle(j,null)||{marginRight:0}).marginRight,10)||0)===0);if(q.attachEvent)for(o in{submit:1,change:1,focusin:1})n="on"+o,p=n in q,p||(q.setAttribute(n,"return;"),p=typeof q[n]=="function"),b[o+"Bubbles"]=p;k.removeChild(q),k=g=h=j=q=i=null,f(function(){var a,d,e,g,h,i,j,k,m,n,o,r=c.getElementsByTagName("body")[0];!r||(j=1,k="position:absolute;top:0;left:0;width:1px;height:1px;margin:0;",m="visibility:hidden;border:0;",n="style='"+k+"border:5px solid #000;padding:0;'",o="<div "+n+"><div></div></div>"+"<table "+n+" cellpadding='0' cellspacing='0'>"+"<tr><td></td></tr></table>",a=c.createElement("div"),a.style.cssText=m+"width:0;height:0;position:static;top:0;margin-top:"+j+"px",r.insertBefore(a,r.firstChild),q=c.createElement("div"),a.appendChild(q),q.innerHTML="<table><tr><td style='padding:0;border:0;display:none'></td><td>t</td></tr></table>",l=q.getElementsByTagName("td"),p=l[0].offsetHeight===0,l[0].style.display="",l[1].style.display="none",b.reliableHiddenOffsets=p&&l[0].offsetHeight===0,q.innerHTML="",q.style.width=q.style.paddingLeft="1px",f.boxModel=b.boxModel=q.offsetWidth===2,typeof q.style.zoom!="undefined"&&(q.style.display="inline",q.style.zoom=1,b.inlineBlockNeedsLayout=q.offsetWidth===2,q.style.display="",q.innerHTML="<div style='width:4px;'></div>",b.shrinkWrapBlocks=q.offsetWidth!==2),q.style.cssText=k+m,q.innerHTML=o,d=q.firstChild,e=d.firstChild,h=d.nextSibling.firstChild.firstChild,i={doesNotAddBorder:e.offsetTop!==5,doesAddBorderForTableAndCells:h.offsetTop===5},e.style.position="fixed",e.style.top="20px",i.fixedPosition=e.offsetTop===20||e.offsetTop===15,e.style.position=e.style.top="",d.style.overflow="hidden",d.style.position="relative",i.subtractsBorderForOverflowNotVisible=e.offsetTop===-5,i.doesNotIncludeMarginInBodyOffset=r.offsetTop!==j,r.removeChild(a),q=a=null,f.extend(b,i))});return b}();var j=/^(?:\{.*\}|\[.*\])$/,k=/([A-Z])/g;f.extend({cache:{},uuid:0,expando:"jQuery"+(f.fn.jquery+Math.random()).replace(/\D/g,""),noData:{embed:!0,object:"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000",applet:!0},hasData:function(a){a=a.nodeType?f.cache[a[f.expando]]:a[f.expando];return!!a&&!m(a)},data:function(a,c,d,e){if(!!f.acceptData(a)){var g,h,i,j=f.expando,k=typeof c=="string",l=a.nodeType,m=l?f.cache:a,n=l?a[j]:a[j]&&j,o=c==="events";if((!n||!m[n]||!o&&!e&&!m[n].data)&&k&&d===b)return;n||(l?a[j]=n=++f.uuid:n=j),m[n]||(m[n]={},l||(m[n].toJSON=f.noop));if(typeof c=="object"||typeof c=="function")e?m[n]=f.extend(m[n],c):m[n].data=f.extend(m[n].data,c);g=h=m[n],e||(h.data||(h.data={}),h=h.data),d!==b&&(h[f.camelCase(c)]=d);if(o&&!h[c])return g.events;k?(i=h[c],i==null&&(i=h[f.camelCase(c)])):i=h;return i}},removeData:function(a,b,c){if(!!f.acceptData(a)){var d,e,g,h=f.expando,i=a.nodeType,j=i?f.cache:a,k=i?a[h]:h;if(!j[k])return;if(b){d=c?j[k]:j[k].data;if(d){f.isArray(b)||(b in d?b=[b]:(b=f.camelCase(b),b in d?b=[b]:b=b.split(" ")));for(e=0,g=b.length;e<g;e++)delete d[b[e]];if(!(c?m:f.isEmptyObject)(d))return}}if(!c){delete j[k].data;if(!m(j[k]))return}f.support.deleteExpando||!j.setInterval?delete j[k]:j[k]=null,i&&(f.support.deleteExpando?delete a[h]:a.removeAttribute?a.removeAttribute(h):a[h]=null)}},_data:function(a,b,c){return f.data(a,b,c,!0)},acceptData:function(a){if(a.nodeName){var b=f.noData[a.nodeName.toLowerCase()];if(b)return b!==!0&&a.getAttribute("classid")===b}return!0}}),f.fn.extend({data:function(a,c){var d,e,g,h=null;if(typeof a=="undefined"){if(this.length){h=f.data(this[0]);if(this[0].nodeType===1&&!f._data(this[0],"parsedAttrs")){e=this[0].attributes;for(var i=0,j=e.length;i<j;i++)g=e[i].name,g.indexOf("data-")===0&&(g=f.camelCase(g.substring(5)),l(this[0],g,h[g]));f._data(this[0],"parsedAttrs",!0)}}return h}if(typeof a=="object")return this.each(function(){f.data(this,a)});d=a.split("."),d[1]=d[1]?"."+d[1]:"";if(c===b){h=this.triggerHandler("getData"+d[1]+"!",[d[0]]),h===b&&this.length&&(h=f.data(this[0],a),h=l(this[0],a,h));return h===b&&d[1]?this.data(d[0]):h}return this.each(function(){var b=f(this),e=[d[0],c];b.triggerHandler("setData"+d[1]+"!",e),f.data(this,a,c),b.triggerHandler("changeData"+d[1]+"!",e)})},removeData:function(a){return this.each(function(){f.removeData(this,a)})}}),f.extend({_mark:function(a,b){a&&(b=(b||"fx")+"mark",f._data(a,b,(f._data(a,b)||0)+1))},_unmark:function(a,b,c){a!==!0&&(c=b,b=a,a=!1);if(b){c=c||"fx";var d=c+"mark",e=a?0:(f._data(b,d)||1)-1;e?f._data(b,d,e):(f.removeData(b,d,!0),n(b,c,"mark"))}},queue:function(a,b,c){var d;if(a){b=(b||"fx")+"queue",d=f._data(a,b),c&&(!d||f.isArray(c)?d=f._data(a,b,f.makeArray(c)):d.push(c));return d||[]}},dequeue:function(a,b){b=b||"fx";var c=f.queue(a,b),d=c.shift(),e={};d==="inprogress"&&(d=c.shift()),d&&(b==="fx"&&c.unshift("inprogress"),f._data(a,b+".run",e),d.call(a,function(){f.dequeue(a,b)},e)),c.length||(f.removeData(a,b+"queue "+b+".run",!0),n(a,b,"queue"))}}),f.fn.extend({queue:function(a,c){typeof a!="string"&&(c=a,a="fx");if(c===b)return f.queue(this[0],a);return this.each(function(){var b=f.queue(this,a,c);a==="fx"&&b[0]!=="inprogress"&&f.dequeue(this,a)})},dequeue:function(a){return this.each(function(){f.dequeue(this,a)})},delay:function(a,b){a=f.fx?f.fx.speeds[a]||a:a,b=b||"fx";return this.queue(b,function(b,c){var d=setTimeout(b,a);c.stop=function(){clearTimeout(d)}})},clearQueue:function(a){return this.queue(a||"fx",[])},promise:function(a,c){function m(){--h||d.resolveWith(e,[e])}typeof a!="string"&&(c=a,a=b),a=a||"fx";var d=f.Deferred(),e=this,g=e.length,h=1,i=a+"defer",j=a+"queue",k=a+"mark",l;while(g--)if(l=f.data(e[g],i,b,!0)||(f.data(e[g],j,b,!0)||f.data(e[g],k,b,!0))&&f.data(e[g],i,f.Callbacks("once memory"),!0))h++,l.add(m);m();return d.promise()}});var o=/[\n\t\r]/g,p=/\s+/,q=/\r/g,r=/^(?:button|input)$/i,s=/^(?:button|input|object|select|textarea)$/i,t=/^a(?:rea)?$/i,u=/^(?:autofocus|autoplay|async|checked|controls|defer|disabled|hidden|loop|multiple|open|readonly|required|scoped|selected)$/i,v=f.support.getSetAttribute,w,x,y;f.fn.extend({attr:function(a,b){return f.access(this,a,b,!0,f.attr)},removeAttr:function(a){return this.each(function(){f.removeAttr(this,a)})},prop:function(a,b){return f.access(this,a,b,!0,f.prop)},removeProp:function(a){a=f.propFix[a]||a;return this.each(function(){try{this[a]=b,delete this[a]}catch(c){}})},addClass:function(a){var b,c,d,e,g,h,i;if(f.isFunction(a))return this.each(function(b){f(this).addClass(a.call(this,b,this.className))});if(a&&typeof a=="string"){b=a.split(p);for(c=0,d=this.length;c<d;c++){e=this[c];if(e.nodeType===1)if(!e.className&&b.length===1)e.className=a;else{g=" "+e.className+" ";for(h=0,i=b.length;h<i;h++)~g.indexOf(" "+b[h]+" ")||(g+=b[h]+" ");e.className=f.trim(g)}}}return this},removeClass:function(a){var c,d,e,g,h,i,j;if(f.isFunction(a))return this.each(function(b){f(this).removeClass(a.call(this,b,this.className))});if(a&&typeof a=="string"||a===b){c=(a||"").split(p);for(d=0,e=this.length;d<e;d++){g=this[d];if(g.nodeType===1&&g.className)if(a){h=(" "+g.className+" ").replace(o," ");for(i=0,j=c.length;i<j;i++)h=h.replace(" "+c[i]+" "," ");g.className=f.trim(h)}else g.className=""}}return this},toggleClass:function(a,b){var c=typeof a,d=typeof b=="boolean";if(f.isFunction(a))return this.each(function(c){f(this).toggleClass(a.call(this,c,this.className,b),b)});return this.each(function(){if(c==="string"){var e,g=0,h=f(this),i=b,j=a.split(p);while(e=j[g++])i=d?i:!h.hasClass(e),h[i?"addClass":"removeClass"](e)}else if(c==="undefined"||c==="boolean")this.className&&f._data(this,"__className__",this.className),this.className=this.className||a===!1?"":f._data(this,"__className__")||""})},hasClass:function(a){var b=" "+a+" ",c=0,d=this.length;for(;c<d;c++)if(this[c].nodeType===1&&(" "+this[c].className+" ").replace(o," ").indexOf(b)>-1)return!0;return!1},val:function(a){var c,d,e,g=this[0];{if(!!arguments.length){e=f.isFunction(a);return this.each(function(d){var g=f(this),h;if(this.nodeType===1){e?h=a.call(this,d,g.val()):h=a,h==null?h="":typeof h=="number"?h+="":f.isArray(h)&&(h=f.map(h,function(a){return a==null?"":a+""})),c=f.valHooks[this.nodeName.toLowerCase()]||f.valHooks[this.type];if(!c||!("set"in c)||c.set(this,h,"value")===b)this.value=h}})}if(g){c=f.valHooks[g.nodeName.toLowerCase()]||f.valHooks[g.type];if(c&&"get"in c&&(d=c.get(g,"value"))!==b)return d;d=g.value;return typeof d=="string"?d.replace(q,""):d==null?"":d}}}}),f.extend({valHooks:{option:{get:function(a){var b=a.attributes.value;return!b||b.specified?a.value:a.text}},select:{get:function(a){var b,c,d,e,g=a.selectedIndex,h=[],i=a.options,j=a.type==="select-one";if(g<0)return null;c=j?g:0,d=j?g+1:i.length;for(;c<d;c++){e=i[c];if(e.selected&&(f.support.optDisabled?!e.disabled:e.getAttribute("disabled")===null)&&(!e.parentNode.disabled||!f.nodeName(e.parentNode,"optgroup"))){b=f(e).val();if(j)return b;h.push(b)}}if(j&&!h.length&&i.length)return f(i[g]).val();return h},set:function(a,b){var c=f.makeArray(b);f(a).find("option").each(function(){this.selected=f.inArray(f(this).val(),c)>=0}),c.length||(a.selectedIndex=-1);return c}}},attrFn:{val:!0,css:!0,html:!0,text:!0,data:!0,width:!0,height:!0,offset:!0},attr:function(a,c,d,e){var g,h,i,j=a.nodeType;if(!!a&&j!==3&&j!==8&&j!==2){if(e&&c in f.attrFn)return f(a)[c](d);if(typeof a.getAttribute=="undefined")return f.prop(a,c,d);i=j!==1||!f.isXMLDoc(a),i&&(c=c.toLowerCase(),h=f.attrHooks[c]||(u.test(c)?x:w));if(d!==b){if(d===null){f.removeAttr(a,c);return}if(h&&"set"in h&&i&&(g=h.set(a,d,c))!==b)return g;a.setAttribute(c,""+d);return d}if(h&&"get"in h&&i&&(g=h.get(a,c))!==null)return g;g=a.getAttribute(c);return g===null?b:g}},removeAttr:function(a,b){var c,d,e,g,h=0;if(b&&a.nodeType===1){d=b.toLowerCase().split(p),g=d.length;for(;h<g;h++)e=d[h],e&&(c=f.propFix[e]||e,f.attr(a,e,""),a.removeAttribute(v?e:c),u.test(e)&&c in a&&(a[c]=!1))}},attrHooks:{type:{set:function(a,b){if(r.test(a.nodeName)&&a.parentNode)f.error("type property can't be changed");else if(!f.support.radioValue&&b==="radio"&&f.nodeName(a,"input")){var c=a.value;a.setAttribute("type",b),c&&(a.value=c);return b}}},value:{get:function(a,b){if(w&&f.nodeName(a,"button"))return w.get(a,b);return b in a?a.value:null},set:function(a,b,c){if(w&&f.nodeName(a,"button"))return w.set(a,b,c);a.value=b}}},propFix:{tabindex:"tabIndex",readonly:"readOnly","for":"htmlFor","class":"className",maxlength:"maxLength",cellspacing:"cellSpacing",cellpadding:"cellPadding",rowspan:"rowSpan",colspan:"colSpan",usemap:"useMap",frameborder:"frameBorder",contenteditable:"contentEditable"},prop:function(a,c,d){var e,g,h,i=a.nodeType;if(!!a&&i!==3&&i!==8&&i!==2){h=i!==1||!f.isXMLDoc(a),h&&(c=f.propFix[c]||c,g=f.propHooks[c]);return d!==b?g&&"set"in g&&(e=g.set(a,d,c))!==b?e:a[c]=d:g&&"get"in g&&(e=g.get(a,c))!==null?e:a[c]}},propHooks:{tabIndex:{get:function(a){var c=a.getAttributeNode("tabindex");return c&&c.specified?parseInt(c.value,10):s.test(a.nodeName)||t.test(a.nodeName)&&a.href?0:b}}}}),f.attrHooks.tabindex=f.propHooks.tabIndex,x={get:function(a,c){var d,e=f.prop(a,c);return e===!0||typeof e!="boolean"&&(d=a.getAttributeNode(c))&&d.nodeValue!==!1?c.toLowerCase():b},set:function(a,b,c){var d;b===!1?f.removeAttr(a,c):(d=f.propFix[c]||c,d in a&&(a[d]=!0),a.setAttribute(c,c.toLowerCase()));return c}},v||(y={name:!0,id:!0},w=f.valHooks.button={get:function(a,c){var d;d=a.getAttributeNode(c);return d&&(y[c]?d.nodeValue!=="":d.specified)?d.nodeValue:b},set:function(a,b,d){var e=a.getAttributeNode(d);e||(e=c.createAttribute(d),a.setAttributeNode(e));return e.nodeValue=b+""}},f.attrHooks.tabindex.set=w.set,f.each(["width","height"],function(a,b){f.attrHooks[b]=f.extend(f.attrHooks[b],{set:function(a,c){if(c===""){a.setAttribute(b,"auto");return c}}})}),f.attrHooks.contenteditable={get:w.get,set:function(a,b,c){b===""&&(b="false"),w.set(a,b,c)}}),f.support.hrefNormalized||f.each(["href","src","width","height"],function(a,c){f.attrHooks[c]=f.extend(f.attrHooks[c],{get:function(a){var d=a.getAttribute(c,2);return d===null?b:d}})}),f.support.style||(f.attrHooks.style={get:function(a){return a.style.cssText.toLowerCase()||b},set:function(a,b){return a.style.cssText=""+b}}),f.support.optSelected||(f.propHooks.selected=f.extend(f.propHooks.selected,{get:function(a){var b=a.parentNode;b&&(b.selectedIndex,b.parentNode&&b.parentNode.selectedIndex);return null}})),f.support.enctype||(f.propFix.enctype="encoding"),f.support.checkOn||f.each(["radio","checkbox"],function(){f.valHooks[this]={get:function(a){return a.getAttribute("value")===null?"on":a.value}}}),f.each(["radio","checkbox"],function(){f.valHooks[this]=f.extend(f.valHooks[this],{set:function(a,b){if(f.isArray(b))return a.checked=f.inArray(f(a).val(),b)>=0}})});var z=/^(?:textarea|input|select)$/i,A=/^([^\.]*)?(?:\.(.+))?$/,B=/\bhover(\.\S+)?\b/,C=/^key/,D=/^(?:mouse|contextmenu)|click/,E=/^(?:focusinfocus|focusoutblur)$/,F=/^(\w*)(?:#([\w\-]+))?(?:\.([\w\-]+))?$/,G=function(a){var b=F.exec(a);b&&(b[1]=(b[1]||"").toLowerCase(),b[3]=b[3]&&new RegExp("(?:^|\\s)"+b[3]+"(?:\\s|$)"));return b},H=function(a,b){var c=a.attributes||{};return(!b[1]||a.nodeName.toLowerCase()===b[1])&&(!b[2]||(c.id||{}).value===b[2])&&(!b[3]||b[3].test((c["class"]||{}).value))},I=function(a){return f.event.special.hover?a:a.replace(B,"mouseenter$1 mouseleave$1")};
f.event={add:function(a,c,d,e,g){var h,i,j,k,l,m,n,o,p,q,r,s;if(!(a.nodeType===3||a.nodeType===8||!c||!d||!(h=f._data(a)))){d.handler&&(p=d,d=p.handler),d.guid||(d.guid=f.guid++),j=h.events,j||(h.events=j={}),i=h.handle,i||(h.handle=i=function(a){return typeof f!="undefined"&&(!a||f.event.triggered!==a.type)?f.event.dispatch.apply(i.elem,arguments):b},i.elem=a),c=f.trim(I(c)).split(" ");for(k=0;k<c.length;k++){l=A.exec(c[k])||[],m=l[1],n=(l[2]||"").split(".").sort(),s=f.event.special[m]||{},m=(g?s.delegateType:s.bindType)||m,s=f.event.special[m]||{},o=f.extend({type:m,origType:l[1],data:e,handler:d,guid:d.guid,selector:g,quick:G(g),namespace:n.join(".")},p),r=j[m];if(!r){r=j[m]=[],r.delegateCount=0;if(!s.setup||s.setup.call(a,e,n,i)===!1)a.addEventListener?a.addEventListener(m,i,!1):a.attachEvent&&a.attachEvent("on"+m,i)}s.add&&(s.add.call(a,o),o.handler.guid||(o.handler.guid=d.guid)),g?r.splice(r.delegateCount++,0,o):r.push(o),f.event.global[m]=!0}a=null}},global:{},remove:function(a,b,c,d,e){var g=f.hasData(a)&&f._data(a),h,i,j,k,l,m,n,o,p,q,r,s;if(!!g&&!!(o=g.events)){b=f.trim(I(b||"")).split(" ");for(h=0;h<b.length;h++){i=A.exec(b[h])||[],j=k=i[1],l=i[2];if(!j){for(j in o)f.event.remove(a,j+b[h],c,d,!0);continue}p=f.event.special[j]||{},j=(d?p.delegateType:p.bindType)||j,r=o[j]||[],m=r.length,l=l?new RegExp("(^|\\.)"+l.split(".").sort().join("\\.(?:.*\\.)?")+"(\\.|$)"):null;for(n=0;n<r.length;n++)s=r[n],(e||k===s.origType)&&(!c||c.guid===s.guid)&&(!l||l.test(s.namespace))&&(!d||d===s.selector||d==="**"&&s.selector)&&(r.splice(n--,1),s.selector&&r.delegateCount--,p.remove&&p.remove.call(a,s));r.length===0&&m!==r.length&&((!p.teardown||p.teardown.call(a,l)===!1)&&f.removeEvent(a,j,g.handle),delete o[j])}f.isEmptyObject(o)&&(q=g.handle,q&&(q.elem=null),f.removeData(a,["events","handle"],!0))}},customEvent:{getData:!0,setData:!0,changeData:!0},trigger:function(c,d,e,g){if(!e||e.nodeType!==3&&e.nodeType!==8){var h=c.type||c,i=[],j,k,l,m,n,o,p,q,r,s;if(E.test(h+f.event.triggered))return;h.indexOf("!")>=0&&(h=h.slice(0,-1),k=!0),h.indexOf(".")>=0&&(i=h.split("."),h=i.shift(),i.sort());if((!e||f.event.customEvent[h])&&!f.event.global[h])return;c=typeof c=="object"?c[f.expando]?c:new f.Event(h,c):new f.Event(h),c.type=h,c.isTrigger=!0,c.exclusive=k,c.namespace=i.join("."),c.namespace_re=c.namespace?new RegExp("(^|\\.)"+i.join("\\.(?:.*\\.)?")+"(\\.|$)"):null,o=h.indexOf(":")<0?"on"+h:"";if(!e){j=f.cache;for(l in j)j[l].events&&j[l].events[h]&&f.event.trigger(c,d,j[l].handle.elem,!0);return}c.result=b,c.target||(c.target=e),d=d!=null?f.makeArray(d):[],d.unshift(c),p=f.event.special[h]||{};if(p.trigger&&p.trigger.apply(e,d)===!1)return;r=[[e,p.bindType||h]];if(!g&&!p.noBubble&&!f.isWindow(e)){s=p.delegateType||h,m=E.test(s+h)?e:e.parentNode,n=null;for(;m;m=m.parentNode)r.push([m,s]),n=m;n&&n===e.ownerDocument&&r.push([n.defaultView||n.parentWindow||a,s])}for(l=0;l<r.length&&!c.isPropagationStopped();l++)m=r[l][0],c.type=r[l][1],q=(f._data(m,"events")||{})[c.type]&&f._data(m,"handle"),q&&q.apply(m,d),q=o&&m[o],q&&f.acceptData(m)&&q.apply(m,d)===!1&&c.preventDefault();c.type=h,!g&&!c.isDefaultPrevented()&&(!p._default||p._default.apply(e.ownerDocument,d)===!1)&&(h!=="click"||!f.nodeName(e,"a"))&&f.acceptData(e)&&o&&e[h]&&(h!=="focus"&&h!=="blur"||c.target.offsetWidth!==0)&&!f.isWindow(e)&&(n=e[o],n&&(e[o]=null),f.event.triggered=h,e[h](),f.event.triggered=b,n&&(e[o]=n));return c.result}},dispatch:function(c){c=f.event.fix(c||a.event);var d=(f._data(this,"events")||{})[c.type]||[],e=d.delegateCount,g=[].slice.call(arguments,0),h=!c.exclusive&&!c.namespace,i=[],j,k,l,m,n,o,p,q,r,s,t;g[0]=c,c.delegateTarget=this;if(e&&!c.target.disabled&&(!c.button||c.type!=="click")){m=f(this),m.context=this.ownerDocument||this;for(l=c.target;l!=this;l=l.parentNode||this){o={},q=[],m[0]=l;for(j=0;j<e;j++)r=d[j],s=r.selector,o[s]===b&&(o[s]=r.quick?H(l,r.quick):m.is(s)),o[s]&&q.push(r);q.length&&i.push({elem:l,matches:q})}}d.length>e&&i.push({elem:this,matches:d.slice(e)});for(j=0;j<i.length&&!c.isPropagationStopped();j++){p=i[j],c.currentTarget=p.elem;for(k=0;k<p.matches.length&&!c.isImmediatePropagationStopped();k++){r=p.matches[k];if(h||!c.namespace&&!r.namespace||c.namespace_re&&c.namespace_re.test(r.namespace))c.data=r.data,c.handleObj=r,n=((f.event.special[r.origType]||{}).handle||r.handler).apply(p.elem,g),n!==b&&(c.result=n,n===!1&&(c.preventDefault(),c.stopPropagation()))}}return c.result},props:"attrChange attrName relatedNode srcElement altKey bubbles cancelable ctrlKey currentTarget eventPhase metaKey relatedTarget shiftKey target timeStamp view which".split(" "),fixHooks:{},keyHooks:{props:"char charCode key keyCode".split(" "),filter:function(a,b){a.which==null&&(a.which=b.charCode!=null?b.charCode:b.keyCode);return a}},mouseHooks:{props:"button buttons clientX clientY fromElement offsetX offsetY pageX pageY screenX screenY toElement".split(" "),filter:function(a,d){var e,f,g,h=d.button,i=d.fromElement;a.pageX==null&&d.clientX!=null&&(e=a.target.ownerDocument||c,f=e.documentElement,g=e.body,a.pageX=d.clientX+(f&&f.scrollLeft||g&&g.scrollLeft||0)-(f&&f.clientLeft||g&&g.clientLeft||0),a.pageY=d.clientY+(f&&f.scrollTop||g&&g.scrollTop||0)-(f&&f.clientTop||g&&g.clientTop||0)),!a.relatedTarget&&i&&(a.relatedTarget=i===a.target?d.toElement:i),!a.which&&h!==b&&(a.which=h&1?1:h&2?3:h&4?2:0);return a}},fix:function(a){if(a[f.expando])return a;var d,e,g=a,h=f.event.fixHooks[a.type]||{},i=h.props?this.props.concat(h.props):this.props;a=f.Event(g);for(d=i.length;d;)e=i[--d],a[e]=g[e];a.target||(a.target=g.srcElement||c),a.target.nodeType===3&&(a.target=a.target.parentNode),a.metaKey===b&&(a.metaKey=a.ctrlKey);return h.filter?h.filter(a,g):a},special:{ready:{setup:f.bindReady},load:{noBubble:!0},focus:{delegateType:"focusin"},blur:{delegateType:"focusout"},beforeunload:{setup:function(a,b,c){f.isWindow(this)&&(this.onbeforeunload=c)},teardown:function(a,b){this.onbeforeunload===b&&(this.onbeforeunload=null)}}},simulate:function(a,b,c,d){var e=f.extend(new f.Event,c,{type:a,isSimulated:!0,originalEvent:{}});d?f.event.trigger(e,null,b):f.event.dispatch.call(b,e),e.isDefaultPrevented()&&c.preventDefault()}},f.event.handle=f.event.dispatch,f.removeEvent=c.removeEventListener?function(a,b,c){a.removeEventListener&&a.removeEventListener(b,c,!1)}:function(a,b,c){a.detachEvent&&a.detachEvent("on"+b,c)},f.Event=function(a,b){if(!(this instanceof f.Event))return new f.Event(a,b);a&&a.type?(this.originalEvent=a,this.type=a.type,this.isDefaultPrevented=a.defaultPrevented||a.returnValue===!1||a.getPreventDefault&&a.getPreventDefault()?K:J):this.type=a,b&&f.extend(this,b),this.timeStamp=a&&a.timeStamp||f.now(),this[f.expando]=!0},f.Event.prototype={preventDefault:function(){this.isDefaultPrevented=K;var a=this.originalEvent;!a||(a.preventDefault?a.preventDefault():a.returnValue=!1)},stopPropagation:function(){this.isPropagationStopped=K;var a=this.originalEvent;!a||(a.stopPropagation&&a.stopPropagation(),a.cancelBubble=!0)},stopImmediatePropagation:function(){this.isImmediatePropagationStopped=K,this.stopPropagation()},isDefaultPrevented:J,isPropagationStopped:J,isImmediatePropagationStopped:J},f.each({mouseenter:"mouseover",mouseleave:"mouseout"},function(a,b){f.event.special[a]={delegateType:b,bindType:b,handle:function(a){var c=this,d=a.relatedTarget,e=a.handleObj,g=e.selector,h;if(!d||d!==c&&!f.contains(c,d))a.type=e.origType,h=e.handler.apply(this,arguments),a.type=b;return h}}}),f.support.submitBubbles||(f.event.special.submit={setup:function(){if(f.nodeName(this,"form"))return!1;f.event.add(this,"click._submit keypress._submit",function(a){var c=a.target,d=f.nodeName(c,"input")||f.nodeName(c,"button")?c.form:b;d&&!d._submit_attached&&(f.event.add(d,"submit._submit",function(a){this.parentNode&&!a.isTrigger&&f.event.simulate("submit",this.parentNode,a,!0)}),d._submit_attached=!0)})},teardown:function(){if(f.nodeName(this,"form"))return!1;f.event.remove(this,"._submit")}}),f.support.changeBubbles||(f.event.special.change={setup:function(){if(z.test(this.nodeName)){if(this.type==="checkbox"||this.type==="radio")f.event.add(this,"propertychange._change",function(a){a.originalEvent.propertyName==="checked"&&(this._just_changed=!0)}),f.event.add(this,"click._change",function(a){this._just_changed&&!a.isTrigger&&(this._just_changed=!1,f.event.simulate("change",this,a,!0))});return!1}f.event.add(this,"beforeactivate._change",function(a){var b=a.target;z.test(b.nodeName)&&!b._change_attached&&(f.event.add(b,"change._change",function(a){this.parentNode&&!a.isSimulated&&!a.isTrigger&&f.event.simulate("change",this.parentNode,a,!0)}),b._change_attached=!0)})},handle:function(a){var b=a.target;if(this!==b||a.isSimulated||a.isTrigger||b.type!=="radio"&&b.type!=="checkbox")return a.handleObj.handler.apply(this,arguments)},teardown:function(){f.event.remove(this,"._change");return z.test(this.nodeName)}}),f.support.focusinBubbles||f.each({focus:"focusin",blur:"focusout"},function(a,b){var d=0,e=function(a){f.event.simulate(b,a.target,f.event.fix(a),!0)};f.event.special[b]={setup:function(){d++===0&&c.addEventListener(a,e,!0)},teardown:function(){--d===0&&c.removeEventListener(a,e,!0)}}}),f.fn.extend({on:function(a,c,d,e,g){var h,i;if(typeof a=="object"){typeof c!="string"&&(d=c,c=b);for(i in a)this.on(i,c,d,a[i],g);return this}d==null&&e==null?(e=c,d=c=b):e==null&&(typeof c=="string"?(e=d,d=b):(e=d,d=c,c=b));if(e===!1)e=J;else if(!e)return this;g===1&&(h=e,e=function(a){f().off(a);return h.apply(this,arguments)},e.guid=h.guid||(h.guid=f.guid++));return this.each(function(){f.event.add(this,a,e,d,c)})},one:function(a,b,c,d){return this.on.call(this,a,b,c,d,1)},off:function(a,c,d){if(a&&a.preventDefault&&a.handleObj){var e=a.handleObj;f(a.delegateTarget).off(e.namespace?e.type+"."+e.namespace:e.type,e.selector,e.handler);return this}if(typeof a=="object"){for(var g in a)this.off(g,c,a[g]);return this}if(c===!1||typeof c=="function")d=c,c=b;d===!1&&(d=J);return this.each(function(){f.event.remove(this,a,d,c)})},bind:function(a,b,c){return this.on(a,null,b,c)},unbind:function(a,b){return this.off(a,null,b)},live:function(a,b,c){f(this.context).on(a,this.selector,b,c);return this},die:function(a,b){f(this.context).off(a,this.selector||"**",b);return this},delegate:function(a,b,c,d){return this.on(b,a,c,d)},undelegate:function(a,b,c){return arguments.length==1?this.off(a,"**"):this.off(b,a,c)},trigger:function(a,b){return this.each(function(){f.event.trigger(a,b,this)})},triggerHandler:function(a,b){if(this[0])return f.event.trigger(a,b,this[0],!0)},toggle:function(a){var b=arguments,c=a.guid||f.guid++,d=0,e=function(c){var e=(f._data(this,"lastToggle"+a.guid)||0)%d;f._data(this,"lastToggle"+a.guid,e+1),c.preventDefault();return b[e].apply(this,arguments)||!1};e.guid=c;while(d<b.length)b[d++].guid=c;return this.click(e)},hover:function(a,b){return this.mouseenter(a).mouseleave(b||a)}}),f.each("blur focus focusin focusout load resize scroll unload click dblclick mousedown mouseup mousemove mouseover mouseout mouseenter mouseleave change select submit keydown keypress keyup error contextmenu".split(" "),function(a,b){f.fn[b]=function(a,c){c==null&&(c=a,a=null);return arguments.length>0?this.on(b,null,a,c):this.trigger(b)},f.attrFn&&(f.attrFn[b]=!0),C.test(b)&&(f.event.fixHooks[b]=f.event.keyHooks),D.test(b)&&(f.event.fixHooks[b]=f.event.mouseHooks)}),function(){function x(a,b,c,e,f,g){for(var h=0,i=e.length;h<i;h++){var j=e[h];if(j){var k=!1;j=j[a];while(j){if(j[d]===c){k=e[j.sizset];break}if(j.nodeType===1){g||(j[d]=c,j.sizset=h);if(typeof b!="string"){if(j===b){k=!0;break}}else if(m.filter(b,[j]).length>0){k=j;break}}j=j[a]}e[h]=k}}}function w(a,b,c,e,f,g){for(var h=0,i=e.length;h<i;h++){var j=e[h];if(j){var k=!1;j=j[a];while(j){if(j[d]===c){k=e[j.sizset];break}j.nodeType===1&&!g&&(j[d]=c,j.sizset=h);if(j.nodeName.toLowerCase()===b){k=j;break}j=j[a]}e[h]=k}}}var a=/((?:\((?:\([^()]+\)|[^()]+)+\)|\[(?:\[[^\[\]]*\]|['"][^'"]*['"]|[^\[\]'"]+)+\]|\\.|[^ >+~,(\[\\]+)+|[>+~])(\s*,\s*)?((?:.|\r|\n)*)/g,d="sizcache"+(Math.random()+"").replace(".",""),e=0,g=Object.prototype.toString,h=!1,i=!0,j=/\\/g,k=/\r\n/g,l=/\W/;[0,0].sort(function(){i=!1;return 0});var m=function(b,d,e,f){e=e||[],d=d||c;var h=d;if(d.nodeType!==1&&d.nodeType!==9)return[];if(!b||typeof b!="string")return e;var i,j,k,l,n,q,r,t,u=!0,v=m.isXML(d),w=[],x=b;do{a.exec(""),i=a.exec(x);if(i){x=i[3],w.push(i[1]);if(i[2]){l=i[3];break}}}while(i);if(w.length>1&&p.exec(b))if(w.length===2&&o.relative[w[0]])j=y(w[0]+w[1],d,f);else{j=o.relative[w[0]]?[d]:m(w.shift(),d);while(w.length)b=w.shift(),o.relative[b]&&(b+=w.shift()),j=y(b,j,f)}else{!f&&w.length>1&&d.nodeType===9&&!v&&o.match.ID.test(w[0])&&!o.match.ID.test(w[w.length-1])&&(n=m.find(w.shift(),d,v),d=n.expr?m.filter(n.expr,n.set)[0]:n.set[0]);if(d){n=f?{expr:w.pop(),set:s(f)}:m.find(w.pop(),w.length===1&&(w[0]==="~"||w[0]==="+")&&d.parentNode?d.parentNode:d,v),j=n.expr?m.filter(n.expr,n.set):n.set,w.length>0?k=s(j):u=!1;while(w.length)q=w.pop(),r=q,o.relative[q]?r=w.pop():q="",r==null&&(r=d),o.relative[q](k,r,v)}else k=w=[]}k||(k=j),k||m.error(q||b);if(g.call(k)==="[object Array]")if(!u)e.push.apply(e,k);else if(d&&d.nodeType===1)for(t=0;k[t]!=null;t++)k[t]&&(k[t]===!0||k[t].nodeType===1&&m.contains(d,k[t]))&&e.push(j[t]);else for(t=0;k[t]!=null;t++)k[t]&&k[t].nodeType===1&&e.push(j[t]);else s(k,e);l&&(m(l,h,e,f),m.uniqueSort(e));return e};m.uniqueSort=function(a){if(u){h=i,a.sort(u);if(h)for(var b=1;b<a.length;b++)a[b]===a[b-1]&&a.splice(b--,1)}return a},m.matches=function(a,b){return m(a,null,null,b)},m.matchesSelector=function(a,b){return m(b,null,null,[a]).length>0},m.find=function(a,b,c){var d,e,f,g,h,i;if(!a)return[];for(e=0,f=o.order.length;e<f;e++){h=o.order[e];if(g=o.leftMatch[h].exec(a)){i=g[1],g.splice(1,1);if(i.substr(i.length-1)!=="\\"){g[1]=(g[1]||"").replace(j,""),d=o.find[h](g,b,c);if(d!=null){a=a.replace(o.match[h],"");break}}}}d||(d=typeof b.getElementsByTagName!="undefined"?b.getElementsByTagName("*"):[]);return{set:d,expr:a}},m.filter=function(a,c,d,e){var f,g,h,i,j,k,l,n,p,q=a,r=[],s=c,t=c&&c[0]&&m.isXML(c[0]);while(a&&c.length){for(h in o.filter)if((f=o.leftMatch[h].exec(a))!=null&&f[2]){k=o.filter[h],l=f[1],g=!1,f.splice(1,1);if(l.substr(l.length-1)==="\\")continue;s===r&&(r=[]);if(o.preFilter[h]){f=o.preFilter[h](f,s,d,r,e,t);if(!f)g=i=!0;else if(f===!0)continue}if(f)for(n=0;(j=s[n])!=null;n++)j&&(i=k(j,f,n,s),p=e^i,d&&i!=null?p?g=!0:s[n]=!1:p&&(r.push(j),g=!0));if(i!==b){d||(s=r),a=a.replace(o.match[h],"");if(!g)return[];break}}if(a===q)if(g==null)m.error(a);else break;q=a}return s},m.error=function(a){throw new Error("Syntax error, unrecognized expression: "+a)};var n=m.getText=function(a){var b,c,d=a.nodeType,e="";if(d){if(d===1||d===9){if(typeof a.textContent=="string")return a.textContent;if(typeof a.innerText=="string")return a.innerText.replace(k,"");for(a=a.firstChild;a;a=a.nextSibling)e+=n(a)}else if(d===3||d===4)return a.nodeValue}else for(b=0;c=a[b];b++)c.nodeType!==8&&(e+=n(c));return e},o=m.selectors={order:["ID","NAME","TAG"],match:{ID:/#((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,CLASS:/\.((?:[\w\u00c0-\uFFFF\-]|\\.)+)/,NAME:/\[name=['"]*((?:[\w\u00c0-\uFFFF\-]|\\.)+)['"]*\]/,ATTR:/\[\s*((?:[\w\u00c0-\uFFFF\-]|\\.)+)\s*(?:(\S?=)\s*(?:(['"])(.*?)\3|(#?(?:[\w\u00c0-\uFFFF\-]|\\.)*)|)|)\s*\]/,TAG:/^((?:[\w\u00c0-\uFFFF\*\-]|\\.)+)/,CHILD:/:(only|nth|last|first)-child(?:\(\s*(even|odd|(?:[+\-]?\d+|(?:[+\-]?\d*)?n\s*(?:[+\-]\s*\d+)?))\s*\))?/,POS:/:(nth|eq|gt|lt|first|last|even|odd)(?:\((\d*)\))?(?=[^\-]|$)/,PSEUDO:/:((?:[\w\u00c0-\uFFFF\-]|\\.)+)(?:\((['"]?)((?:\([^\)]+\)|[^\(\)]*)+)\2\))?/},leftMatch:{},attrMap:{"class":"className","for":"htmlFor"},attrHandle:{href:function(a){return a.getAttribute("href")},type:function(a){return a.getAttribute("type")}},relative:{"+":function(a,b){var c=typeof b=="string",d=c&&!l.test(b),e=c&&!d;d&&(b=b.toLowerCase());for(var f=0,g=a.length,h;f<g;f++)if(h=a[f]){while((h=h.previousSibling)&&h.nodeType!==1);a[f]=e||h&&h.nodeName.toLowerCase()===b?h||!1:h===b}e&&m.filter(b,a,!0)},">":function(a,b){var c,d=typeof b=="string",e=0,f=a.length;if(d&&!l.test(b)){b=b.toLowerCase();for(;e<f;e++){c=a[e];if(c){var g=c.parentNode;a[e]=g.nodeName.toLowerCase()===b?g:!1}}}else{for(;e<f;e++)c=a[e],c&&(a[e]=d?c.parentNode:c.parentNode===b);d&&m.filter(b,a,!0)}},"":function(a,b,c){var d,f=e++,g=x;typeof b=="string"&&!l.test(b)&&(b=b.toLowerCase(),d=b,g=w),g("parentNode",b,f,a,d,c)},"~":function(a,b,c){var d,f=e++,g=x;typeof b=="string"&&!l.test(b)&&(b=b.toLowerCase(),d=b,g=w),g("previousSibling",b,f,a,d,c)}},find:{ID:function(a,b,c){if(typeof b.getElementById!="undefined"&&!c){var d=b.getElementById(a[1]);return d&&d.parentNode?[d]:[]}},NAME:function(a,b){if(typeof b.getElementsByName!="undefined"){var c=[],d=b.getElementsByName(a[1]);for(var e=0,f=d.length;e<f;e++)d[e].getAttribute("name")===a[1]&&c.push(d[e]);return c.length===0?null:c}},TAG:function(a,b){if(typeof b.getElementsByTagName!="undefined")return b.getElementsByTagName(a[1])}},preFilter:{CLASS:function(a,b,c,d,e,f){a=" "+a[1].replace(j,"")+" ";if(f)return a;for(var g=0,h;(h=b[g])!=null;g++)h&&(e^(h.className&&(" "+h.className+" ").replace(/[\t\n\r]/g," ").indexOf(a)>=0)?c||d.push(h):c&&(b[g]=!1));return!1},ID:function(a){return a[1].replace(j,"")},TAG:function(a,b){return a[1].replace(j,"").toLowerCase()},CHILD:function(a){if(a[1]==="nth"){a[2]||m.error(a[0]),a[2]=a[2].replace(/^\+|\s*/g,"");var b=/(-?)(\d*)(?:n([+\-]?\d*))?/.exec(a[2]==="even"&&"2n"||a[2]==="odd"&&"2n+1"||!/\D/.test(a[2])&&"0n+"+a[2]||a[2]);a[2]=b[1]+(b[2]||1)-0,a[3]=b[3]-0}else a[2]&&m.error(a[0]);a[0]=e++;return a},ATTR:function(a,b,c,d,e,f){var g=a[1]=a[1].replace(j,"");!f&&o.attrMap[g]&&(a[1]=o.attrMap[g]),a[4]=(a[4]||a[5]||"").replace(j,""),a[2]==="~="&&(a[4]=" "+a[4]+" ");return a},PSEUDO:function(b,c,d,e,f){if(b[1]==="not")if((a.exec(b[3])||"").length>1||/^\w/.test(b[3]))b[3]=m(b[3],null,null,c);else{var g=m.filter(b[3],c,d,!0^f);d||e.push.apply(e,g);return!1}else if(o.match.POS.test(b[0])||o.match.CHILD.test(b[0]))return!0;return b},POS:function(a){a.unshift(!0);return a}},filters:{enabled:function(a){return a.disabled===!1&&a.type!=="hidden"},disabled:function(a){return a.disabled===!0},checked:function(a){return a.checked===!0},selected:function(a){a.parentNode&&a.parentNode.selectedIndex;return a.selected===!0},parent:function(a){return!!a.firstChild},empty:function(a){return!a.firstChild},has:function(a,b,c){return!!m(c[3],a).length},header:function(a){return/h\d/i.test(a.nodeName)},text:function(a){var b=a.getAttribute("type"),c=a.type;return a.nodeName.toLowerCase()==="input"&&"text"===c&&(b===c||b===null)},radio:function(a){return a.nodeName.toLowerCase()==="input"&&"radio"===a.type},checkbox:function(a){return a.nodeName.toLowerCase()==="input"&&"checkbox"===a.type},file:function(a){return a.nodeName.toLowerCase()==="input"&&"file"===a.type},password:function(a){return a.nodeName.toLowerCase()==="input"&&"password"===a.type},submit:function(a){var b=a.nodeName.toLowerCase();return(b==="input"||b==="button")&&"submit"===a.type},image:function(a){return a.nodeName.toLowerCase()==="input"&&"image"===a.type},reset:function(a){var b=a.nodeName.toLowerCase();return(b==="input"||b==="button")&&"reset"===a.type},button:function(a){var b=a.nodeName.toLowerCase();return b==="input"&&"button"===a.type||b==="button"},input:function(a){return/input|select|textarea|button/i.test(a.nodeName)},focus:function(a){return a===a.ownerDocument.activeElement}},setFilters:{first:function(a,b){return b===0},last:function(a,b,c,d){return b===d.length-1},even:function(a,b){return b%2===0},odd:function(a,b){return b%2===1},lt:function(a,b,c){return b<c[3]-0},gt:function(a,b,c){return b>c[3]-0},nth:function(a,b,c){return c[3]-0===b},eq:function(a,b,c){return c[3]-0===b}},filter:{PSEUDO:function(a,b,c,d){var e=b[1],f=o.filters[e];if(f)return f(a,c,b,d);if(e==="contains")return(a.textContent||a.innerText||n([a])||"").indexOf(b[3])>=0;if(e==="not"){var g=b[3];for(var h=0,i=g.length;h<i;h++)if(g[h]===a)return!1;return!0}m.error(e)},CHILD:function(a,b){var c,e,f,g,h,i,j,k=b[1],l=a;switch(k){case"only":case"first":while(l=l.previousSibling)if(l.nodeType===1)return!1;if(k==="first")return!0;l=a;case"last":while(l=l.nextSibling)if(l.nodeType===1)return!1;return!0;case"nth":c=b[2],e=b[3];if(c===1&&e===0)return!0;f=b[0],g=a.parentNode;if(g&&(g[d]!==f||!a.nodeIndex)){i=0;for(l=g.firstChild;l;l=l.nextSibling)l.nodeType===1&&(l.nodeIndex=++i);g[d]=f}j=a.nodeIndex-e;return c===0?j===0:j%c===0&&j/c>=0}},ID:function(a,b){return a.nodeType===1&&a.getAttribute("id")===b},TAG:function(a,b){return b==="*"&&a.nodeType===1||!!a.nodeName&&a.nodeName.toLowerCase()===b},CLASS:function(a,b){return(" "+(a.className||a.getAttribute("class"))+" ").indexOf(b)>-1},ATTR:function(a,b){var c=b[1],d=m.attr?m.attr(a,c):o.attrHandle[c]?o.attrHandle[c](a):a[c]!=null?a[c]:a.getAttribute(c),e=d+"",f=b[2],g=b[4];return d==null?f==="!=":!f&&m.attr?d!=null:f==="="?e===g:f==="*="?e.indexOf(g)>=0:f==="~="?(" "+e+" ").indexOf(g)>=0:g?f==="!="?e!==g:f==="^="?e.indexOf(g)===0:f==="$="?e.substr(e.length-g.length)===g:f==="|="?e===g||e.substr(0,g.length+1)===g+"-":!1:e&&d!==!1},POS:function(a,b,c,d){var e=b[2],f=o.setFilters[e];if(f)return f(a,c,b,d)}}},p=o.match.POS,q=function(a,b){return"\\"+(b-0+1)};for(var r in o.match)o.match[r]=new RegExp(o.match[r].source+/(?![^\[]*\])(?![^\(]*\))/.source),o.leftMatch[r]=new RegExp(/(^(?:.|\r|\n)*?)/.source+o.match[r].source.replace(/\\(\d+)/g,q));var s=function(a,b){a=Array.prototype.slice.call(a,0);if(b){b.push.apply(b,a);return b}return a};try{Array.prototype.slice.call(c.documentElement.childNodes,0)[0].nodeType}catch(t){s=function(a,b){var c=0,d=b||[];if(g.call(a)==="[object Array]")Array.prototype.push.apply(d,a);else if(typeof a.length=="number")for(var e=a.length;c<e;c++)d.push(a[c]);else for(;a[c];c++)d.push(a[c]);return d}}var u,v;c.documentElement.compareDocumentPosition?u=function(a,b){if(a===b){h=!0;return 0}if(!a.compareDocumentPosition||!b.compareDocumentPosition)return a.compareDocumentPosition?-1:1;return a.compareDocumentPosition(b)&4?-1:1}:(u=function(a,b){if(a===b){h=!0;return 0}if(a.sourceIndex&&b.sourceIndex)return a.sourceIndex-b.sourceIndex;var c,d,e=[],f=[],g=a.parentNode,i=b.parentNode,j=g;if(g===i)return v(a,b);if(!g)return-1;if(!i)return 1;while(j)e.unshift(j),j=j.parentNode;j=i;while(j)f.unshift(j),j=j.parentNode;c=e.length,d=f.length;for(var k=0;k<c&&k<d;k++)if(e[k]!==f[k])return v(e[k],f[k]);return k===c?v(a,f[k],-1):v(e[k],b,1)},v=function(a,b,c){if(a===b)return c;var d=a.nextSibling;while(d){if(d===b)return-1;d=d.nextSibling}return 1}),function(){var a=c.createElement("div"),d="script"+(new Date).getTime(),e=c.documentElement;a.innerHTML="<a name='"+d+"'/>",e.insertBefore(a,e.firstChild),c.getElementById(d)&&(o.find.ID=function(a,c,d){if(typeof c.getElementById!="undefined"&&!d){var e=c.getElementById(a[1]);return e?e.id===a[1]||typeof e.getAttributeNode!="undefined"&&e.getAttributeNode("id").nodeValue===a[1]?[e]:b:[]}},o.filter.ID=function(a,b){var c=typeof a.getAttributeNode!="undefined"&&a.getAttributeNode("id");return a.nodeType===1&&c&&c.nodeValue===b}),e.removeChild(a),e=a=null}(),function(){var a=c.createElement("div");a.appendChild(c.createComment("")),a.getElementsByTagName("*").length>0&&(o.find.TAG=function(a,b){var c=b.getElementsByTagName(a[1]);if(a[1]==="*"){var d=[];for(var e=0;c[e];e++)c[e].nodeType===1&&d.push(c[e]);c=d}return c}),a.innerHTML="<a href='#'></a>",a.firstChild&&typeof a.firstChild.getAttribute!="undefined"&&a.firstChild.getAttribute("href")!=="#"&&(o.attrHandle.href=function(a){return a.getAttribute("href",2)}),a=null}(),c.querySelectorAll&&function(){var a=m,b=c.createElement("div"),d="__sizzle__";b.innerHTML="<p class='TEST'></p>";if(!b.querySelectorAll||b.querySelectorAll(".TEST").length!==0){m=function(b,e,f,g){e=e||c;if(!g&&!m.isXML(e)){var h=/^(\w+$)|^\.([\w\-]+$)|^#([\w\-]+$)/.exec(b);if(h&&(e.nodeType===1||e.nodeType===9)){if(h[1])return s(e.getElementsByTagName(b),f);if(h[2]&&o.find.CLASS&&e.getElementsByClassName)return s(e.getElementsByClassName(h[2]),f)}if(e.nodeType===9){if(b==="body"&&e.body)return s([e.body],f);if(h&&h[3]){var i=e.getElementById(h[3]);if(!i||!i.parentNode)return s([],f);if(i.id===h[3])return s([i],f)}try{return s(e.querySelectorAll(b),f)}catch(j){}}else if(e.nodeType===1&&e.nodeName.toLowerCase()!=="object"){var k=e,l=e.getAttribute("id"),n=l||d,p=e.parentNode,q=/^\s*[+~]/.test(b);l?n=n.replace(/'/g,"\\$&"):e.setAttribute("id",n),q&&p&&(e=e.parentNode);try{if(!q||p)return s(e.querySelectorAll("[id='"+n+"'] "+b),f)}catch(r){}finally{l||k.removeAttribute("id")}}}return a(b,e,f,g)};for(var e in a)m[e]=a[e];b=null}}(),function(){var a=c.documentElement,b=a.matchesSelector||a.mozMatchesSelector||a.webkitMatchesSelector||a.msMatchesSelector;if(b){var d=!b.call(c.createElement("div"),"div"),e=!1;try{b.call(c.documentElement,"[test!='']:sizzle")}catch(f){e=!0}m.matchesSelector=function(a,c){c=c.replace(/\=\s*([^'"\]]*)\s*\]/g,"='$1']");if(!m.isXML(a))try{if(e||!o.match.PSEUDO.test(c)&&!/!=/.test(c)){var f=b.call(a,c);if(f||!d||a.document&&a.document.nodeType!==11)return f}}catch(g){}return m(c,null,null,[a]).length>0}}}(),function(){var a=c.createElement("div");a.innerHTML="<div class='test e'></div><div class='test'></div>";if(!!a.getElementsByClassName&&a.getElementsByClassName("e").length!==0){a.lastChild.className="e";if(a.getElementsByClassName("e").length===1)return;o.order.splice(1,0,"CLASS"),o.find.CLASS=function(a,b,c){if(typeof b.getElementsByClassName!="undefined"&&!c)return b.getElementsByClassName(a[1])},a=null}}(),c.documentElement.contains?m.contains=function(a,b){return a!==b&&(a.contains?a.contains(b):!0)}:c.documentElement.compareDocumentPosition?m.contains=function(a,b){return!!(a.compareDocumentPosition(b)&16)}:m.contains=function(){return!1},m.isXML=function(a){var b=(a?a.ownerDocument||a:0).documentElement;return b?b.nodeName!=="HTML":!1};var y=function(a,b,c){var d,e=[],f="",g=b.nodeType?[b]:b;while(d=o.match.PSEUDO.exec(a))f+=d[0],a=a.replace(o.match.PSEUDO,"");a=o.relative[a]?a+"*":a;for(var h=0,i=g.length;h<i;h++)m(a,g[h],e,c);return m.filter(f,e)};m.attr=f.attr,m.selectors.attrMap={},f.find=m,f.expr=m.selectors,f.expr[":"]=f.expr.filters,f.unique=m.uniqueSort,f.text=m.getText,f.isXMLDoc=m.isXML,f.contains=m.contains}();var L=/Until$/,M=/^(?:parents|prevUntil|prevAll)/,N=/,/,O=/^.[^:#\[\.,]*$/,P=Array.prototype.slice,Q=f.expr.match.POS,R={children:!0,contents:!0,next:!0,prev:!0};f.fn.extend({find:function(a){var b=this,c,d;if(typeof a!="string")return f(a).filter(function(){for(c=0,d=b.length;c<d;c++)if(f.contains(b[c],this))return!0});var e=this.pushStack("","find",a),g,h,i;for(c=0,d=this.length;c<d;c++){g=e.length,f.find(a,this[c],e);if(c>0)for(h=g;h<e.length;h++)for(i=0;i<g;i++)if(e[i]===e[h]){e.splice(h--,1);break}}return e},has:function(a){var b=f(a);return this.filter(function(){for(var a=0,c=b.length;a<c;a++)if(f.contains(this,b[a]))return!0})},not:function(a){return this.pushStack(T(this,a,!1),"not",a)},filter:function(a){return this.pushStack(T(this,a,!0),"filter",a)},is:function(a){return!!a&&(typeof a=="string"?Q.test(a)?f(a,this.context).index(this[0])>=0:f.filter(a,this).length>0:this.filter(a).length>0)},closest:function(a,b){var c=[],d,e,g=this[0];if(f.isArray(a)){var h=1;while(g&&g.ownerDocument&&g!==b){for(d=0;d<a.length;d++)f(g).is(a[d])&&c.push({selector:a[d],elem:g,level:h});g=g.parentNode,h++}return c}var i=Q.test(a)||typeof a!="string"?f(a,b||this.context):0;for(d=0,e=this.length;d<e;d++){g=this[d];while(g){if(i?i.index(g)>-1:f.find.matchesSelector(g,a)){c.push(g);break}g=g.parentNode;if(!g||!g.ownerDocument||g===b||g.nodeType===11)break}}c=c.length>1?f.unique(c):c;return this.pushStack(c,"closest",a)},index:function(a){if(!a)return this[0]&&this[0].parentNode?this.prevAll().length:-1;if(typeof a=="string")return f.inArray(this[0],f(a));return f.inArray(a.jquery?a[0]:a,this)},add:function(a,b){var c=typeof a=="string"?f(a,b):f.makeArray(a&&a.nodeType?[a]:a),d=f.merge(this.get(),c);return this.pushStack(S(c[0])||S(d[0])?d:f.unique(d))},andSelf:function(){return this.add(this.prevObject)}}),f.each({parent:function(a){var b=a.parentNode;return b&&b.nodeType!==11?b:null},parents:function(a){return f.dir(a,"parentNode")},parentsUntil:function(a,b,c){return f.dir(a,"parentNode",c)},next:function(a){return f.nth(a,2,"nextSibling")},prev:function(a){return f.nth(a,2,"previousSibling")},nextAll:function(a){return f.dir(a,"nextSibling")},prevAll:function(a){return f.dir(a,"previousSibling")},nextUntil:function(a,b,c){return f.dir(a,"nextSibling",c)},prevUntil:function(a,b,c){return f.dir(a,"previousSibling",c)},siblings:function(a){return f.sibling(a.parentNode.firstChild,a)},children:function(a){return f.sibling(a.firstChild)},contents:function(a){return f.nodeName(a,"iframe")?a.contentDocument||a.contentWindow.document:f.makeArray(a.childNodes)}},function(a,b){f.fn[a]=function(c,d){var e=f.map(this,b,c);L.test(a)||(d=c),d&&typeof d=="string"&&(e=f.filter(d,e)),e=this.length>1&&!R[a]?f.unique(e):e,(this.length>1||N.test(d))&&M.test(a)&&(e=e.reverse());return this.pushStack(e,a,P.call(arguments).join(","))}}),f.extend({filter:function(a,b,c){c&&(a=":not("+a+")");return b.length===1?f.find.matchesSelector(b[0],a)?[b[0]]:[]:f.find.matches(a,b)},dir:function(a,c,d){var e=[],g=a[c];while(g&&g.nodeType!==9&&(d===b||g.nodeType!==1||!f(g).is(d)))g.nodeType===1&&e.push(g),g=g[c];return e},nth:function(a,b,c,d){b=b||1;var e=0;for(;a;a=a[c])if(a.nodeType===1&&++e===b)break;return a},sibling:function(a,b){var c=[];for(;a;a=a.nextSibling)a.nodeType===1&&a!==b&&c.push(a);return c}});var V="abbr|article|aside|audio|canvas|datalist|details|figcaption|figure|footer|header|hgroup|mark|meter|nav|output|progress|section|summary|time|video",W=/ jQuery\d+="(?:\d+|null)"/g,X=/^\s+/,Y=/<(?!area|br|col|embed|hr|img|input|link|meta|param)(([\w:]+)[^>]*)\/>/ig,Z=/<([\w:]+)/,$=/<tbody/i,_=/<|&#?\w+;/,ba=/<(?:script|style)/i,bb=/<(?:script|object|embed|option|style)/i,bc=new RegExp("<(?:"+V+")","i"),bd=/checked\s*(?:[^=]|=\s*.checked.)/i,be=/\/(java|ecma)script/i,bf=/^\s*<!(?:\[CDATA\[|\-\-)/,bg={option:[1,"<select multiple='multiple'>","</select>"],legend:[1,"<fieldset>","</fieldset>"],thead:[1,"<table>","</table>"],tr:[2,"<table><tbody>","</tbody></table>"],td:[3,"<table><tbody><tr>","</tr></tbody></table>"],col:[2,"<table><tbody></tbody><colgroup>","</colgroup></table>"],area:[1,"<map>","</map>"],_default:[0,"",""]},bh=U(c);bg.optgroup=bg.option,bg.tbody=bg.tfoot=bg.colgroup=bg.caption=bg.thead,bg.th=bg.td,f.support.htmlSerialize||(bg._default=[1,"div<div>","</div>"]),f.fn.extend({text:function(a){if(f.isFunction(a))return this.each(function(b){var c=f(this);c.text(a.call(this,b,c.text()))});if(typeof a!="object"&&a!==b)return this.empty().append((this[0]&&this[0].ownerDocument||c).createTextNode(a));return f.text(this)},wrapAll:function(a){if(f.isFunction(a))return this.each(function(b){f(this).wrapAll(a.call(this,b))});if(this[0]){var b=f(a,this[0].ownerDocument).eq(0).clone(!0);this[0].parentNode&&b.insertBefore(this[0]),b.map(function(){var a=this;while(a.firstChild&&a.firstChild.nodeType===1)a=a.firstChild;return a}).append(this)}return this},wrapInner:function(a){if(f.isFunction(a))return this.each(function(b){f(this).wrapInner(a.call(this,b))});return this.each(function(){var b=f(this),c=b.contents();c.length?c.wrapAll(a):b.append(a)})},wrap:function(a){var b=f.isFunction(a);return this.each(function(c){f(this).wrapAll(b?a.call(this,c):a)})},unwrap:function(){return this.parent().each(function(){f.nodeName(this,"body")||f(this).replaceWith(this.childNodes)}).end()},append:function(){return this.domManip(arguments,!0,function(a){this.nodeType===1&&this.appendChild(a)})},prepend:function(){return this.domManip(arguments,!0,function(a){this.nodeType===1&&this.insertBefore(a,this.firstChild)})},before:function(){if(this[0]&&this[0].parentNode)return this.domManip(arguments,!1,function(a){this.parentNode.insertBefore(a,this)});if(arguments.length){var a=f.clean(arguments);a.push.apply(a,this.toArray());return this.pushStack(a,"before",arguments)}},after:function(){if(this[0]&&this[0].parentNode)return this.domManip(arguments,!1,function(a){this.parentNode.insertBefore(a,this.nextSibling)});if(arguments.length){var a=this.pushStack(this,"after",arguments);a.push.apply(a,f.clean(arguments));return a}},remove:function(a,b){for(var c=0,d;(d=this[c])!=null;c++)if(!a||f.filter(a,[d]).length)!b&&d.nodeType===1&&(f.cleanData(d.getElementsByTagName("*")),f.cleanData([d])),d.parentNode&&d.parentNode.removeChild(d);return this},empty:function()
{for(var a=0,b;(b=this[a])!=null;a++){b.nodeType===1&&f.cleanData(b.getElementsByTagName("*"));while(b.firstChild)b.removeChild(b.firstChild)}return this},clone:function(a,b){a=a==null?!1:a,b=b==null?a:b;return this.map(function(){return f.clone(this,a,b)})},html:function(a){if(a===b)return this[0]&&this[0].nodeType===1?this[0].innerHTML.replace(W,""):null;if(typeof a=="string"&&!ba.test(a)&&(f.support.leadingWhitespace||!X.test(a))&&!bg[(Z.exec(a)||["",""])[1].toLowerCase()]){a=a.replace(Y,"<$1></$2>");try{for(var c=0,d=this.length;c<d;c++)this[c].nodeType===1&&(f.cleanData(this[c].getElementsByTagName("*")),this[c].innerHTML=a)}catch(e){this.empty().append(a)}}else f.isFunction(a)?this.each(function(b){var c=f(this);c.html(a.call(this,b,c.html()))}):this.empty().append(a);return this},replaceWith:function(a){if(this[0]&&this[0].parentNode){if(f.isFunction(a))return this.each(function(b){var c=f(this),d=c.html();c.replaceWith(a.call(this,b,d))});typeof a!="string"&&(a=f(a).detach());return this.each(function(){var b=this.nextSibling,c=this.parentNode;f(this).remove(),b?f(b).before(a):f(c).append(a)})}return this.length?this.pushStack(f(f.isFunction(a)?a():a),"replaceWith",a):this},detach:function(a){return this.remove(a,!0)},domManip:function(a,c,d){var e,g,h,i,j=a[0],k=[];if(!f.support.checkClone&&arguments.length===3&&typeof j=="string"&&bd.test(j))return this.each(function(){f(this).domManip(a,c,d,!0)});if(f.isFunction(j))return this.each(function(e){var g=f(this);a[0]=j.call(this,e,c?g.html():b),g.domManip(a,c,d)});if(this[0]){i=j&&j.parentNode,f.support.parentNode&&i&&i.nodeType===11&&i.childNodes.length===this.length?e={fragment:i}:e=f.buildFragment(a,this,k),h=e.fragment,h.childNodes.length===1?g=h=h.firstChild:g=h.firstChild;if(g){c=c&&f.nodeName(g,"tr");for(var l=0,m=this.length,n=m-1;l<m;l++)d.call(c?bi(this[l],g):this[l],e.cacheable||m>1&&l<n?f.clone(h,!0,!0):h)}k.length&&f.each(k,bp)}return this}}),f.buildFragment=function(a,b,d){var e,g,h,i,j=a[0];b&&b[0]&&(i=b[0].ownerDocument||b[0]),i.createDocumentFragment||(i=c),a.length===1&&typeof j=="string"&&j.length<512&&i===c&&j.charAt(0)==="<"&&!bb.test(j)&&(f.support.checkClone||!bd.test(j))&&(f.support.html5Clone||!bc.test(j))&&(g=!0,h=f.fragments[j],h&&h!==1&&(e=h)),e||(e=i.createDocumentFragment(),f.clean(a,i,e,d)),g&&(f.fragments[j]=h?e:1);return{fragment:e,cacheable:g}},f.fragments={},f.each({appendTo:"append",prependTo:"prepend",insertBefore:"before",insertAfter:"after",replaceAll:"replaceWith"},function(a,b){f.fn[a]=function(c){var d=[],e=f(c),g=this.length===1&&this[0].parentNode;if(g&&g.nodeType===11&&g.childNodes.length===1&&e.length===1){e[b](this[0]);return this}for(var h=0,i=e.length;h<i;h++){var j=(h>0?this.clone(!0):this).get();f(e[h])[b](j),d=d.concat(j)}return this.pushStack(d,a,e.selector)}}),f.extend({clone:function(a,b,c){var d,e,g,h=f.support.html5Clone||!bc.test("<"+a.nodeName)?a.cloneNode(!0):bo(a);if((!f.support.noCloneEvent||!f.support.noCloneChecked)&&(a.nodeType===1||a.nodeType===11)&&!f.isXMLDoc(a)){bk(a,h),d=bl(a),e=bl(h);for(g=0;d[g];++g)e[g]&&bk(d[g],e[g])}if(b){bj(a,h);if(c){d=bl(a),e=bl(h);for(g=0;d[g];++g)bj(d[g],e[g])}}d=e=null;return h},clean:function(a,b,d,e){var g;b=b||c,typeof b.createElement=="undefined"&&(b=b.ownerDocument||b[0]&&b[0].ownerDocument||c);var h=[],i;for(var j=0,k;(k=a[j])!=null;j++){typeof k=="number"&&(k+="");if(!k)continue;if(typeof k=="string")if(!_.test(k))k=b.createTextNode(k);else{k=k.replace(Y,"<$1></$2>");var l=(Z.exec(k)||["",""])[1].toLowerCase(),m=bg[l]||bg._default,n=m[0],o=b.createElement("div");b===c?bh.appendChild(o):U(b).appendChild(o),o.innerHTML=m[1]+k+m[2];while(n--)o=o.lastChild;if(!f.support.tbody){var p=$.test(k),q=l==="table"&&!p?o.firstChild&&o.firstChild.childNodes:m[1]==="<table>"&&!p?o.childNodes:[];for(i=q.length-1;i>=0;--i)f.nodeName(q[i],"tbody")&&!q[i].childNodes.length&&q[i].parentNode.removeChild(q[i])}!f.support.leadingWhitespace&&X.test(k)&&o.insertBefore(b.createTextNode(X.exec(k)[0]),o.firstChild),k=o.childNodes}var r;if(!f.support.appendChecked)if(k[0]&&typeof (r=k.length)=="number")for(i=0;i<r;i++)bn(k[i]);else bn(k);k.nodeType?h.push(k):h=f.merge(h,k)}if(d){g=function(a){return!a.type||be.test(a.type)};for(j=0;h[j];j++)if(e&&f.nodeName(h[j],"script")&&(!h[j].type||h[j].type.toLowerCase()==="text/javascript"))e.push(h[j].parentNode?h[j].parentNode.removeChild(h[j]):h[j]);else{if(h[j].nodeType===1){var s=f.grep(h[j].getElementsByTagName("script"),g);h.splice.apply(h,[j+1,0].concat(s))}d.appendChild(h[j])}}return h},cleanData:function(a){var b,c,d=f.cache,e=f.event.special,g=f.support.deleteExpando;for(var h=0,i;(i=a[h])!=null;h++){if(i.nodeName&&f.noData[i.nodeName.toLowerCase()])continue;c=i[f.expando];if(c){b=d[c];if(b&&b.events){for(var j in b.events)e[j]?f.event.remove(i,j):f.removeEvent(i,j,b.handle);b.handle&&(b.handle.elem=null)}g?delete i[f.expando]:i.removeAttribute&&i.removeAttribute(f.expando),delete d[c]}}}});var bq=/alpha\([^)]*\)/i,br=/opacity=([^)]*)/,bs=/([A-Z]|^ms)/g,bt=/^-?\d+(?:px)?$/i,bu=/^-?\d/,bv=/^([\-+])=([\-+.\de]+)/,bw={position:"absolute",visibility:"hidden",display:"block"},bx=["Left","Right"],by=["Top","Bottom"],bz,bA,bB;f.fn.css=function(a,c){if(arguments.length===2&&c===b)return this;return f.access(this,a,c,!0,function(a,c,d){return d!==b?f.style(a,c,d):f.css(a,c)})},f.extend({cssHooks:{opacity:{get:function(a,b){if(b){var c=bz(a,"opacity","opacity");return c===""?"1":c}return a.style.opacity}}},cssNumber:{fillOpacity:!0,fontWeight:!0,lineHeight:!0,opacity:!0,orphans:!0,widows:!0,zIndex:!0,zoom:!0},cssProps:{"float":f.support.cssFloat?"cssFloat":"styleFloat"},style:function(a,c,d,e){if(!!a&&a.nodeType!==3&&a.nodeType!==8&&!!a.style){var g,h,i=f.camelCase(c),j=a.style,k=f.cssHooks[i];c=f.cssProps[i]||i;if(d===b){if(k&&"get"in k&&(g=k.get(a,!1,e))!==b)return g;return j[c]}h=typeof d,h==="string"&&(g=bv.exec(d))&&(d=+(g[1]+1)*+g[2]+parseFloat(f.css(a,c)),h="number");if(d==null||h==="number"&&isNaN(d))return;h==="number"&&!f.cssNumber[i]&&(d+="px");if(!k||!("set"in k)||(d=k.set(a,d))!==b)try{j[c]=d}catch(l){}}},css:function(a,c,d){var e,g;c=f.camelCase(c),g=f.cssHooks[c],c=f.cssProps[c]||c,c==="cssFloat"&&(c="float");if(g&&"get"in g&&(e=g.get(a,!0,d))!==b)return e;if(bz)return bz(a,c)},swap:function(a,b,c){var d={};for(var e in b)d[e]=a.style[e],a.style[e]=b[e];c.call(a);for(e in b)a.style[e]=d[e]}}),f.curCSS=f.css,f.each(["height","width"],function(a,b){f.cssHooks[b]={get:function(a,c,d){var e;if(c){if(a.offsetWidth!==0)return bC(a,b,d);f.swap(a,bw,function(){e=bC(a,b,d)});return e}},set:function(a,b){if(!bt.test(b))return b;b=parseFloat(b);if(b>=0)return b+"px"}}}),f.support.opacity||(f.cssHooks.opacity={get:function(a,b){return br.test((b&&a.currentStyle?a.currentStyle.filter:a.style.filter)||"")?parseFloat(RegExp.$1)/100+"":b?"1":""},set:function(a,b){var c=a.style,d=a.currentStyle,e=f.isNumeric(b)?"alpha(opacity="+b*100+")":"",g=d&&d.filter||c.filter||"";c.zoom=1;if(b>=1&&f.trim(g.replace(bq,""))===""){c.removeAttribute("filter");if(d&&!d.filter)return}c.filter=bq.test(g)?g.replace(bq,e):g+" "+e}}),f(function(){f.support.reliableMarginRight||(f.cssHooks.marginRight={get:function(a,b){var c;f.swap(a,{display:"inline-block"},function(){b?c=bz(a,"margin-right","marginRight"):c=a.style.marginRight});return c}})}),c.defaultView&&c.defaultView.getComputedStyle&&(bA=function(a,b){var c,d,e;b=b.replace(bs,"-$1").toLowerCase(),(d=a.ownerDocument.defaultView)&&(e=d.getComputedStyle(a,null))&&(c=e.getPropertyValue(b),c===""&&!f.contains(a.ownerDocument.documentElement,a)&&(c=f.style(a,b)));return c}),c.documentElement.currentStyle&&(bB=function(a,b){var c,d,e,f=a.currentStyle&&a.currentStyle[b],g=a.style;f===null&&g&&(e=g[b])&&(f=e),!bt.test(f)&&bu.test(f)&&(c=g.left,d=a.runtimeStyle&&a.runtimeStyle.left,d&&(a.runtimeStyle.left=a.currentStyle.left),g.left=b==="fontSize"?"1em":f||0,f=g.pixelLeft+"px",g.left=c,d&&(a.runtimeStyle.left=d));return f===""?"auto":f}),bz=bA||bB,f.expr&&f.expr.filters&&(f.expr.filters.hidden=function(a){var b=a.offsetWidth,c=a.offsetHeight;return b===0&&c===0||!f.support.reliableHiddenOffsets&&(a.style&&a.style.display||f.css(a,"display"))==="none"},f.expr.filters.visible=function(a){return!f.expr.filters.hidden(a)});var bD=/%20/g,bE=/\[\]$/,bF=/\r?\n/g,bG=/#.*$/,bH=/^(.*?):[ \t]*([^\r\n]*)\r?$/mg,bI=/^(?:color|date|datetime|datetime-local|email|hidden|month|number|password|range|search|tel|text|time|url|week)$/i,bJ=/^(?:about|app|app\-storage|.+\-extension|file|res|widget):$/,bK=/^(?:GET|HEAD)$/,bL=/^\/\//,bM=/\?/,bN=/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi,bO=/^(?:select|textarea)/i,bP=/\s+/,bQ=/([?&])_=[^&]*/,bR=/^([\w\+\.\-]+:)(?:\/\/([^\/?#:]*)(?::(\d+))?)?/,bS=f.fn.load,bT={},bU={},bV,bW,bX=["*/"]+["*"];try{bV=e.href}catch(bY){bV=c.createElement("a"),bV.href="",bV=bV.href}bW=bR.exec(bV.toLowerCase())||[],f.fn.extend({load:function(a,c,d){if(typeof a!="string"&&bS)return bS.apply(this,arguments);if(!this.length)return this;var e=a.indexOf(" ");if(e>=0){var g=a.slice(e,a.length);a=a.slice(0,e)}var h="GET";c&&(f.isFunction(c)?(d=c,c=b):typeof c=="object"&&(c=f.param(c,f.ajaxSettings.traditional),h="POST"));var i=this;f.ajax({url:a,type:h,dataType:"html",data:c,complete:function(a,b,c){c=a.responseText,a.isResolved()&&(a.done(function(a){c=a}),i.html(g?f("<div>").append(c.replace(bN,"")).find(g):c)),d&&i.each(d,[c,b,a])}});return this},serialize:function(){return f.param(this.serializeArray())},serializeArray:function(){return this.map(function(){return this.elements?f.makeArray(this.elements):this}).filter(function(){return this.name&&!this.disabled&&(this.checked||bO.test(this.nodeName)||bI.test(this.type))}).map(function(a,b){var c=f(this).val();return c==null?null:f.isArray(c)?f.map(c,function(a,c){return{name:b.name,value:a.replace(bF,"\r\n")}}):{name:b.name,value:c.replace(bF,"\r\n")}}).get()}}),f.each("ajaxStart ajaxStop ajaxComplete ajaxError ajaxSuccess ajaxSend".split(" "),function(a,b){f.fn[b]=function(a){return this.on(b,a)}}),f.each(["get","post"],function(a,c){f[c]=function(a,d,e,g){f.isFunction(d)&&(g=g||e,e=d,d=b);return f.ajax({type:c,url:a,data:d,success:e,dataType:g})}}),f.extend({getScript:function(a,c){return f.get(a,b,c,"script")},getJSON:function(a,b,c){return f.get(a,b,c,"json")},ajaxSetup:function(a,b){b?b_(a,f.ajaxSettings):(b=a,a=f.ajaxSettings),b_(a,b);return a},ajaxSettings:{url:bV,isLocal:bJ.test(bW[1]),global:!0,type:"GET",contentType:"application/x-www-form-urlencoded",processData:!0,async:!0,accepts:{xml:"application/xml, text/xml",html:"text/html",text:"text/plain",json:"application/json, text/javascript","*":bX},contents:{xml:/xml/,html:/html/,json:/json/},responseFields:{xml:"responseXML",text:"responseText"},converters:{"* text":a.String,"text html":!0,"text json":f.parseJSON,"text xml":f.parseXML},flatOptions:{context:!0,url:!0}},ajaxPrefilter:bZ(bT),ajaxTransport:bZ(bU),ajax:function(a,c){function w(a,c,l,m){if(s!==2){s=2,q&&clearTimeout(q),p=b,n=m||"",v.readyState=a>0?4:0;var o,r,u,w=c,x=l?cb(d,v,l):b,y,z;if(a>=200&&a<300||a===304){if(d.ifModified){if(y=v.getResponseHeader("Last-Modified"))f.lastModified[k]=y;if(z=v.getResponseHeader("Etag"))f.etag[k]=z}if(a===304)w="notmodified",o=!0;else try{r=cc(d,x),w="success",o=!0}catch(A){w="parsererror",u=A}}else{u=w;if(!w||a)w="error",a<0&&(a=0)}v.status=a,v.statusText=""+(c||w),o?h.resolveWith(e,[r,w,v]):h.rejectWith(e,[v,w,u]),v.statusCode(j),j=b,t&&g.trigger("ajax"+(o?"Success":"Error"),[v,d,o?r:u]),i.fireWith(e,[v,w]),t&&(g.trigger("ajaxComplete",[v,d]),--f.active||f.event.trigger("ajaxStop"))}}typeof a=="object"&&(c=a,a=b),c=c||{};var d=f.ajaxSetup({},c),e=d.context||d,g=e!==d&&(e.nodeType||e instanceof f)?f(e):f.event,h=f.Deferred(),i=f.Callbacks("once memory"),j=d.statusCode||{},k,l={},m={},n,o,p,q,r,s=0,t,u,v={readyState:0,setRequestHeader:function(a,b){if(!s){var c=a.toLowerCase();a=m[c]=m[c]||a,l[a]=b}return this},getAllResponseHeaders:function(){return s===2?n:null},getResponseHeader:function(a){var c;if(s===2){if(!o){o={};while(c=bH.exec(n))o[c[1].toLowerCase()]=c[2]}c=o[a.toLowerCase()]}return c===b?null:c},overrideMimeType:function(a){s||(d.mimeType=a);return this},abort:function(a){a=a||"abort",p&&p.abort(a),w(0,a);return this}};h.promise(v),v.success=v.done,v.error=v.fail,v.complete=i.add,v.statusCode=function(a){if(a){var b;if(s<2)for(b in a)j[b]=[j[b],a[b]];else b=a[v.status],v.then(b,b)}return this},d.url=((a||d.url)+"").replace(bG,"").replace(bL,bW[1]+"//"),d.dataTypes=f.trim(d.dataType||"*").toLowerCase().split(bP),d.crossDomain==null&&(r=bR.exec(d.url.toLowerCase()),d.crossDomain=!(!r||r[1]==bW[1]&&r[2]==bW[2]&&(r[3]||(r[1]==="http:"?80:443))==(bW[3]||(bW[1]==="http:"?80:443)))),d.data&&d.processData&&typeof d.data!="string"&&(d.data=f.param(d.data,d.traditional)),b$(bT,d,c,v);if(s===2)return!1;t=d.global,d.type=d.type.toUpperCase(),d.hasContent=!bK.test(d.type),t&&f.active++===0&&f.event.trigger("ajaxStart");if(!d.hasContent){d.data&&(d.url+=(bM.test(d.url)?"&":"?")+d.data,delete d.data),k=d.url;if(d.cache===!1){var x=f.now(),y=d.url.replace(bQ,"$1_="+x);d.url=y+(y===d.url?(bM.test(d.url)?"&":"?")+"_="+x:"")}}(d.data&&d.hasContent&&d.contentType!==!1||c.contentType)&&v.setRequestHeader("Content-Type",d.contentType),d.ifModified&&(k=k||d.url,f.lastModified[k]&&v.setRequestHeader("If-Modified-Since",f.lastModified[k]),f.etag[k]&&v.setRequestHeader("If-None-Match",f.etag[k])),v.setRequestHeader("Accept",d.dataTypes[0]&&d.accepts[d.dataTypes[0]]?d.accepts[d.dataTypes[0]]+(d.dataTypes[0]!=="*"?", "+bX+"; q=0.01":""):d.accepts["*"]);for(u in d.headers)v.setRequestHeader(u,d.headers[u]);if(d.beforeSend&&(d.beforeSend.call(e,v,d)===!1||s===2)){v.abort();return!1}for(u in{success:1,error:1,complete:1})v[u](d[u]);p=b$(bU,d,c,v);if(!p)w(-1,"No Transport");else{v.readyState=1,t&&g.trigger("ajaxSend",[v,d]),d.async&&d.timeout>0&&(q=setTimeout(function(){v.abort("timeout")},d.timeout));try{s=1,p.send(l,w)}catch(z){if(s<2)w(-1,z);else throw z}}return v},param:function(a,c){var d=[],e=function(a,b){b=f.isFunction(b)?b():b,d[d.length]=encodeURIComponent(a)+"="+encodeURIComponent(b)};c===b&&(c=f.ajaxSettings.traditional);if(f.isArray(a)||a.jquery&&!f.isPlainObject(a))f.each(a,function(){e(this.name,this.value)});else for(var g in a)ca(g,a[g],c,e);return d.join("&").replace(bD,"+")}}),f.extend({active:0,lastModified:{},etag:{}});var cd=f.now(),ce=/(\=)\?(&|$)|\?\?/i;f.ajaxSetup({jsonp:"callback",jsonpCallback:function(){return f.expando+"_"+cd++}}),f.ajaxPrefilter("json jsonp",function(b,c,d){var e=b.contentType==="application/x-www-form-urlencoded"&&typeof b.data=="string";if(b.dataTypes[0]==="jsonp"||b.jsonp!==!1&&(ce.test(b.url)||e&&ce.test(b.data))){var g,h=b.jsonpCallback=f.isFunction(b.jsonpCallback)?b.jsonpCallback():b.jsonpCallback,i=a[h],j=b.url,k=b.data,l="$1"+h+"$2";b.jsonp!==!1&&(j=j.replace(ce,l),b.url===j&&(e&&(k=k.replace(ce,l)),b.data===k&&(j+=(/\?/.test(j)?"&":"?")+b.jsonp+"="+h))),b.url=j,b.data=k,a[h]=function(a){g=[a]},d.always(function(){a[h]=i,g&&f.isFunction(i)&&a[h](g[0])}),b.converters["script json"]=function(){g||f.error(h+" was not called");return g[0]},b.dataTypes[0]="json";return"script"}}),f.ajaxSetup({accepts:{script:"text/javascript, application/javascript, application/ecmascript, application/x-ecmascript"},contents:{script:/javascript|ecmascript/},converters:{"text script":function(a){f.globalEval(a);return a}}}),f.ajaxPrefilter("script",function(a){a.cache===b&&(a.cache=!1),a.crossDomain&&(a.type="GET",a.global=!1)}),f.ajaxTransport("script",function(a){if(a.crossDomain){var d,e=c.head||c.getElementsByTagName("head")[0]||c.documentElement;return{send:function(f,g){d=c.createElement("script"),d.async="async",a.scriptCharset&&(d.charset=a.scriptCharset),d.src=a.url,d.onload=d.onreadystatechange=function(a,c){if(c||!d.readyState||/loaded|complete/.test(d.readyState))d.onload=d.onreadystatechange=null,e&&d.parentNode&&e.removeChild(d),d=b,c||g(200,"success")},e.insertBefore(d,e.firstChild)},abort:function(){d&&d.onload(0,1)}}}});var cf=a.ActiveXObject?function(){for(var a in ch)ch[a](0,1)}:!1,cg=0,ch;f.ajaxSettings.xhr=a.ActiveXObject?function(){return!this.isLocal&&ci()||cj()}:ci,function(a){f.extend(f.support,{ajax:!!a,cors:!!a&&"withCredentials"in a})}(f.ajaxSettings.xhr()),f.support.ajax&&f.ajaxTransport(function(c){if(!c.crossDomain||f.support.cors){var d;return{send:function(e,g){var h=c.xhr(),i,j;c.username?h.open(c.type,c.url,c.async,c.username,c.password):h.open(c.type,c.url,c.async);if(c.xhrFields)for(j in c.xhrFields)h[j]=c.xhrFields[j];c.mimeType&&h.overrideMimeType&&h.overrideMimeType(c.mimeType),!c.crossDomain&&!e["X-Requested-With"]&&(e["X-Requested-With"]="XMLHttpRequest");try{for(j in e)h.setRequestHeader(j,e[j])}catch(k){}h.send(c.hasContent&&c.data||null),d=function(a,e){var j,k,l,m,n;try{if(d&&(e||h.readyState===4)){d=b,i&&(h.onreadystatechange=f.noop,cf&&delete ch[i]);if(e)h.readyState!==4&&h.abort();else{j=h.status,l=h.getAllResponseHeaders(),m={},n=h.responseXML,n&&n.documentElement&&(m.xml=n),m.text=h.responseText;try{k=h.statusText}catch(o){k=""}!j&&c.isLocal&&!c.crossDomain?j=m.text?200:404:j===1223&&(j=204)}}}catch(p){e||g(-1,p)}m&&g(j,k,m,l)},!c.async||h.readyState===4?d():(i=++cg,cf&&(ch||(ch={},f(a).unload(cf)),ch[i]=d),h.onreadystatechange=d)},abort:function(){d&&d(0,1)}}}});var ck={},cl,cm,cn=/^(?:toggle|show|hide)$/,co=/^([+\-]=)?([\d+.\-]+)([a-z%]*)$/i,cp,cq=[["height","marginTop","marginBottom","paddingTop","paddingBottom"],["width","marginLeft","marginRight","paddingLeft","paddingRight"],["opacity"]],cr;f.fn.extend({show:function(a,b,c){var d,e;if(a||a===0)return this.animate(cu("show",3),a,b,c);for(var g=0,h=this.length;g<h;g++)d=this[g],d.style&&(e=d.style.display,!f._data(d,"olddisplay")&&e==="none"&&(e=d.style.display=""),e===""&&f.css(d,"display")==="none"&&f._data(d,"olddisplay",cv(d.nodeName)));for(g=0;g<h;g++){d=this[g];if(d.style){e=d.style.display;if(e===""||e==="none")d.style.display=f._data(d,"olddisplay")||""}}return this},hide:function(a,b,c){if(a||a===0)return this.animate(cu("hide",3),a,b,c);var d,e,g=0,h=this.length;for(;g<h;g++)d=this[g],d.style&&(e=f.css(d,"display"),e!=="none"&&!f._data(d,"olddisplay")&&f._data(d,"olddisplay",e));for(g=0;g<h;g++)this[g].style&&(this[g].style.display="none");return this},_toggle:f.fn.toggle,toggle:function(a,b,c){var d=typeof a=="boolean";f.isFunction(a)&&f.isFunction(b)?this._toggle.apply(this,arguments):a==null||d?this.each(function(){var b=d?a:f(this).is(":hidden");f(this)[b?"show":"hide"]()}):this.animate(cu("toggle",3),a,b,c);return this},fadeTo:function(a,b,c,d){return this.filter(":hidden").css("opacity",0).show().end().animate({opacity:b},a,c,d)},animate:function(a,b,c,d){function g(){e.queue===!1&&f._mark(this);var b=f.extend({},e),c=this.nodeType===1,d=c&&f(this).is(":hidden"),g,h,i,j,k,l,m,n,o;b.animatedProperties={};for(i in a){g=f.camelCase(i),i!==g&&(a[g]=a[i],delete a[i]),h=a[g],f.isArray(h)?(b.animatedProperties[g]=h[1],h=a[g]=h[0]):b.animatedProperties[g]=b.specialEasing&&b.specialEasing[g]||b.easing||"swing";if(h==="hide"&&d||h==="show"&&!d)return b.complete.call(this);c&&(g==="height"||g==="width")&&(b.overflow=[this.style.overflow,this.style.overflowX,this.style.overflowY],f.css(this,"display")==="inline"&&f.css(this,"float")==="none"&&(!f.support.inlineBlockNeedsLayout||cv(this.nodeName)==="inline"?this.style.display="inline-block":this.style.zoom=1))}b.overflow!=null&&(this.style.overflow="hidden");for(i in a)j=new f.fx(this,b,i),h=a[i],cn.test(h)?(o=f._data(this,"toggle"+i)||(h==="toggle"?d?"show":"hide":0),o?(f._data(this,"toggle"+i,o==="show"?"hide":"show"),j[o]()):j[h]()):(k=co.exec(h),l=j.cur(),k?(m=parseFloat(k[2]),n=k[3]||(f.cssNumber[i]?"":"px"),n!=="px"&&(f.style(this,i,(m||1)+n),l=(m||1)/j.cur()*l,f.style(this,i,l+n)),k[1]&&(m=(k[1]==="-="?-1:1)*m+l),j.custom(l,m,n)):j.custom(l,h,""));return!0}var e=f.speed(b,c,d);if(f.isEmptyObject(a))return this.each(e.complete,[!1]);a=f.extend({},a);return e.queue===!1?this.each(g):this.queue(e.queue,g)},stop:function(a,c,d){typeof a!="string"&&(d=c,c=a,a=b),c&&a!==!1&&this.queue(a||"fx",[]);return this.each(function(){function h(a,b,c){var e=b[c];f.removeData(a,c,!0),e.stop(d)}var b,c=!1,e=f.timers,g=f._data(this);d||f._unmark(!0,this);if(a==null)for(b in g)g[b]&&g[b].stop&&b.indexOf(".run")===b.length-4&&h(this,g,b);else g[b=a+".run"]&&g[b].stop&&h(this,g,b);for(b=e.length;b--;)e[b].elem===this&&(a==null||e[b].queue===a)&&(d?e[b](!0):e[b].saveState(),c=!0,e.splice(b,1));(!d||!c)&&f.dequeue(this,a)})}}),f.each({slideDown:cu("show",1),slideUp:cu("hide",1),slideToggle:cu("toggle",1),fadeIn:{opacity:"show"},fadeOut:{opacity:"hide"},fadeToggle:{opacity:"toggle"}},function(a,b){f.fn[a]=function(a,c,d){return this.animate(b,a,c,d)}}),f.extend({speed:function(a,b,c){var d=a&&typeof a=="object"?f.extend({},a):{complete:c||!c&&b||f.isFunction(a)&&a,duration:a,easing:c&&b||b&&!f.isFunction(b)&&b};d.duration=f.fx.off?0:typeof d.duration=="number"?d.duration:d.duration in f.fx.speeds?f.fx.speeds[d.duration]:f.fx.speeds._default;if(d.queue==null||d.queue===!0)d.queue="fx";d.old=d.complete,d.complete=function(a){f.isFunction(d.old)&&d.old.call(this),d.queue?f.dequeue(this,d.queue):a!==!1&&f._unmark(this)};return d},easing:{linear:function(a,b,c,d){return c+d*a},swing:function(a,b,c,d){return(-Math.cos(a*Math.PI)/2+.5)*d+c}},timers:[],fx:function(a,b,c){this.options=b,this.elem=a,this.prop=c,b.orig=b.orig||{}}}),f.fx.prototype={update:function(){this.options.step&&this.options.step.call(this.elem,this.now,this),(f.fx.step[this.prop]||f.fx.step._default)(this)},cur:function(){if(this.elem[this.prop]!=null&&(!this.elem.style||this.elem.style[this.prop]==null))return this.elem[this.prop];var a,b=f.css(this.elem,this.prop);return isNaN(a=parseFloat(b))?!b||b==="auto"?0:b:a},custom:function(a,c,d){function h(a){return e.step(a)}var e=this,g=f.fx;this.startTime=cr||cs(),this.end=c,this.now=this.start=a,this.pos=this.state=0,this.unit=d||this.unit||(f.cssNumber[this.prop]?"":"px"),h.queue=this.options.queue,h.elem=this.elem,h.saveState=function(){e.options.hide&&f._data(e.elem,"fxshow"+e.prop)===b&&f._data(e.elem,"fxshow"+e.prop,e.start)},h()&&f.timers.push(h)&&!cp&&(cp=setInterval(g.tick,g.interval))},show:function(){var a=f._data(this.elem,"fxshow"+this.prop);this.options.orig[this.prop]=a||f.style(this.elem,this.prop),this.options.show=!0,a!==b?this.custom(this.cur(),a):this.custom(this.prop==="width"||this.prop==="height"?1:0,this.cur()),f(this.elem).show()},hide:function(){this.options.orig[this.prop]=f._data(this.elem,"fxshow"+this.prop)||f.style(this.elem,this.prop),this.options.hide=!0,this.custom(this.cur(),0)},step:function(a){var b,c,d,e=cr||cs(),g=!0,h=this.elem,i=this.options;if(a||e>=i.duration+this.startTime){this.now=this.end,this.pos=this.state=1,this.update(),i.animatedProperties[this.prop]=!0;for(b in i.animatedProperties)i.animatedProperties[b]!==!0&&(g=!1);if(g){i.overflow!=null&&!f.support.shrinkWrapBlocks&&f.each(["","X","Y"],function(a,b){h.style["overflow"+b]=i.overflow[a]}),i.hide&&f(h).hide();if(i.hide||i.show)for(b in i.animatedProperties)f.style(h,b,i.orig[b]),f.removeData(h,"fxshow"+b,!0),f.removeData(h,"toggle"+b,!0);d=i.complete,d&&(i.complete=!1,d.call(h))}return!1}i.duration==Infinity?this.now=e:(c=e-this.startTime,this.state=c/i.duration,this.pos=f.easing[i.animatedProperties[this.prop]](this.state,c,0,1,i.duration),this.now=this.start+(this.end-this.start)*this.pos),this.update();return!0}},f.extend(f.fx,{tick:function(){var a,b=f.timers,c=0;for(;c<b.length;c++)a=b[c],!a()&&b[c]===a&&b.splice(c--,1);b.length||f.fx.stop()},interval:13,stop:function(){clearInterval(cp),cp=null},speeds:{slow:600,fast:200,_default:400},step:{opacity:function(a){f.style(a.elem,"opacity",a.now)},_default:function(a){a.elem.style&&a.elem.style[a.prop]!=null?a.elem.style[a.prop]=a.now+a.unit:a.elem[a.prop]=a.now}}}),f.each(["width","height"],function(a,b){f.fx.step[b]=function(a){f.style(a.elem,b,Math.max(0,a.now)+a.unit)}}),f.expr&&f.expr.filters&&(f.expr.filters.animated=function(a){return f.grep(f.timers,function(b){return a===b.elem}).length});var cw=/^t(?:able|d|h)$/i,cx=/^(?:body|html)$/i;"getBoundingClientRect"in c.documentElement?f.fn.offset=function(a){var b=this[0],c;if(a)return this.each(function(b){f.offset.setOffset(this,a,b)});if(!b||!b.ownerDocument)return null;if(b===b.ownerDocument.body)return f.offset.bodyOffset(b);try{c=b.getBoundingClientRect()}catch(d){}var e=b.ownerDocument,g=e.documentElement;if(!c||!f.contains(g,b))return c?{top:c.top,left:c.left}:{top:0,left:0};var h=e.body,i=cy(e),j=g.clientTop||h.clientTop||0,k=g.clientLeft||h.clientLeft||0,l=i.pageYOffset||f.support.boxModel&&g.scrollTop||h.scrollTop,m=i.pageXOffset||f.support.boxModel&&g.scrollLeft||h.scrollLeft,n=c.top+l-j,o=c.left+m-k;return{top:n,left:o}}:f.fn.offset=function(a){var b=this[0];if(a)return this.each(function(b){f.offset.setOffset(this,a,b)});if(!b||!b.ownerDocument)return null;if(b===b.ownerDocument.body)return f.offset.bodyOffset(b);var c,d=b.offsetParent,e=b,g=b.ownerDocument,h=g.documentElement,i=g.body,j=g.defaultView,k=j?j.getComputedStyle(b,null):b.currentStyle,l=b.offsetTop,m=b.offsetLeft;while((b=b.parentNode)&&b!==i&&b!==h){if(f.support.fixedPosition&&k.position==="fixed")break;c=j?j.getComputedStyle(b,null):b.currentStyle,l-=b.scrollTop,m-=b.scrollLeft,b===d&&(l+=b.offsetTop,m+=b.offsetLeft,f.support.doesNotAddBorder&&(!f.support.doesAddBorderForTableAndCells||!cw.test(b.nodeName))&&(l+=parseFloat(c.borderTopWidth)||0,m+=parseFloat(c.borderLeftWidth)||0),e=d,d=b.offsetParent),f.support.subtractsBorderForOverflowNotVisible&&c.overflow!=="visible"&&(l+=parseFloat(c.borderTopWidth)||0,m+=parseFloat(c.borderLeftWidth)||0),k=c}if(k.position==="relative"||k.position==="static")l+=i.offsetTop,m+=i.offsetLeft;f.support.fixedPosition&&k.position==="fixed"&&(l+=Math.max(h.scrollTop,i.scrollTop),m+=Math.max(h.scrollLeft,i.scrollLeft));return{top:l,left:m}},f.offset={bodyOffset:function(a){var b=a.offsetTop,c=a.offsetLeft;f.support.doesNotIncludeMarginInBodyOffset&&(b+=parseFloat(f.css(a,"marginTop"))||0,c+=parseFloat(f.css(a,"marginLeft"))||0);return{top:b,left:c}},setOffset:function(a,b,c){var d=f.css(a,"position");d==="static"&&(a.style.position="relative");var e=f(a),g=e.offset(),h=f.css(a,"top"),i=f.css(a,"left"),j=(d==="absolute"||d==="fixed")&&f.inArray("auto",[h,i])>-1,k={},l={},m,n;j?(l=e.position(),m=l.top,n=l.left):(m=parseFloat(h)||0,n=parseFloat(i)||0),f.isFunction(b)&&(b=b.call(a,c,g)),b.top!=null&&(k.top=b.top-g.top+m),b.left!=null&&(k.left=b.left-g.left+n),"using"in b?b.using.call(a,k):e.css(k)}},f.fn.extend({position:function(){if(!this[0])return null;var a=this[0],b=this.offsetParent(),c=this.offset(),d=cx.test(b[0].nodeName)?{top:0,left:0}:b.offset();c.top-=parseFloat(f.css(a,"marginTop"))||0,c.left-=parseFloat(f.css(a,"marginLeft"))||0,d.top+=parseFloat(f.css(b[0],"borderTopWidth"))||0,d.left+=parseFloat(f.css(b[0],"borderLeftWidth"))||0;return{top:c.top-d.top,left:c.left-d.left}},offsetParent:function(){return this.map(function(){var a=this.offsetParent||c.body;while(a&&!cx.test(a.nodeName)&&f.css(a,"position")==="static")a=a.offsetParent;return a})}}),f.each(["Left","Top"],function(a,c){var d="scroll"+c;f.fn[d]=function(c){var e,g;if(c===b){e=this[0];if(!e)return null;g=cy(e);return g?"pageXOffset"in g?g[a?"pageYOffset":"pageXOffset"]:f.support.boxModel&&g.document.documentElement[d]||g.document.body[d]:e[d]}return this.each(function(){g=cy(this),g?g.scrollTo(a?f(g).scrollLeft():c,a?c:f(g).scrollTop()):this[d]=c})}}),f.each(["Height","Width"],function(a,c){var d=c.toLowerCase();f.fn["inner"+c]=function(){var a=this[0];return a?a.style?parseFloat(f.css(a,d,"padding")):this[d]():null},f.fn["outer"+c]=function(a){var b=this[0];return b?b.style?parseFloat(f.css(b,d,a?"margin":"border")):this[d]():null},f.fn[d]=function(a){var e=this[0];if(!e)return a==null?null:this;if(f.isFunction(a))return this.each(function(b){var c=f(this);c[d](a.call(this,b,c[d]()))});if(f.isWindow(e)){var g=e.document.documentElement["client"+c],h=e.document.body;return e.document.compatMode==="CSS1Compat"&&g||h&&h["client"+c]||g}if(e.nodeType===9)return Math.max(e.documentElement["client"+c],e.body["scroll"+c],e.documentElement["scroll"+c],e.body["offset"+c],e.documentElement["offset"+c]);if(a===b){var i=f.css(e,d),j=parseFloat(i);return f.isNumeric(j)?j:i}return this.css(d,typeof a=="string"?a:a+"px")}}),a.jQuery=a.$=f,typeof define=="function"&&define.amd&&define.amd.jQuery&&define("jquery",[],function(){return f})})(window);// Underscore.js 1.3.1
// (c) 2009-2012 Jeremy Ashkenas, DocumentCloud Inc.
// Underscore is freely distributable under the MIT license.
// Portions of Underscore are inspired or borrowed from Prototype,
// Oliver Steele's Functional, and John Resig's Micro-Templating.
// For all details and documentation:
// http://documentcloud.github.com/underscore
(function(){function q(a,c,d){if(a===c)return a!==0||1/a==1/c;if(a==null||c==null)return a===c;if(a._chain)a=a._wrapped;if(c._chain)c=c._wrapped;if(a.isEqual&&b.isFunction(a.isEqual))return a.isEqual(c);if(c.isEqual&&b.isFunction(c.isEqual))return c.isEqual(a);var e=l.call(a);if(e!=l.call(c))return false;switch(e){case "[object String]":return a==String(c);case "[object Number]":return a!=+a?c!=+c:a==0?1/a==1/c:a==+c;case "[object Date]":case "[object Boolean]":return+a==+c;case "[object RegExp]":return a.source==
c.source&&a.global==c.global&&a.multiline==c.multiline&&a.ignoreCase==c.ignoreCase}if(typeof a!="object"||typeof c!="object")return false;for(var f=d.length;f--;)if(d[f]==a)return true;d.push(a);var f=0,g=true;if(e=="[object Array]"){if(f=a.length,g=f==c.length)for(;f--;)if(!(g=f in a==f in c&&q(a[f],c[f],d)))break}else{if("constructor"in a!="constructor"in c||a.constructor!=c.constructor)return false;for(var h in a)if(b.has(a,h)&&(f++,!(g=b.has(c,h)&&q(a[h],c[h],d))))break;if(g){for(h in c)if(b.has(c,
h)&&!f--)break;g=!f}}d.pop();return g}var r=this,G=r._,n={},k=Array.prototype,o=Object.prototype,i=k.slice,H=k.unshift,l=o.toString,I=o.hasOwnProperty,w=k.forEach,x=k.map,y=k.reduce,z=k.reduceRight,A=k.filter,B=k.every,C=k.some,p=k.indexOf,D=k.lastIndexOf,o=Array.isArray,J=Object.keys,s=Function.prototype.bind,b=function(a){return new m(a)};if(typeof exports!=="undefined"){if(typeof module!=="undefined"&&module.exports)exports=module.exports=b;exports._=b}else r._=b;b.VERSION="1.3.1";var j=b.each=
b.forEach=function(a,c,d){if(a!=null)if(w&&a.forEach===w)a.forEach(c,d);else if(a.length===+a.length)for(var e=0,f=a.length;e<f;e++){if(e in a&&c.call(d,a[e],e,a)===n)break}else for(e in a)if(b.has(a,e)&&c.call(d,a[e],e,a)===n)break};b.map=b.collect=function(a,c,b){var e=[];if(a==null)return e;if(x&&a.map===x)return a.map(c,b);j(a,function(a,g,h){e[e.length]=c.call(b,a,g,h)});if(a.length===+a.length)e.length=a.length;return e};b.reduce=b.foldl=b.inject=function(a,c,d,e){var f=arguments.length>2;a==
null&&(a=[]);if(y&&a.reduce===y)return e&&(c=b.bind(c,e)),f?a.reduce(c,d):a.reduce(c);j(a,function(a,b,i){f?d=c.call(e,d,a,b,i):(d=a,f=true)});if(!f)throw new TypeError("Reduce of empty array with no initial value");return d};b.reduceRight=b.foldr=function(a,c,d,e){var f=arguments.length>2;a==null&&(a=[]);if(z&&a.reduceRight===z)return e&&(c=b.bind(c,e)),f?a.reduceRight(c,d):a.reduceRight(c);var g=b.toArray(a).reverse();e&&!f&&(c=b.bind(c,e));return f?b.reduce(g,c,d,e):b.reduce(g,c)};b.find=b.detect=
function(a,c,b){var e;E(a,function(a,g,h){if(c.call(b,a,g,h))return e=a,true});return e};b.filter=b.select=function(a,c,b){var e=[];if(a==null)return e;if(A&&a.filter===A)return a.filter(c,b);j(a,function(a,g,h){c.call(b,a,g,h)&&(e[e.length]=a)});return e};b.reject=function(a,c,b){var e=[];if(a==null)return e;j(a,function(a,g,h){c.call(b,a,g,h)||(e[e.length]=a)});return e};b.every=b.all=function(a,c,b){var e=true;if(a==null)return e;if(B&&a.every===B)return a.every(c,b);j(a,function(a,g,h){if(!(e=
e&&c.call(b,a,g,h)))return n});return e};var E=b.some=b.any=function(a,c,d){c||(c=b.identity);var e=false;if(a==null)return e;if(C&&a.some===C)return a.some(c,d);j(a,function(a,b,h){if(e||(e=c.call(d,a,b,h)))return n});return!!e};b.include=b.contains=function(a,c){var b=false;if(a==null)return b;return p&&a.indexOf===p?a.indexOf(c)!=-1:b=E(a,function(a){return a===c})};b.invoke=function(a,c){var d=i.call(arguments,2);return b.map(a,function(a){return(b.isFunction(c)?c||a:a[c]).apply(a,d)})};b.pluck=
function(a,c){return b.map(a,function(a){return a[c]})};b.max=function(a,c,d){if(!c&&b.isArray(a))return Math.max.apply(Math,a);if(!c&&b.isEmpty(a))return-Infinity;var e={computed:-Infinity};j(a,function(a,b,h){b=c?c.call(d,a,b,h):a;b>=e.computed&&(e={value:a,computed:b})});return e.value};b.min=function(a,c,d){if(!c&&b.isArray(a))return Math.min.apply(Math,a);if(!c&&b.isEmpty(a))return Infinity;var e={computed:Infinity};j(a,function(a,b,h){b=c?c.call(d,a,b,h):a;b<e.computed&&(e={value:a,computed:b})});
return e.value};b.shuffle=function(a){var b=[],d;j(a,function(a,f){f==0?b[0]=a:(d=Math.floor(Math.random()*(f+1)),b[f]=b[d],b[d]=a)});return b};b.sortBy=function(a,c,d){return b.pluck(b.map(a,function(a,b,g){return{value:a,criteria:c.call(d,a,b,g)}}).sort(function(a,b){var c=a.criteria,d=b.criteria;return c<d?-1:c>d?1:0}),"value")};b.groupBy=function(a,c){var d={},e=b.isFunction(c)?c:function(a){return a[c]};j(a,function(a,b){var c=e(a,b);(d[c]||(d[c]=[])).push(a)});return d};b.sortedIndex=function(a,
c,d){d||(d=b.identity);for(var e=0,f=a.length;e<f;){var g=e+f>>1;d(a[g])<d(c)?e=g+1:f=g}return e};b.toArray=function(a){return!a?[]:a.toArray?a.toArray():b.isArray(a)?i.call(a):b.isArguments(a)?i.call(a):b.values(a)};b.size=function(a){return b.toArray(a).length};b.first=b.head=function(a,b,d){return b!=null&&!d?i.call(a,0,b):a[0]};b.initial=function(a,b,d){return i.call(a,0,a.length-(b==null||d?1:b))};b.last=function(a,b,d){return b!=null&&!d?i.call(a,Math.max(a.length-b,0)):a[a.length-1]};b.rest=
b.tail=function(a,b,d){return i.call(a,b==null||d?1:b)};b.compact=function(a){return b.filter(a,function(a){return!!a})};b.flatten=function(a,c){return b.reduce(a,function(a,e){if(b.isArray(e))return a.concat(c?e:b.flatten(e));a[a.length]=e;return a},[])};b.without=function(a){return b.difference(a,i.call(arguments,1))};b.uniq=b.unique=function(a,c,d){var d=d?b.map(a,d):a,e=[];b.reduce(d,function(d,g,h){if(0==h||(c===true?b.last(d)!=g:!b.include(d,g)))d[d.length]=g,e[e.length]=a[h];return d},[]);
return e};b.union=function(){return b.uniq(b.flatten(arguments,true))};b.intersection=b.intersect=function(a){var c=i.call(arguments,1);return b.filter(b.uniq(a),function(a){return b.every(c,function(c){return b.indexOf(c,a)>=0})})};b.difference=function(a){var c=b.flatten(i.call(arguments,1));return b.filter(a,function(a){return!b.include(c,a)})};b.zip=function(){for(var a=i.call(arguments),c=b.max(b.pluck(a,"length")),d=Array(c),e=0;e<c;e++)d[e]=b.pluck(a,""+e);return d};b.indexOf=function(a,c,
d){if(a==null)return-1;var e;if(d)return d=b.sortedIndex(a,c),a[d]===c?d:-1;if(p&&a.indexOf===p)return a.indexOf(c);for(d=0,e=a.length;d<e;d++)if(d in a&&a[d]===c)return d;return-1};b.lastIndexOf=function(a,b){if(a==null)return-1;if(D&&a.lastIndexOf===D)return a.lastIndexOf(b);for(var d=a.length;d--;)if(d in a&&a[d]===b)return d;return-1};b.range=function(a,b,d){arguments.length<=1&&(b=a||0,a=0);for(var d=arguments[2]||1,e=Math.max(Math.ceil((b-a)/d),0),f=0,g=Array(e);f<e;)g[f++]=a,a+=d;return g};
var F=function(){};b.bind=function(a,c){var d,e;if(a.bind===s&&s)return s.apply(a,i.call(arguments,1));if(!b.isFunction(a))throw new TypeError;e=i.call(arguments,2);return d=function(){if(!(this instanceof d))return a.apply(c,e.concat(i.call(arguments)));F.prototype=a.prototype;var b=new F,g=a.apply(b,e.concat(i.call(arguments)));return Object(g)===g?g:b}};b.bindAll=function(a){var c=i.call(arguments,1);c.length==0&&(c=b.functions(a));j(c,function(c){a[c]=b.bind(a[c],a)});return a};b.memoize=function(a,
c){var d={};c||(c=b.identity);return function(){var e=c.apply(this,arguments);return b.has(d,e)?d[e]:d[e]=a.apply(this,arguments)}};b.delay=function(a,b){var d=i.call(arguments,2);return setTimeout(function(){return a.apply(a,d)},b)};b.defer=function(a){return b.delay.apply(b,[a,1].concat(i.call(arguments,1)))};b.throttle=function(a,c){var d,e,f,g,h,i=b.debounce(function(){h=g=false},c);return function(){d=this;e=arguments;var b;f||(f=setTimeout(function(){f=null;h&&a.apply(d,e);i()},c));g?h=true:
a.apply(d,e);i();g=true}};b.debounce=function(a,b){var d;return function(){var e=this,f=arguments;clearTimeout(d);d=setTimeout(function(){d=null;a.apply(e,f)},b)}};b.once=function(a){var b=false,d;return function(){if(b)return d;b=true;return d=a.apply(this,arguments)}};b.wrap=function(a,b){return function(){var d=[a].concat(i.call(arguments,0));return b.apply(this,d)}};b.compose=function(){var a=arguments;return function(){for(var b=arguments,d=a.length-1;d>=0;d--)b=[a[d].apply(this,b)];return b[0]}};
b.after=function(a,b){return a<=0?b():function(){if(--a<1)return b.apply(this,arguments)}};b.keys=J||function(a){if(a!==Object(a))throw new TypeError("Invalid object");var c=[],d;for(d in a)b.has(a,d)&&(c[c.length]=d);return c};b.values=function(a){return b.map(a,b.identity)};b.functions=b.methods=function(a){var c=[],d;for(d in a)b.isFunction(a[d])&&c.push(d);return c.sort()};b.extend=function(a){j(i.call(arguments,1),function(b){for(var d in b)a[d]=b[d]});return a};b.defaults=function(a){j(i.call(arguments,
1),function(b){for(var d in b)a[d]==null&&(a[d]=b[d])});return a};b.clone=function(a){return!b.isObject(a)?a:b.isArray(a)?a.slice():b.extend({},a)};b.tap=function(a,b){b(a);return a};b.isEqual=function(a,b){return q(a,b,[])};b.isEmpty=function(a){if(b.isArray(a)||b.isString(a))return a.length===0;for(var c in a)if(b.has(a,c))return false;return true};b.isElement=function(a){return!!(a&&a.nodeType==1)};b.isArray=o||function(a){return l.call(a)=="[object Array]"};b.isObject=function(a){return a===Object(a)};
b.isArguments=function(a){return l.call(a)=="[object Arguments]"};if(!b.isArguments(arguments))b.isArguments=function(a){return!(!a||!b.has(a,"callee"))};b.isFunction=function(a){return l.call(a)=="[object Function]"};b.isString=function(a){return l.call(a)=="[object String]"};b.isNumber=function(a){return l.call(a)=="[object Number]"};b.isNaN=function(a){return a!==a};b.isBoolean=function(a){return a===true||a===false||l.call(a)=="[object Boolean]"};b.isDate=function(a){return l.call(a)=="[object Date]"};
b.isRegExp=function(a){return l.call(a)=="[object RegExp]"};b.isNull=function(a){return a===null};b.isUndefined=function(a){return a===void 0};b.has=function(a,b){return I.call(a,b)};b.noConflict=function(){r._=G;return this};b.identity=function(a){return a};b.times=function(a,b,d){for(var e=0;e<a;e++)b.call(d,e)};b.escape=function(a){return(""+a).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;").replace(/'/g,"&#x27;").replace(/\//g,"&#x2F;")};b.mixin=function(a){j(b.functions(a),
function(c){K(c,b[c]=a[c])})};var L=0;b.uniqueId=function(a){var b=L++;return a?a+b:b};b.templateSettings={evaluate:/<%([\s\S]+?)%>/g,interpolate:/<%=([\s\S]+?)%>/g,escape:/<%-([\s\S]+?)%>/g};var t=/.^/,u=function(a){return a.replace(/\\\\/g,"\\").replace(/\\'/g,"'")};b.template=function(a,c){var d=b.templateSettings,d="var __p=[],print=function(){__p.push.apply(__p,arguments);};with(obj||{}){__p.push('"+a.replace(/\\/g,"\\\\").replace(/'/g,"\\'").replace(d.escape||t,function(a,b){return"',_.escape("+
u(b)+"),'"}).replace(d.interpolate||t,function(a,b){return"',"+u(b)+",'"}).replace(d.evaluate||t,function(a,b){return"');"+u(b).replace(/[\r\n\t]/g," ")+";__p.push('"}).replace(/\r/g,"\\r").replace(/\n/g,"\\n").replace(/\t/g,"\\t")+"');}return __p.join('');",e=new Function("obj","_",d);return c?e(c,b):function(a){return e.call(this,a,b)}};b.chain=function(a){return b(a).chain()};var m=function(a){this._wrapped=a};b.prototype=m.prototype;var v=function(a,c){return c?b(a).chain():a},K=function(a,c){m.prototype[a]=
function(){var a=i.call(arguments);H.call(a,this._wrapped);return v(c.apply(b,a),this._chain)}};b.mixin(b);j("pop,push,reverse,shift,sort,splice,unshift".split(","),function(a){var b=k[a];m.prototype[a]=function(){var d=this._wrapped;b.apply(d,arguments);var e=d.length;(a=="shift"||a=="splice")&&e===0&&delete d[0];return v(d,this._chain)}});j(["concat","join","slice"],function(a){var b=k[a];m.prototype[a]=function(){return v(b.apply(this._wrapped,arguments),this._chain)}});m.prototype.chain=function(){this._chain=
true;return this};m.prototype.value=function(){return this._wrapped}}).call(this);
// Backbone.js 0.9.0
// (c) 2010-2012 Jeremy Ashkenas, DocumentCloud Inc.
// Backbone may be freely distributed under the MIT license.
// For all details and documentation:
// http://backbonejs.org
(function(){var i=this,r=i.Backbone,s=Array.prototype.slice,t=Array.prototype.splice,g;g=typeof exports!=="undefined"?exports:i.Backbone={};g.VERSION="0.9.0";var f=i._;!f&&typeof require!=="undefined"&&(f=require("underscore"));var h=i.jQuery||i.Zepto||i.ender;g.noConflict=function(){i.Backbone=r;return this};g.emulateHTTP=false;g.emulateJSON=false;g.Events={on:function(a,b,c){for(var d,a=a.split(/\s+/),e=this._callbacks||(this._callbacks={});d=a.shift();){d=e[d]||(e[d]={});var f=d.tail||(d.tail=
d.next={});f.callback=b;f.context=c;d.tail=f.next={}}return this},off:function(a,b,c){var d,e,f;if(a){if(e=this._callbacks)for(a=a.split(/\s+/);d=a.shift();)if(f=e[d],delete e[d],b&&f)for(;(f=f.next)&&f.next;)if(!(f.callback===b&&(!c||f.context===c)))this.on(d,f.callback,f.context)}else delete this._callbacks;return this},trigger:function(a){var b,c,d,e;if(!(d=this._callbacks))return this;e=d.all;for((a=a.split(/\s+/)).push(null);b=a.shift();)e&&a.push({next:e.next,tail:e.tail,event:b}),(c=d[b])&&
a.push({next:c.next,tail:c.tail});for(e=s.call(arguments,1);c=a.pop();){b=c.tail;for(d=c.event?[c.event].concat(e):e;(c=c.next)!==b;)c.callback.apply(c.context||this,d)}return this}};g.Events.bind=g.Events.on;g.Events.unbind=g.Events.off;g.Model=function(a,b){var c;a||(a={});b&&b.parse&&(a=this.parse(a));if(c=j(this,"defaults"))a=f.extend({},c,a);if(b&&b.collection)this.collection=b.collection;this.attributes={};this._escapedAttributes={};this.cid=f.uniqueId("c");this._changed={};if(!this.set(a,{silent:true}))throw Error("Can't create an invalid model");
this._changed={};this._previousAttributes=f.clone(this.attributes);this.initialize.apply(this,arguments)};f.extend(g.Model.prototype,g.Events,{idAttribute:"id",initialize:function(){},toJSON:function(){return f.clone(this.attributes)},get:function(a){return this.attributes[a]},escape:function(a){var b;if(b=this._escapedAttributes[a])return b;b=this.attributes[a];return this._escapedAttributes[a]=f.escape(b==null?"":""+b)},has:function(a){return this.attributes[a]!=null},set:function(a,b,c){var d,
e;f.isObject(a)||a==null?(d=a,c=b):(d={},d[a]=b);c||(c={});if(!d)return this;if(d instanceof g.Model)d=d.attributes;if(c.unset)for(e in d)d[e]=void 0;if(this.validate&&!this._performValidation(d,c))return false;if(this.idAttribute in d)this.id=d[this.idAttribute];var b=this.attributes,k=this._escapedAttributes,n=this._previousAttributes||{},u=this._changing;this._changing=true;for(e in d)if(a=d[e],f.isEqual(b[e],a)||delete k[e],c.unset?delete b[e]:b[e]=a,delete this._changed[e],!f.isEqual(n[e],a)||
f.has(b,e)!=f.has(n,e))this._changed[e]=a;if(!u)!c.silent&&this.hasChanged()&&this.change(c),this._changing=false;return this},unset:function(a,b){(b||(b={})).unset=true;return this.set(a,null,b)},clear:function(a){(a||(a={})).unset=true;return this.set(f.clone(this.attributes),a)},fetch:function(a){var a=a?f.clone(a):{},b=this,c=a.success;a.success=function(d,e,f){if(!b.set(b.parse(d,f),a))return false;c&&c(b,d)};a.error=g.wrapError(a.error,b,a);return(this.sync||g.sync).call(this,"read",this,a)},
save:function(a,b,c){var d;f.isObject(a)||a==null?(d=a,c=b):(d={},d[a]=b);c=c?f.clone(c):{};if(d&&!this[c.wait?"_performValidation":"set"](d,c))return false;var e=this,k=c.success;c.success=function(a,b,g){b=e.parse(a,g);c.wait&&(b=f.extend(d||{},b));if(!e.set(b,c))return false;k?k(e,a):e.trigger("sync",e,a,c)};c.error=g.wrapError(c.error,e,c);a=this.isNew()?"create":"update";return(this.sync||g.sync).call(this,a,this,c)},destroy:function(a){var a=a?f.clone(a):{},b=this,c=a.success,d=function(){b.trigger("destroy",
b,b.collection,a)};if(this.isNew())return d();a.success=function(e){a.wait&&d();c?c(b,e):b.trigger("sync",b,e,a)};a.error=g.wrapError(a.error,b,a);var e=(this.sync||g.sync).call(this,"delete",this,a);a.wait||d();return e},url:function(){var a=j(this.collection,"url")||j(this,"urlRoot")||o();return this.isNew()?a:a+(a.charAt(a.length-1)=="/"?"":"/")+encodeURIComponent(this.id)},parse:function(a){return a},clone:function(){return new this.constructor(this.attributes)},isNew:function(){return this.id==
null},change:function(a){for(var b in this._changed)this.trigger("change:"+b,this,this._changed[b],a);this.trigger("change",this,a);this._previousAttributes=f.clone(this.attributes);this._changed={}},hasChanged:function(a){return a?f.has(this._changed,a):!f.isEmpty(this._changed)},changedAttributes:function(a){if(!a)return this.hasChanged()?f.clone(this._changed):false;var b,c=false,d=this._previousAttributes,e;for(e in a)if(!f.isEqual(d[e],b=a[e]))(c||(c={}))[e]=b;return c},previous:function(a){return!a||
!this._previousAttributes?null:this._previousAttributes[a]},previousAttributes:function(){return f.clone(this._previousAttributes)},_performValidation:function(a,b){var c=this.validate(f.extend({},this.attributes,a),b);return c?(b.error?b.error(this,c,b):this.trigger("error",this,c,b),false):true}});g.Collection=function(a,b){b||(b={});if(b.comparator)this.comparator=b.comparator;this._reset();this.initialize.apply(this,arguments);a&&this.reset(a,{silent:true,parse:b.parse})};f.extend(g.Collection.prototype,
g.Events,{model:g.Model,initialize:function(){},toJSON:function(){return this.map(function(a){return a.toJSON()})},add:function(a,b){var c,d,e,g,h,i={},j={};b||(b={});a=f.isArray(a)?a.slice():[a];for(c=0,d=a.length;c<d;c++){if(!(e=a[c]=this._prepareModel(a[c],b)))throw Error("Can't add an invalid model to a collection");if(i[g=e.cid]||this._byCid[g]||(h=e.id)!=null&&(j[h]||this._byId[h]))throw Error("Can't add the same model to a collection twice");i[g]=j[h]=e}for(c=0;c<d;c++)(e=a[c]).on("all",this._onModelEvent,
this),this._byCid[e.cid]=e,e.id!=null&&(this._byId[e.id]=e);this.length+=d;t.apply(this.models,[b.at!=null?b.at:this.models.length,0].concat(a));this.comparator&&this.sort({silent:true});if(b.silent)return this;for(c=0,d=this.models.length;c<d;c++)if(i[(e=this.models[c]).cid])b.index=c,e.trigger("add",e,this,b);return this},remove:function(a,b){var c,d,e,g;b||(b={});a=f.isArray(a)?a.slice():[a];for(c=0,d=a.length;c<d;c++)if(g=this.getByCid(a[c])||this.get(a[c])){delete this._byId[g.id];delete this._byCid[g.cid];
e=this.indexOf(g);this.models.splice(e,1);this.length--;if(!b.silent)b.index=e,g.trigger("remove",g,this,b);this._removeReference(g)}return this},get:function(a){return a==null?null:this._byId[a.id!=null?a.id:a]},getByCid:function(a){return a&&this._byCid[a.cid||a]},at:function(a){return this.models[a]},sort:function(a){a||(a={});if(!this.comparator)throw Error("Cannot sort a set without a comparator");var b=f.bind(this.comparator,this);this.comparator.length==1?this.models=this.sortBy(b):this.models.sort(b);
a.silent||this.trigger("reset",this,a);return this},pluck:function(a){return f.map(this.models,function(b){return b.get(a)})},reset:function(a,b){a||(a=[]);b||(b={});for(var c=0,d=this.models.length;c<d;c++)this._removeReference(this.models[c]);this._reset();this.add(a,{silent:true,parse:b.parse});b.silent||this.trigger("reset",this,b);return this},fetch:function(a){a=a?f.clone(a):{};if(a.parse===void 0)a.parse=true;var b=this,c=a.success;a.success=function(d,e,f){b[a.add?"add":"reset"](b.parse(d,
f),a);c&&c(b,d)};a.error=g.wrapError(a.error,b,a);return(this.sync||g.sync).call(this,"read",this,a)},create:function(a,b){var c=this,b=b?f.clone(b):{},a=this._prepareModel(a,b);if(!a)return false;b.wait||c.add(a,b);var d=b.success;b.success=function(e,f){b.wait&&c.add(e,b);d?d(e,f):e.trigger("sync",a,f,b)};a.save(null,b);return a},parse:function(a){return a},chain:function(){return f(this.models).chain()},_reset:function(){this.length=0;this.models=[];this._byId={};this._byCid={}},_prepareModel:function(a,
b){if(a instanceof g.Model){if(!a.collection)a.collection=this}else{var c;b.collection=this;a=new this.model(a,b);a.validate&&!a._performValidation(a.attributes,b)&&(a=false)}return a},_removeReference:function(a){this==a.collection&&delete a.collection;a.off("all",this._onModelEvent,this)},_onModelEvent:function(a,b,c,d){(a=="add"||a=="remove")&&c!=this||(a=="destroy"&&this.remove(b,d),b&&a==="change:"+b.idAttribute&&(delete this._byId[b.previous(b.idAttribute)],this._byId[b.id]=b),this.trigger.apply(this,
arguments))}});f.each("forEach,each,map,reduce,reduceRight,find,detect,filter,select,reject,every,all,some,any,include,contains,invoke,max,min,sortBy,sortedIndex,toArray,size,first,initial,rest,last,without,indexOf,shuffle,lastIndexOf,isEmpty,groupBy".split(","),function(a){g.Collection.prototype[a]=function(){return f[a].apply(f,[this.models].concat(f.toArray(arguments)))}});g.Router=function(a){a||(a={});if(a.routes)this.routes=a.routes;this._bindRoutes();this.initialize.apply(this,arguments)};
var v=/:\w+/g,w=/\*\w+/g,x=/[-[\]{}()+?.,\\^$|#\s]/g;f.extend(g.Router.prototype,g.Events,{initialize:function(){},route:function(a,b,c){g.history||(g.history=new g.History);f.isRegExp(a)||(a=this._routeToRegExp(a));c||(c=this[b]);g.history.route(a,f.bind(function(d){d=this._extractParameters(a,d);c&&c.apply(this,d);this.trigger.apply(this,["route:"+b].concat(d));g.history.trigger("route",this,b,d)},this));return this},navigate:function(a,b){g.history.navigate(a,b)},_bindRoutes:function(){if(this.routes){var a=
[],b;for(b in this.routes)a.unshift([b,this.routes[b]]);b=0;for(var c=a.length;b<c;b++)this.route(a[b][0],a[b][1],this[a[b][1]])}},_routeToRegExp:function(a){a=a.replace(x,"\\$&").replace(v,"([^/]+)").replace(w,"(.*?)");return RegExp("^"+a+"$")},_extractParameters:function(a,b){return a.exec(b).slice(1)}});g.History=function(){this.handlers=[];f.bindAll(this,"checkUrl")};var m=/^[#\/]/,y=/msie [\w.]+/,l=false;f.extend(g.History.prototype,g.Events,{interval:50,getFragment:function(a,b){if(a==null)if(this._hasPushState||
b){var a=window.location.pathname,c=window.location.search;c&&(a+=c)}else a=window.location.hash;a=decodeURIComponent(a.replace(m,""));a.indexOf(this.options.root)||(a=a.substr(this.options.root.length));return a},start:function(a){if(l)throw Error("Backbone.history has already been started");this.options=f.extend({},{root:"/"},this.options,a);this._wantsHashChange=this.options.hashChange!==false;this._wantsPushState=!!this.options.pushState;this._hasPushState=!(!this.options.pushState||!window.history||
!window.history.pushState);var a=this.getFragment(),b=document.documentMode;if(b=y.exec(navigator.userAgent.toLowerCase())&&(!b||b<=7))this.iframe=h('<iframe src="javascript:0" tabindex="-1" />').hide().appendTo("body")[0].contentWindow,this.navigate(a);if(this._hasPushState)h(window).bind("popstate",this.checkUrl);else if(this._wantsHashChange&&"onhashchange"in window&&!b)h(window).bind("hashchange",this.checkUrl);else if(this._wantsHashChange)this._checkUrlInterval=setInterval(this.checkUrl,this.interval);
this.fragment=a;l=true;a=window.location;b=a.pathname==this.options.root;if(this._wantsHashChange&&this._wantsPushState&&!this._hasPushState&&!b)return this.fragment=this.getFragment(null,true),window.location.replace(this.options.root+"#"+this.fragment),true;else if(this._wantsPushState&&this._hasPushState&&b&&a.hash)this.fragment=a.hash.replace(m,""),window.history.replaceState({},document.title,a.protocol+"//"+a.host+this.options.root+this.fragment);if(!this.options.silent)return this.loadUrl()},
stop:function(){h(window).unbind("popstate",this.checkUrl).unbind("hashchange",this.checkUrl);clearInterval(this._checkUrlInterval);l=false},route:function(a,b){this.handlers.unshift({route:a,callback:b})},checkUrl:function(){var a=this.getFragment();a==this.fragment&&this.iframe&&(a=this.getFragment(this.iframe.location.hash));if(a==this.fragment||a==decodeURIComponent(this.fragment))return false;this.iframe&&this.navigate(a);this.loadUrl()||this.loadUrl(window.location.hash)},loadUrl:function(a){var b=
this.fragment=this.getFragment(a);return f.any(this.handlers,function(a){if(a.route.test(b))return a.callback(b),true})},navigate:function(a,b){if(!l)return false;if(!b||b===true)b={trigger:b};var c=(a||"").replace(m,"");if(!(this.fragment==c||this.fragment==decodeURIComponent(c)))this._hasPushState?(c.indexOf(this.options.root)!=0&&(c=this.options.root+c),this.fragment=c,window.history[b.replace?"replaceState":"pushState"]({},document.title,c)):this._wantsHashChange?(this.fragment=c,this._updateHash(window.location,
c,b.replace),this.iframe&&c!=this.getFragment(this.iframe.location.hash)&&(b.replace||this.iframe.document.open().close(),this._updateHash(this.iframe.location,c,b.replace))):window.location.assign(this.options.root+a),b.trigger&&this.loadUrl(a)},_updateHash:function(a,b,c){c?a.replace(a.toString().replace(/(javascript:|#).*$/,"")+"#"+b):a.hash=b}});g.View=function(a){this.cid=f.uniqueId("view");this._configure(a||{});this._ensureElement();this.initialize.apply(this,arguments);this.delegateEvents()};
var z=/^(\S+)\s*(.*)$/,p="model,collection,el,id,attributes,className,tagName".split(",");f.extend(g.View.prototype,g.Events,{tagName:"div",$:function(a){return this.$el.find(a)},initialize:function(){},render:function(){return this},remove:function(){this.$el.remove();return this},make:function(a,b,c){a=document.createElement(a);b&&h(a).attr(b);c&&h(a).html(c);return a},setElement:function(a,b){this.$el=h(a);this.el=this.$el[0];b!==false&&this.delegateEvents()},delegateEvents:function(a){if(a||(a=
j(this,"events"))){this.undelegateEvents();for(var b in a){var c=a[b];f.isFunction(c)||(c=this[a[b]]);if(!c)throw Error('Event "'+a[b]+'" does not exist');var d=b.match(z),e=d[1],d=d[2],c=f.bind(c,this);e+=".delegateEvents"+this.cid;d===""?this.$el.bind(e,c):this.$el.delegate(d,e,c)}}},undelegateEvents:function(){this.$el.unbind(".delegateEvents"+this.cid)},_configure:function(a){this.options&&(a=f.extend({},this.options,a));for(var b=0,c=p.length;b<c;b++){var d=p[b];a[d]&&(this[d]=a[d])}this.options=
a},_ensureElement:function(){if(this.el)this.setElement(this.el,false);else{var a=j(this,"attributes")||{};if(this.id)a.id=this.id;if(this.className)a["class"]=this.className;this.setElement(this.make(this.tagName,a),false)}}});g.Model.extend=g.Collection.extend=g.Router.extend=g.View.extend=function(a,b){var c=A(this,a,b);c.extend=this.extend;return c};var B={create:"POST",update:"PUT","delete":"DELETE",read:"GET"};g.sync=function(a,b,c){var d=B[a],e={type:d,dataType:"json"};if(!c.url)e.url=j(b,
"url")||o();if(!c.data&&b&&(a=="create"||a=="update"))e.contentType="application/json",e.data=JSON.stringify(b.toJSON());if(g.emulateJSON)e.contentType="application/x-www-form-urlencoded",e.data=e.data?{model:e.data}:{};if(g.emulateHTTP&&(d==="PUT"||d==="DELETE")){if(g.emulateJSON)e.data._method=d;e.type="POST";e.beforeSend=function(a){a.setRequestHeader("X-HTTP-Method-Override",d)}}if(e.type!=="GET"&&!g.emulateJSON)e.processData=false;return h.ajax(f.extend(e,c))};g.wrapError=function(a,b,c){return function(d,
e){e=d===b?e:d;a?a(d,e,c):b.trigger("error",d,e,c)}};var q=function(){},A=function(a,b,c){var d;d=b&&b.hasOwnProperty("constructor")?b.constructor:function(){a.apply(this,arguments)};f.extend(d,a);q.prototype=a.prototype;d.prototype=new q;b&&f.extend(d.prototype,b);c&&f.extend(d,c);d.prototype.constructor=d;d.__super__=a.prototype;return d},j=function(a,b){return!a||!a[b]?null:f.isFunction(a[b])?a[b]():a[b]},o=function(){throw Error('A "url" property or function must be specified');}}).call(this);
//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/
var root = this,
    jQuery = root.jQuery,
    Backbone = root.Backbone,
    _ = root._;

// ## VIE constructor
//
// The VIE constructor is the way to initialize VIE for your
// application. The instance of VIE handles all management of
// semantic interaction, including keeping track of entities,
// changes to them, the possible RDFa views on the page where
// the entities are displayed, and connections to external
// services like Stanbol and DBPedia.
//
// To get a VIE instance, simply run:
//
//     var vie = new VIE();
//
// You can also pass configurations to the VIE instance through
// the constructor. For example, to set a different default
// namespace to be used for names that don't have a namespace
// specified, do:
//
//     var vie = new VIE({
//         baseNamespace: 'http://example.net'
//     });
//
// ### Differences with VIE 1.x
//
// VIE 1.x used singletons for managing entities and views loaded
// from a page. This has been changed with VIE 2.x, and now all
// data managed by VIE is tied to the instance of VIE being used.
//
// This means that VIE needs to be instantiated before using. So,
// when previously you could get entities from page with:
//
//     VIE.RDFaEntities.getInstances();
//
// Now you need to instantiate VIE first. This example uses the
// Classic API compatibility layer instead of the `load` method:
//
//     var vie = new VIE();
//     vie.RDFaEntities.getInstances();
//
// Currently the Classic API is enabled by default, but it is
// recommended to ensure it is enabled before using it. So:
//
//     var vie = new VIE({classic: true});
//     vie.RDFaEntities.getInstances();
var VIE = root.VIE = function(config) {
    this.config = (config) ? config : {};
    this.services = {};
    this.jQuery = jQuery;
    this.entities = new this.Collection();

    this.Entity.prototype.entities = this.entities;
    this.entities.vie = this;
    this.Entity.prototype.entityCollection = this.Collection;
    this.Entity.prototype.vie = this;
    
    this.Namespaces.prototype.vie = this;
// ### Namespaces in VIE
// VIE supports different ontologies and an easy use of them.
// Namespace prefixes reduce the amount of code you have to
// write. In VIE, it does not matter if you access an entitie's
// property with 
// `entity.get('<http://dbpedia.org/property/capitalOf>')` or 
// `entity.get('dbprop:capitalOf')` or even 
// `entity.get('capitalOf')` once the corresponding namespace
// is registered as *baseNamespace*.
// By default `"http://viejs.org/ns/"`is set as base namespace.
// For more information about how to set, get and list all
// registered namespaces, refer to the 
// <a href="Namespace.html">Namespaces documentation</a>.
    this.namespaces = new this.Namespaces(
        (this.config.baseNamespace) ? this.config.baseNamespace : "http://viejs.org/ns/",
        
// By default, VIE is shipped with common namespace prefixes:

// +    owl    : "http://www.w3.org/2002/07/owl#"
// +    rdfs   : "http://www.w3.org/2000/01/rdf-schema#"
// +    rdf    : "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
// +    schema : 'http://schema.org/'
// +    foaf   : 'http://xmlns.com/foaf/0.1/'
// +    geo    : 'http://www.w3.org/2003/01/geo/wgs84_pos#'
// +    dbpedia: "http://dbpedia.org/ontology/"
// +    dbprop : "http://dbpedia.org/property/"
// +    skos   : "http://www.w3.org/2004/02/skos/core#"
// +    xsd    : "http://www.w3.org/2001/XMLSchema#"
// +    sioc   : "http://rdfs.org/sioc/ns#"
// +    dcterms: "http://purl.org/dc/terms/"
        {
            owl    : "http://www.w3.org/2002/07/owl#",
            rdfs   : "http://www.w3.org/2000/01/rdf-schema#",
            rdf    : "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            schema : 'http://schema.org/',
            foaf   : 'http://xmlns.com/foaf/0.1/',
            geo    : 'http://www.w3.org/2003/01/geo/wgs84_pos#',
            dbpedia: "http://dbpedia.org/ontology/",
            dbprop : "http://dbpedia.org/property/",
            skos   : "http://www.w3.org/2004/02/skos/core#",
            xsd    : "http://www.w3.org/2001/XMLSchema#",
            sioc   : "http://rdfs.org/sioc/ns#",
            dcterms: "http://purl.org/dc/terms/"
        }
    );


    this.Type.prototype.vie = this;
    this.Types.prototype.vie = this;
    this.Attribute.prototype.vie = this;
    this.Attributes.prototype.vie = this;
// ### Type hierarchy in VIE
// VIE takes care about type hierarchy of entities
// (aka. *schema* or *ontology*).
// Once a type hierarchy is known to VIE, we can leverage
// this information, to easily ask, whether an entity
// is of type, e.g., *foaf:Person* or *schema:Place*.
// For more information about how to generate such a type
// hierarchy, refer to the 
// <a href="Type.html">Types documentation</a>.
    this.types = new this.Types();
// By default, there is a parent type in VIE, called
// *owl:Thing*. All types automatically inherit from this
// type and all registered entities, are of this type.
    this.types.add("owl:Thing");

// As described above, the Classic API of VIE 1.x is loaded
// by default. As this might change in the future, it is
// recommended to ensure it is enabled before using it. So:
//
//     var vie = new VIE({classic: true});
//     vie.RDFaEntities.getInstances();
    if (this.config.classic !== false) {
        /* Load Classic API as well */
        this.RDFa = new this.ClassicRDFa(this);
        this.RDFaEntities = new this.ClassicRDFaEntities(this);
        this.EntityManager = new this.ClassicEntityManager(this);

        this.cleanup = function() {
            this.entities.reset();
        };
    }
};

// ### use(service, name)
// This method registers services within VIE.  
// **Parameters**:  
// *{string|object}* **service** The service to be registered.  
// *{string}* **name** An optional name to register the service with. If this
// is not set, the default name that comes with the service is taken.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE}* : The current VIE instance.  
// **Example usage**:  
//
//     var vie = new VIE();
//     var conf1 = {...};
//     var conf2 = {...};
//     vie.use(new vie.StanbolService());
//     vie.use(new vie.StanbolService(conf1), "stanbol_1");
//     vie.use(new vie.StanbolService(conf2), "stanbol_2");
//     // <-- this means that there are now 3 services registered!
VIE.prototype.use = function(service, name) {
  if (!name && !service.name) {
    throw new Error("Please provide a name for the service!");
  }
  service.vie = this;
  service.name = (name)? name : service.name;
  if (service.init) {
      service.init();
  }
  this.services[service.name] = service;
  
  return this;
};

// ### service(name)
// This method returns the service object that is
// registered under the given name.  
// **Parameters**:  
// *{string}* **name** ...  
// **Throws**:  
// *{Error}* if no service could be found.  
// **Returns**:  
// *{object}* : The service to be queried.  
// **Example usage**:  
//
//     var vie = new VIE();
//     vie.use(new vie.StanbolService(), "stanbol");
//     var service = vie.service("stanbol");
VIE.prototype.service = function(name) {
  if (!this.services[name]) {
    throw "Undefined service " + name;
  }
  return this.services[name];
};

// ### getServicesArray()
// This method returns an array of all registered services.  
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{array}* : An array of service instances.  
// **Example usage**:  
//
//     var vie = new VIE();
//     vie.use(new vie.StanbolService(), "stanbol");
//     var services = vie.getServicesArray();
//     services.length; // <-- 1
VIE.prototype.getServicesArray = function() {
  return _.map(this.services, function (v) {return v;});
};

// ### load(options)
// This method instantiates a new VIE.Loadable in order to
// perform queries on the services.  
// **Parameters**:  
// *{object}* **options** Options to be set.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Loadable}* : A new instance of VIE.Loadable.  
// **Example usage**:  
//
//     var vie = new VIE();
//     vie.use(new vie.StanbolService(), "stanbol");
//     var loader = vie.load({...});
VIE.prototype.load = function(options) {
  if (!options) { options = {}; }
  options.vie = this;
  return new this.Loadable(options);
};

// ### save(options)
// This method instantiates a new VIE.Savable in order to
// perform queries on the services.  
// **Parameters**:  
// *{object}* **options** Options to be set.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Savable}* : A new instance of VIE.Savable.  
// **Example usage**:  
//
//     var vie = new VIE();
//     vie.use(new vie.StanbolService(), "stanbol");
//     var saver = vie.save({...});
VIE.prototype.save = function(options) {
  if (!options) { options = {}; }
  options.vie = this;
  return new this.Savable(options);
};

// ### remove(options)
// This method instantiates a new VIE.Removable in order to
// perform queries on the services.  
// **Parameters**:  
// *{object}* **options** Options to be set.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Removable}* : A new instance of VIE.Removable.  
// **Example usage**:  
//
//     var vie = new VIE();
//     vie.use(new vie.StanbolService(), "stanbol");
//     var remover = vie.remove({...});
VIE.prototype.remove = function(options) {
  if (!options) { options = {}; }
  options.vie = this;
  return new this.Removable(options);
};

// ### analyze(options)
// This method instantiates a new VIE.Analyzable in order to
// perform queries on the services.  
// **Parameters**:  
// *{object}* **options** Options to be set.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Analyzable}* : A new instance of VIE.Analyzable.  
// **Example usage**:  
//
//     var vie = new VIE();
//     vie.use(new vie.StanbolService(), "stanbol");
//     var analyzer = vie.analyze({...});
VIE.prototype.analyze = function(options) {
  if (!options) { options = {}; }
  options.vie = this;
  return new this.Analyzable(options);
};

// ### find(options)
// This method instantiates a new VIE.Findable in order to
// perform queries on the services.  
// **Parameters**:  
// *{object}* **options** Options to be set.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Findable}* : A new instance of VIE.Findable.  
// **Example usage**:  
//
//     var vie = new VIE();
//     vie.use(new vie.StanbolService(), "stanbol");
//     var finder = vie.find({...});
VIE.prototype.find = function(options) {
  if (!options) { options = {}; }
  options.vie = this;
  return new this.Findable(options);
};

// ### loadSchema(url, options)
// VIE only knows the *owl:Thing* type by default.
// You can use this method to import another
// schema (ontology) from an external resource.
// (Currently, this supports only the JSON format!!)
// As this method works asynchronously, you might want
// to register `success` and `error` callbacks via the
// options.  
// **Parameters**:  
// *{string}* **url** The url, pointing to the schema to import.  
// *{object}* **options** Options to be set.
// (Set ```success``` and ```error``` as callbacks.).  
// **Throws**:  
// *{Error}* if the url is not set.  
// **Returns**:  
// *{VIE}* : The VIE instance itself.  
// **Example usage**:  
//
//     var vie = new VIE();
//     vie.loadSchema("http://schema.rdfs.org/all.json", 
//        {
//          baseNS : "http://schema.org/",
//          succes : function () {console.log("success");},
//          error  : function (msg) {console.warn(msg);}
//        });
VIE.prototype.loadSchema = function(url, options) {
    options = (!options)? {} : options;
    
    if (!url) {
        throw new Error("Please provide a proper URL");
    }
    else {
        var vie = this;
        jQuery.getJSON(url)
        .success(function(data) {
            VIE.Util.loadSchemaOrg(vie, data, options.baseNS);
            if (options.success) {
                options.success.call(vie);
            }
         })
        .error(function(data, textStatus, jqXHR) { 
            if (options.error) {
                console.warn(data, textStatus, jqXHR);
                options.error.call(vie, "Could not load schema from URL (" + url + ")");
            }
         });
    }
    
    return this;
};

// ## Running VIE on Node.js
//
// When VIE is running under Node.js we can use the CommonJS
// require interface to load our dependencies automatically.
//
// This means Node.js users don't need to care about dependencies
// and can just run VIE with:
//
//     var VIE = require('vie');
//
// In browser environments the dependencies have to be included
// before including VIE itself.
if(typeof exports === 'object') {
    exports.VIE = VIE;

    if (!jQuery) {
        jQuery = require('jquery');
    }
    if (!Backbone) {
        Backbone = require('backbone');
    }
    if (!_) {
        _ = require('underscore')._;
    }
}
//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/

// ## VIE.Able
// VIE implements asynchronius service methods through
// [jQuery.Deferred](http://api.jquery.com/category/deferred-object/) objects.
// Loadable, Analysable, Savable, etc. are part of the VIE service API and 
// are implemented with the generic VIE.Able class.
// Example:
//
//      VIE.prototype.Loadable = function (options) {
//          this.init(options,"load");
//      };
//      VIE.prototype.Loadable.prototype = new VIE.prototype.Able();
//
// This defines 
//
//     someVIEService.load(options)
//     .using(...)
//     .execute()
//     .success(...)
//     .fail(...)
// which will run the asynchronius `load` function of the service with the created Loadable
// object.

// ### VIE.Able()
// This is the constructor of a VIE.Able. This should not be called
// globally but using the inherited classes below.  
// **Parameters**: 
// *nothing*  
// **Throws**: 
// *nothing*  
// **Returns**: 
// *{VIE.Able}* : A **new** VIE.Able object. 
// Example:
//
//      VIE.prototype.Loadable = function (options) {
//          this.init(options,"load");
//      };
//      VIE.prototype.Loadable.prototype = new VIE.prototype.Able();
VIE.prototype.Able = function(){

// ### init(options, methodName)
// Internal method, called during initialization.
// **Parameters**:  
// *{object}* **options** the *able* options coming from the API call
// *{string}* **methodName** the service method called on `.execute`.
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Able}* : The current instance.  
// **Example usage**:  
//
//      VIE.prototype.Loadable = function (options) {
//          this.init(options,"load");
//      };
//      VIE.prototype.Loadable.prototype = new VIE.prototype.Able();
    this.init = function(options, methodName) {
        this.options = options;
        this.services = options.from || options.using || options.to || [];
        this.vie = options.vie;

        this.methodName = methodName;

        // Instantiate the deferred object
        this.deferred = jQuery.Deferred();

// In order to get more information and documentation about the passed-through
// deferred methods and their synonyms, please see the documentation of 
// the [jQuery.Deferred object](http://api.jquery.com/category/deferred-object/)
        /* Public deferred-methods */
        this.resolve = this.deferred.resolve;
        this.resolveWith = this.deferred.resolveWith;
        this.reject = this.deferred.reject;
        this.rejectWith = this.deferred.rejectWith;
        this.success = this.done = this.deferred.done;
        this.fail = this.deferred.fail;
        this.then = this.deferred.then;
        this.always = this.deferred.always;
        this.from = this.using;
        this.to = this.using;

        return this;
    };


// ### using(services)
// This method registers services with the current able instance.  
// **Parameters**:  
// *{string|array}* **services** An id of a service or an array of strings.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Able}* : The current instance.  
// **Example usage**:  
//
//     var loadable = vie.load({id: "http://example.com/entity/1234"});
//     able.using("myService");
    this.using = function(services) {
        var self = this;
        services = (_.isArray(services))? services : [ services ];
        _.each (services, function (s) {
            var obj = (typeof s === "string")? self.vie.service(s) : s;
            self.services.push(obj);
        });
        return this;
    };
    
// ### execute()
// This method runs the actual method on all registered services.  
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing* ...   
// **Returns**:  
// *{VIE.Able}* : The current instance.  
// **Example usage**:  
//
//     var able = new vie.Able().init();
//     able.using("stanbol")
//     .done(function () {alert("finished");})
//     .execute();
    this.execute = function() {
        /* call service[methodName] */
        var able = this;
        _(this.services).each(function(service){
            service[able.methodName](able);
        });
        return this;
    };
};

// ## VIE.Loadable
// A ```VIE.Loadable``` is a wrapper around the deferred object
// to **load** semantic data from a semantic web service.
VIE.prototype.Loadable = function (options) {
    this.init(options,"load");
};
VIE.prototype.Loadable.prototype = new VIE.prototype.Able();

// ## VIE.Savable
// A ```VIE.Savable``` is a wrapper around the deferred object
// to **save** entities by a VIE service. The RDFaService would write the data
// in the HTML as RDFa, the StanbolService stores the data in its Entityhub, etc.
VIE.prototype.Savable = function(options){
    this.init(options, "save");
};
VIE.prototype.Savable.prototype = new VIE.prototype.Able();

// ## VIE.Removable
// A ```VIE.Removable``` is a wrapper around the deferred object
// to **remove** semantic data from a semantic web service.
VIE.prototype.Removable = function(options){
    this.init(options, "remove");
};
VIE.prototype.Removable.prototype = new VIE.prototype.Able();

// ## VIE.Analyzable
// A ```VIE.Analyzable``` is a wrapper around the deferred object
// to **analyze** data and extract semantic information with the
// help of a semantic web service.
VIE.prototype.Analyzable = function (options) {
    this.init(options, "analyze");
};
VIE.prototype.Analyzable.prototype = new VIE.prototype.Able();

// ## VIE.Findable
// A ```VIE.Findable``` is a wrapper around the deferred object
// to **find** semantic data on a semantic storage.
VIE.prototype.Findable = function (options) {
    this.init(options, "find");
};
VIE.prototype.Findable.prototype = new VIE.prototype.Able();

//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/

// ## VIE Utils
//
// The here-listed methods are utility methods for the day-to-day 
// VIE.js usage. All methods are within the static namespace ```VIE.Util```.
VIE.Util = {

// ### VIE.Util.toCurie(uri, safe, namespaces)
// This method converts a given 
// URI into a CURIE (or SCURIE), based on the given ```VIE.Namespaces``` object.
// If the given uri is already a URI, it is left untouched and directly returned.
// If no prefix could be found, an ```Error``` is thrown.  
// **Parameters**:  
// *{string}* **uri** The URI to be transformed.  
// *{boolean}* **safe** A flag whether to generate CURIEs or SCURIEs.  
// *{VIE.Namespaces}* **namespaces** The namespaces to be used for the prefixes.  
// **Throws**:  
// *{Error}* If no prefix could be found in the passed namespaces.  
// **Returns**:  
// *{string}* The CURIE or SCURIE.  
// **Example usage**: 
//
//     var ns = new myVIE.Namespaces(
//           "http://viejs.org/ns/", 
//           { "dbp": "http://dbpedia.org/ontology/" }
//     );
//     var uri = "<http://dbpedia.org/ontology/Person>";
//     VIE.Util.toCurie(uri, false, ns); // --> dbp:Person
//     VIE.Util.toCurie(uri, true, ns); // --> [dbp:Person]
	toCurie : function (uri, safe, namespaces) {
        if (VIE.Util.isCurie(uri, namespaces)) {
            return uri;
        }
        var delim = ":";
        for (var k in namespaces.toObj()) {
            if (uri.indexOf(namespaces.get(k)) === 1) {
                var pattern = new RegExp("^" + "<?" + namespaces.get(k));
                if (k === '') {
                    delim = '';
                }
                return ((safe)? "[" : "") + 
                        uri.replace(pattern, k + delim).replace(/>$/, '') +
                        ((safe)? "]" : "");
            }
        }
        throw new Error("No prefix found for URI '" + uri + "'!");
    },

// ### VIE.Util.isCurie(curie, namespaces)
// This method checks, whether 
// the given string is a CURIE and returns ```true``` if so and ```false```otherwise.  
// **Parameters**:  
// *{string}* **curie** The CURIE (or SCURIE) to be checked.  
// *{VIE.Namespaces}* **namespaces** The namespaces to be used for the prefixes.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{boolean}* ```true``` if the given curie is a CURIE or SCURIE and ```false``` otherwise.  
// **Example usage**: 
//
//     var ns = new myVIE.Namespaces(
//           "http://viejs.org/ns/", 
//           { "dbp": "http://dbpedia.org/ontology/" }
//     );
//     var uri = "<http://dbpedia.org/ontology/Person>";
//     var curie = "dbp:Person";
//     var scurie = "[dbp:Person]";
//     var text = "This is some text.";
//     VIE.Util.isCurie(uri, ns);    // --> false
//     VIE.Util.isCurie(curie, ns);  // --> true
//     VIE.Util.isCurie(scurie, ns); // --> true
//     VIE.Util.isCurie(text, ns);   // --> false
    isCurie : function (curie, namespaces) {
        if (VIE.Util.isUri(curie)) {
            return false;
        } else {
            try {
                VIE.Util.toUri(curie, namespaces);
                return true;
            } catch (e) {
                return false;
            }
        }
    },

// ### VIE.Util.toUri(curie, namespaces)
// This method converts a 
// given CURIE (or save CURIE) into a URI, based on the given ```VIE.Namespaces``` object.  
// **Parameters**:  
// *{string}* **curie** The CURIE to be transformed.  
// *{VIE.Namespaces}* **namespaces** The namespaces object  
// **Throws**:  
// *{Error}* If no URI could be assembled.  
// **Returns**:  
// *{string}* : A string, representing the URI.  
// **Example usage**: 
//
//     var ns = new myVIE.Namespaces(
//           "http://viejs.org/ns/", 
//           { "dbp": "http://dbpedia.org/ontology/" }
//     );
//     var curie = "dbp:Person";
//     var scurie = "[dbp:Person]";
//     VIE.Util.toUri(curie, ns); 
//          --> <http://dbpedia.org/ontology/Person>
//     VIE.Util.toUri(scurie, ns);
//          --> <http://dbpedia.org/ontology/Person>
    toUri : function (curie, namespaces) {
        var delim = ":";
        for (var prefix in namespaces.toObj()) {
            if (prefix !== "" && (curie.indexOf(prefix + ":") === 0 || curie.indexOf("[" + prefix + ":") === 0)) {
                var pattern = new RegExp("^" + "\\[{0,1}" + prefix + delim);
                return "<" + curie.replace(pattern, namespaces.get(prefix)).replace(/\]{0,1}$/, '') + ">";
            }
        }
        /* check for the default namespace */
        if (curie.indexOf(delim) === -1) {
            return "<" + namespaces.base() + curie + ">";
        }
        throw new Error("No prefix found for CURIE '" + curie + "'!");
    },
    
// ### VIE.Util.isUri(something)
// This method checks, whether the given string is a URI.  
// **Parameters**:  
// *{string}* **something** : The string to be checked.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{boolean}* : ```true``` if the string is a URI, ```false``` otherwise.  
// **Example usage**: 
//
//     var uri = "<http://dbpedia.org/ontology/Person>";
//     var curie = "dbp:Person";
//     VIE.Util.isUri(uri);   // --> true
//     VIE.Util.isUri(curie); // --> false
    isUri : function (something) {
        return (typeof something === "string" && something.search(/^<.+>$/) === 0);
    },
    
// ### VIE.Util.rdf2Entities(service, results)
// This method converts *rdf/json* data from an external service
// into VIE.Entities.  
// **Parameters**:  
// *{object}* **service** The service that retrieved the data.  
// *{object}* **results** The data to be transformed.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{[VIE.Entity]}* : An array, containing VIE.Entity instances which have been transformed from the given data.
    rdf2Entities: function (service, results) {
        if (typeof jQuery.rdf !== 'function') {
            /* fallback if no rdfQuery has been loaded */
            return VIE.Util._rdf2EntitiesNoRdfQuery(service, results);
        }
        try {
	        var rdf = (results instanceof jQuery.rdf)? results : jQuery.rdf().load(results, {});
	
	        rdf.base(service.vie.namespaces.base());
	        /* if the service contains rules to apply special transformation, they are executed here.*/
	        if (service.rules) {
	            var rules = jQuery.rdf.ruleset();
	            for (var prefix in service.vie.namespaces.toObj()) {
	                if (prefix !== "") {
	                    rules.prefix(prefix, service.vie.namespaces.get(prefix));
	                }
	            }
	            for (var i = 0; i < service.rules.length; i++)if(service.rules.hasOwnProperty(i)) {
	                var rule = service.rules[i];
	                rules.add(rule['left'], rule['right']);
	            }
	            rdf = rdf.reason(rules, 10); /* execute the rules only 10 times to avoid looping */
	        }
	        var entities = {};
	        rdf.where('?subject ?property ?object').each(function() {
	            var subject = this.subject.toString();
	            if (!entities[subject]) {
	                entities[subject] = {
	                    '@subject': subject,
	                    '@context': service.vie.namespaces.toObj(true),
	                    '@type': []
	                };
	            }
	            var propertyUri = this.property.toString();
	            var propertyCurie;
	
	            try {
	                propertyCurie = service.vie.namespaces.curie(propertyUri);
	                //jQuery.createCurie(propertyUri, {namespaces: service.vie.namespaces.toObj(true)});
	            } catch (e) {
	                propertyCurie = propertyUri;
	                console.warn(propertyUri + " doesn't have a namespace definition in '", service.vie.namespaces.toObj());
	            }
	            entities[subject][propertyCurie] = entities[subject][propertyCurie] || [];
	
	            function getValue(rdfQueryLiteral){
	                if(typeof rdfQueryLiteral.value === "string"){
	                    if (rdfQueryLiteral.lang)
	                        return rdfQueryLiteral.toString();
	                    else
	                        return rdfQueryLiteral.value;
	                    return rdfQueryLiteral.value.toString();
	                } else if (rdfQueryLiteral.type === "uri"){
	                    return rdfQueryLiteral.toString();
	                } else {
	                    return rdfQueryLiteral.value;
	                }
	            }
	            entities[subject][propertyCurie].push(getValue(this.object));
	        });
	
	        _(entities).each(function(ent){
	            ent["@type"] = ent["@type"].concat(ent["rdf:type"]);
	            delete ent["rdf:type"];
	            _(ent).each(function(value, property){
	                if(value.length === 1){
	                    ent[property] = value[0];
	                }
	            });
	        });
	
	        var vieEntities = [];
	        jQuery.each(entities, function() {
	            var entityInstance = new service.vie.Entity(this);
	            entityInstance = service.vie.entities.addOrUpdate(entityInstance);
	            vieEntities.push(entityInstance);
	        });
	        return vieEntities;
        } catch (e) {
        	console.warn("Something went wrong while parsing the returned results!", e);
        	return [];
        }
    },
    
// ### VIE.Util._rdf2EntitiesNoRdfQuery(service, results)
// This is a **private** method which should
// only be accessed through ```VIE.Util._rdf2Entities()``` and is a helper method in case there is no
// rdfQuery loaded (*not recommended*).  
// **Parameters**:  
// *{object}* **service** The service that retrieved the data.  
// *{object}* **results** The data to be transformed.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{[VIE.Entity]}* : An array, containing VIE.Entity instances which have been transformed from the given data.
    _rdf2EntitiesNoRdfQuery: function (service, results) {
        var jsonLD = [];
        _.forEach(results, function(value, key) {
            var entity = {};
            entity['@subject'] = '<' + key + '>';
            _.forEach(value, function(triples, predicate) {
                predicate = '<' + predicate + '>';
                _.forEach(triples, function(triple) {
                    if (triple.type === 'uri') {
                        triple.value = '<' + triple.value + '>';
                    }

                    if (entity[predicate] && !_.isArray(entity[predicate])) {
                        entity[predicate] = [entity[predicate]];
                    }

                    if (_.isArray(entity[predicate])) {
                        entity[predicate].push(triple.value);
                        return;
                    }
                    entity[predicate] = triple.value;
                });
            });
            jsonLD.push(entity);
        });
        return jsonLD;
    },

// ### VIE.Util.loadSchemaOrg(vie, SchemaOrg, baseNS)
// This method is a wrapper around
// the <a href="http://schema.org/">schema.org</a> ontology. It adds all the
// given types and properties as ```VIE.Type``` instances to the given VIE instance.
// If the paramenter **baseNS** is set, the method automatically sets the namespace
// to the provided one. If it is not set, it will keep the base namespace of VIE untouched.  
// **Parameters**:  
// *{VIE}* **vie** The instance of ```VIE```.   
// *{object}* **SchemaOrg** The data imported from schema.org.   
// *{string|undefined}* **baseNS** If set, this will become the new baseNamespace within the given ```VIE``` instance.   
// **Throws**:  
// *{Error}* If the parameter was not given.  
// **Returns**:  
// *nothing*
    loadSchemaOrg : function (vie, SchemaOrg, baseNS) {
    
        if (!SchemaOrg) {
            throw new Error("Please load the schema.json file.");
        }
        vie.types.remove("<http://schema.org/Thing>");
        
        var baseNSBefore = (baseNS)? baseNS : vie.namespaces.base();
        vie.namespaces.base(baseNS);
        
        var datatypeMapping = {
            'DataType': 'xsd:anyType',
            'Boolean' : 'xsd:boolean',
            'Date'    : 'xsd:date',
            'Float'   : 'xsd:float',
            'Integer' : 'xsd:integer',
            'Number'  : 'xsd:anySimpleType',
            'Text'    : 'xsd:string',
            'URL'     : 'xsd:anyURI'
        };
        
        var dataTypeHelper = function (ancestors, id) {
            var type = vie.types.add(id, [{'id' : 'value', 'range' : datatypeMapping[id]}]);
            
            for (var i = 0; i < ancestors.length; i++) {
                var supertype = (vie.types.get(ancestors[i]))? vie.types.get(ancestors[i]) :
                    dataTypeHelper.call(vie, SchemaOrg["datatypes"][ancestors[i]].supertypes, ancestors[i]);
                type.inherit(supertype);
            }
            return type;
        };
        
        for (var dt in SchemaOrg["datatypes"]) {
            if (!vie.types.get(dt)) {
                var ancestors = SchemaOrg["datatypes"][dt].supertypes;
                dataTypeHelper.call(vie, ancestors, dt);
            }
        }
        
        var typeProps = function (id) {
            var props = [];
            var specProps = SchemaOrg["types"][id]["specific_properties"];
            for (var p = 0; p < specProps.length; p++) {
                var pId = specProps[p];
                var range = SchemaOrg["properties"][pId]["ranges"];
                props.push({
                    'id'    : pId,
                    'range' : range
                });
            }
            return props;
        };
        
        var typeHelper = function (ancestors, id, props) {
            var type = vie.types.add(id, props);
           
            for (var i = 0; i < ancestors.length; i++) {
                var supertype = (vie.types.get(ancestors[i]))? vie.types.get(ancestors[i]) :
                    typeHelper.call(vie, SchemaOrg["types"][ancestors[i]].supertypes, ancestors[i], typeProps.call(vie, ancestors[i]));
                type.inherit(supertype);
            }
            if (id === "Thing" && !type.isof("owl:Thing")) {
                type.inherit("owl:Thing");
            }
            return type;
        };
        
        for (var t in SchemaOrg["types"]) {
            if (!vie.types.get(t)) {
                var ancestors = SchemaOrg["types"][t].supertypes;
                typeHelper.call(vie, ancestors, t, typeProps.call(vie, t));
            }
        }
        /* set the namespace to either the old value or the provided baseNS value */
        vie.namespaces.base(baseNSBefore);
    },

// ### VIE.Util.xsdDateTime(date)
// This transforms a ```Date``` instance into an xsd:DateTime format.  
// **Parameters**:  
// *{```Date```}* **date** An instance of a javascript ```Date```.  
// **Throws**: 
// *nothing*..  
// **Returns**: 
// *{string}* A string representation of the dateTime in the xsd:dateTime format.
    xsdDateTime : function(date) {
        function pad(n) {
            var s = n.toString();
            return s.length < 2 ? '0'+s : s;
        };

        var yyyy = date.getFullYear();
        var mm1  = pad(date.getMonth()+1);
        var dd   = pad(date.getDate());
        var hh   = pad(date.getHours());
        var mm2  = pad(date.getMinutes());
        var ss   = pad(date.getSeconds());

        return yyyy +'-' +mm1 +'-' +dd +'T' +hh +':' +mm2 +':' +ss;
    },
    
// ### VIE.Util.transformationRules(service)
// This returns a default set of rdfQuery rules that transform semantic data into the
// VIE entity types.  
// **Parameters**:  
// *{object}* **service** An instance of a vie.service.  
// **Throws**: 
// *nothing*..  
// **Returns**: 
// *{array}* An array of rules with 'left' and 'right' side.
    transformationRules : function (service) {
        var res = [
            // rule(s) to transform a dbpedia:Person into a VIE:Person
             {
                'left' : [
                    '?subject a dbpedia:Person',
                    '?subject rdfs:label ?label'
                 ],
                 'right': function(ns){
                     return function(){
                         return [
                             jQuery.rdf.triple(this.subject.toString(),
                                 'a',
                                 '<' + ns.base() + 'Person>', {
                                     namespaces: ns.toObj()
                                 }),
                             jQuery.rdf.triple(this.subject.toString(),
                                 '<' + ns.base() + 'name>',
                                 this.label, {
                                     namespaces: ns.toObj()
                                 })
                             ];
                     };
                 }(service.vie.namespaces)
             },
             // rule(s) to transform a foaf:Person into a VIE:Person
             {
             'left' : [
                     '?subject a foaf:Person',
                     '?subject rdfs:label ?label'
                  ],
                  'right': function(ns){
                      return function(){
                          return [
                              jQuery.rdf.triple(this.subject.toString(),
                                  'a',
                                  '<' + ns.base() + 'Person>', {
                                      namespaces: ns.toObj()
                                  }),
                              jQuery.rdf.triple(this.subject.toString(),
                                  '<' + ns.base() + 'name>',
                                  this.label, {
                                      namespaces: ns.toObj()
                                  })
                              ];
                      };
                  }(service.vie.namespaces)
              },
             // rule(s) to transform a dbpedia:Place into a VIE:Place
             {
                 'left' : [
                     '?subject a dbpedia:Place',
                     '?subject rdfs:label ?label'
                  ],
                  'right': function(ns) {
                      return function() {
                          return [
                          jQuery.rdf.triple(this.subject.toString(),
                              'a',
                              '<' + ns.base() + 'Place>', {
                                  namespaces: ns.toObj()
                              }),
                          jQuery.rdf.triple(this.subject.toString(),
                                  '<' + ns.base() + 'name>',
                              this.label.toString(), {
                                  namespaces: ns.toObj()
                              })
                          ];
                      };
                  }(service.vie.namespaces)
              },
             // rule(s) to transform a dbpedia:City into a VIE:City
              {
                 'left' : [
                     '?subject a dbpedia:City',
                     '?subject rdfs:label ?label',
                     '?subject dbpedia:abstract ?abs',
                     '?subject dbpedia:country ?country'
                  ],
                  'right': function(ns) {
                      return function() {
                          return [
                          jQuery.rdf.triple(this.subject.toString(),
                              'a',
                              '<' + ns.base() + 'City>', {
                                  namespaces: ns.toObj()
                              }),
                          jQuery.rdf.triple(this.subject.toString(),
                                  '<' + ns.base() + 'name>',
                              this.label.toString(), {
                                  namespaces: ns.toObj()
                              }),
                          jQuery.rdf.triple(this.subject.toString(),
                                  '<' + ns.base() + 'description>',
                              this.abs.toString(), {
                                  namespaces: ns.toObj()
                              }),
                          jQuery.rdf.triple(this.subject.toString(),
                                  '<' + ns.base() + 'containedIn>',
                              this.country.toString(), {
                                  namespaces: ns.toObj()
                              })
                          ];
                      };
                  }(service.vie.namespaces)
              },
        ];
        return res;
    }
};

VIE.prototype.Entity = function(attrs, opts) {

    attrs = (attrs)? attrs : {};
    opts = (opts)? opts : {};

    var self = this;

    var mapAttributeNS = function (attr, ns) {
        var a = attr;
        if (ns.isUri (attr) || attr.indexOf('@') === 0) {
            //ignore
        } else if (ns.isCurie(attr)) {
            a = ns.uri(attr);
        } else if (!ns.isUri(attr)) {
            if (attr.indexOf(":") === -1) {
                a = '<' + ns.base() + attr + '>';
            } else {
                a = '<' + attr + '>';
            }
        }
        return a;
    };

    if (attrs['@type'] !== undefined) {
        attrs['@type'] = (_.isArray(attrs['@type']))? attrs['@type'] : [ attrs['@type'] ];
        attrs['@type'] = _.map(attrs['@type'], function(val){
            if (!self.vie.types.get(val)) {
                //if there is no such type -> add it and let it inherit from "owl:Thing"
                self.vie.types.add(val).inherit("owl:Thing");
            }
            return self.vie.types.get(val).id;
        });
        attrs['@type'] = (attrs['@type'].length === 1)? attrs['@type'][0] : attrs['@type'];
    } else {
        // provide "owl:Thing" as the default type if none was given
        attrs['@type'] = self.vie.types.get("owl:Thing").id;
    }

    //the following provides full seamless namespace support
    //for attributes. It should not matter, if you
    //query for `model.get('name')` or `model.get('foaf:name')`
    //or even `model.get('http://xmlns.com/foaf/0.1/name');`
    //However, if we just overwrite `set()` and `get()`, this
    //raises a lot of side effects, so we need to expand
    //the attributes before we create the model.
    _.each (attrs, function (value, key) {
        var newKey = mapAttributeNS(key, this.namespaces);
        if (key !== newKey) {
            delete attrs[key];
            attrs[newKey] = value;
        }
    }, self.vie);

    var Model = Backbone.Model.extend({
        idAttribute: '@subject',

        initialize: function(attributes, options) {
            if (attributes['@subject']) {
                this.id = this['@subject'] = this.toReference(attributes['@subject']);
            } else {
                this.id = this['@subject'] = attributes['@subject'] = this.cid.replace('c', '_:bnode');
            }
            return this;
        },

        get: function (attr) {
            attr = mapAttributeNS(attr, self.vie.namespaces);
            var value = Backbone.Model.prototype.get.call(this, attr);
            value = (_.isArray(value))? value : [ value ];

            value = _.map(value, function(v) {
                if (v !== undefined && attr === '@type' && self.vie.types.get(v)) {
                    return self.vie.types.get(v);
                } else if (v !== undefined && self.vie.entities.get(v)) {
                    return self.vie.entities.get(v);
                } else {
                    return v;
                }
            }, this);
            if(value.length === 0) {
                return undefined;
            }
            // if there is only one element, just return that one
            value = (value.length === 1)? value[0] : value;
            return value;
        },

        has: function(attr) {
            attr = mapAttributeNS(attr, self.vie.namespaces);
            return Backbone.Model.prototype.has.call(this, attr);
        },

        set : function(attrs, options, opts) {
            
            if (!attrs) {
                return this;
            }
            
            if (typeof attrs === "string") {
                var obj = {};
                obj[attrs] = options;
                return this.set(obj, opts);
            }
            if (attrs.attributes) {
                attrs = attrs.attributes;
            }
            var self = this;
            _.each (attrs, function (value, key) {
                var newKey = mapAttributeNS(key, self.vie.namespaces);
                if (key !== newKey) {
                    delete attrs[key];
                    attrs[newKey] = value;
                }
            }, this);
            _.each (attrs, function (value, key) {
               if (!value) { return; }
               if (key.indexOf('@') === -1) {
                   if (value.isCollection) {
                       // ignore
                       value.each(function (child) {
                           self.vie.entities.addOrUpdate(child);
                       });
                   } else if (value.isEntity) {
                       self.vie.entities.addOrUpdate(value);
                       var coll = new self.vie.Collection();
                       coll.add(value);
                       attrs[key] = coll;
                   } else if (_.isArray(value)) {
                       // ignore
                   } else if (typeof value == "object") {
                       var child = new self.vie.Entity(value, options);
                       self.vie.entities.addOrUpdate(child);
                       var coll = new self.vie.Collection();
                       coll.add(value);
                       attrs[key] = coll;
                   } else {
                       // ignore
                   }
               }
            }, this);
            return Backbone.Model.prototype.set.call(this, attrs, options);
        },

        unset: function (attr, opts) {
            attr = mapAttributeNS(attr, self.vie.namespaces);
            return Backbone.Model.prototype.unset.call(this, attr, opts);
        },

        getSubject: function(){
            if (typeof this.id === "undefined") {
                this.id = this.attributes[this.idAttribute];
            }
            if (typeof this.id === 'string') {
                if (this.id.substr(0, 7) === 'http://' || this.id.substr(0, 4) === 'urn:') {
                    return this.toReference(this.id);
                }
                return this.id;
            }
            return this.cid.replace('c', '_:bnode');
        },

        getSubjectUri: function(){
            return this.fromReference(this.getSubject());
        },

        toReference: function(uri){
            var ns = this.vie.namespaces;
            var ret = uri;
            if (uri.substring(0, 2) === "_:") {
                ret = uri;
            }
            else if (ns.isCurie(uri)) {
                ret = ns.uri(uri);
                if (ret === "<" + ns.base() + uri + ">") {
                    /* no base namespace extension with IDs */
                    ret = '<' + uri + '>';
                }
            } else if (!ns.isUri(uri)) {
                ret = '<' + uri + '>';
            }
            return ret;
        },

        fromReference: function(uri){
            var ns = this.vie.namespaces;
            if (!ns.isUri(uri)) {
                return uri;
            }
            return uri.substring(1, uri.length - 1);
        },

        as: function(encoding){
            if (encoding === "JSON") {
                return this.toJSON();
            }
            if (encoding === "JSONLD") {
                return this.toJSONLD();
            }
            throw new Error("Unknown encoding " + encoding);
        },

        toJSONLD: function(){
            var instanceLD = {};
            var instance = this;
            _.each(instance.attributes, function(value, name){
                var entityValue = value; //instance.get(name);

                if (value instanceof instance.vie.Collection) {
                    entityValue = value.map(function(instance) {
                        return instance.getSubject();
                    });
                }

                // TODO: Handle collections separately
                instanceLD[name] = entityValue;
            });

            instanceLD['@subject'] = instance.getSubject();

            return instanceLD;
        },

        setOrAdd: function (arg1, arg2) {
            var entity = this;
            if (typeof arg1 === "string" && arg2) {
                // calling entity.setOrAdd("rdfs:type", "example:Musician")
                entity._setOrAddOne(arg1, arg2);
            }
            else
                if (typeof arg1 === "object") {
                    // calling entity.setOrAdd({"rdfs:type": "example:Musician", ...})
                    _(arg1).each(function(val, key){
                        entity._setOrAddOne(key, val);
                    });
                }
            return this;
        },


        /* attr is always of type string */
        /* value can be of type: undefined,string,int,double,object,array,VIE.Entity,VIE.Collection */
       /*  val can be of type: undefined,string,int,double,array,VIE.Collection */
       
        /* depending on the type of value and the type of val, different actions need to be made */
        _setOrAddOne: function (attr, value) {
            if (!attr || !value)
                return;
                
            attr = mapAttributeNS(attr, self.vie.namespaces);
            
            if (_.isArray(value)) {
                for (var v = 0; v < value.length; v++) {
                    this._setOrAddOne(attr, value[v]);
                }
                return;
            }
            
            if (attr === "@type" && value instanceof self.vie.Type) {
            	value = value.id
            }
            
            var obj = {};
            var existing = Backbone.Model.prototype.get.call(this, attr);
            
            if (!existing) {
                obj[attr] = value;
                this.set(obj);
            } else if (existing.isCollection) {
                if (value.isCollection) {
                    value.each(function (model) {
                        existing.add(model);
                    });
                } else if (value.isEntity) {
                    existing.add(value);
                } else if (typeof value === "object") {
                    value = new this.vie.Entity(value);
                    existing.add(value);
                } else {
                    throw new Error("you cannot add a literal to a collection of entities!");
                }
                this.trigger('change:' + attr, this, value, {});
                this.change({});
            } else if (_.isArray(existing)) {
                if (value.isCollection) {
                    throw new Error("you cannot add a collection of entities to an array of literals!");
                } else if (value.isEntity) {
                	this._setOrAddOne(attr, value.getSubject());
                    /*throw new Error("you cannot add an entity to an array of literals!");*/
                } else if (typeof value === "object") {
                    throw new Error("you cannot add an entity of entities to an array of literals!");
                } else {
                    existing.push(value);
                    obj[attr] = existing;
                    this.set(obj);
                }
            } else {
                if (value.isCollection) {
                    throw new Error("you cannot add a collection of entities to a literal!");
                } else if (value.isEntity) {
                	this._setOrAddOne(attr, value.getSubject());
                    /*throw new Error("you cannot add an entity to a literal!");*/
                } else if (typeof value === "object") {
                    throw new Error("you cannot add an entity of entities to a literal!");
                } else {
                    /* yes, we allow multiple equal literals */
                    var arr = [];
                    arr.push(existing);
                    arr.push(value);
                    obj[attr] = arr;
                    this.set(obj);
                }
            }
            return;
        },

        hasType: function(type){
            type = self.vie.types.get(type);
            return this.hasPropertyValue("@type", type);
        },

        hasPropertyValue: function(property, value) {
            var t = this.get(property);
            if (!(value instanceof Object)) {
                value = self.vie.entities.get(value);
            }
            if (t instanceof Array) {
                return t.indexOf(value) !== -1;
            }
            else {
                return t === value;
            }
        },

        isof: function (type) {
            var types = this.get('@type');
            
            if (types === undefined) {
                return false;
            }
            types = (_.isArray(types))? types : [ types ];
            
            type = (self.vie.types.get(type))? self.vie.types.get(type) : new self.vie.Type(type);
            for (var t = 0; t < types.length; t++) {
                if (self.vie.types.get(types[t])) {
                    if (self.vie.types.get(types[t]).isof(type)) {
                        return true;
                    }
                } else {
                    var typeTmp = new self.vie.Type(types[t]);
                    if (typeTmp.id === type.id) {
                        return true;
                    }
                }
            }
            return false;
        },
        
        addTo : function (collection, update) {
            var self = this;
            if (collection instanceof self.vie.Collection) {
                if (update) {
                    collection.addOrUpdate(self);
                } else {
                    collection.add(self);
                }
                return this;
            }
            throw new Error("Please provide a proper collection of type VIE.Collection as argument!");
        },

        isEntity: true,

        vie: self.vie
    });

    return new Model(attrs, opts);
};
VIE.prototype.Collection = Backbone.Collection.extend({
    model: VIE.prototype.Entity,
    
    get: function(id) {
        if (id === null) {
            return null;
        }
        
        id = (id.getSubject)? id.getSubject() : id;        
        if (typeof id === "string" && id.indexOf("_:") === 0) {
            if (id.indexOf("bnode") === 2) {
                //bnode!
                id = id.replace("_:bnode", 'c');
                return this._byCid[id];
            } else {
                return this._byId["<" + id + ">"];
            }
        } else {
            id = this.toReference(id);
            return this._byId[id];
        }
    },

    addOrUpdate: function(model, options) {
        options || (options = {});

        var collection = this;
        var existing;
        if (_.isArray(model)) {
            var entities = [];
            _.each(model, function(item) {
                entities.push(collection.addOrUpdate(item, options));
            });
            return entities;
        }

        if (!model.isEntity) {
            model = new this.model(model);
        }

        if (model.id && this.get(model.id)) {
            existing = this.get(model.id);
        }
        if (this.getByCid(model.cid)) {
            var existing = this.getByCid(model.cid);
        }
        if (existing) {
            var newAttribs = {};
            _.each(model.attributes, function(value, attribute) {
                if (!existing.has(attribute)) {
                    newAttribs[attribute] = value;
                    return true;
                }
                else if (existing.get(attribute) === value) {
                    return true;
                } else {
                    //merge existing attribute values with new ones!
                    //not just overwrite 'em!!
                    var oldVals = existing.attributes[attribute];
                    var newVals = value;
                    if (oldVals instanceof collection.vie.Collection) {
                        // TODO: Merge collections
                        return true;
                    }
                    
                    if (attribute === '@context') {
                        newAttribs[attribute] = jQuery.extend(true, {}, oldVals, newVals);
                    } else {
                        oldVals = (jQuery.isArray(oldVals))? oldVals : [ oldVals ];
                        newVals = (jQuery.isArray(newVals))? newVals : [ newVals ];
                        newAttribs[attribute] = _.uniq(oldVals.concat(newVals));
                        newAttribs[attribute] = (newAttribs[attribute].length === 1)? newAttribs[attribute][0] : newAttribs[attribute];
                    }
                }
            });

            if (!_.isEmpty(newAttribs)) {
                existing.set(newAttribs, options.updateOptions);
            }
            return existing;
        }
        this.add(model, options.addOptions);
        return model;
    },

    isReference: function(uri){
        var matcher = new RegExp("^\\<([^\\>]*)\\>$");
        if (matcher.exec(uri)) {
            return true;
        }
        return false;
    },
        
    toReference: function(uri){
        if (this.isReference(uri)) {
            return uri;
        }
        return '<' + uri + '>';
    },
        
    fromReference: function(uri){
        if (!this.isReference(uri)) {
            return uri;
        }
        return uri.substring(1, uri.length - 1);
    },
    
    isCollection: true
});
//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/
//

// ## VIE.Types
// Within VIE, we provide special capabilities of handling types of entites. This helps
// for example to query easily for certain entities (e.g., you only need to query for *Person*s 
// and not for all subtypes).
if (VIE.prototype.Type) {
	throw new Error("ERROR: VIE.Type is already defined. Please check your installation!");
}
if (VIE.prototype.Types) {
	throw new Error("ERROR: VIE.Types is already defined. Please check your installation!");
}

// ### VIE.Type(id, attrs)
// This is the constructor of a VIE.Type.  
// **Parameters**:  
// *{string}* **id** The id of the type.  
// *{string|array|VIE.Attribute}* **attrs** A string, proper ```VIE.Attribute``` or an array of these which 
// are the possible attributes of the type  
// **Throws**:  
// *{Error}* if one of the given paramenters is missing.  
// **Returns**:  
// *{VIE.Type}* : A **new** VIE.Type object.  
// **Example usage**:  
//
//     var person = new vie.Type("Person", ["name", "knows"]);
VIE.prototype.Type = function (id, attrs) {
    if (id === undefined || typeof id !== 'string') {
        throw "The type constructor needs an 'id' of type string! E.g., 'Person'";
    }

// ### id
// This field stores the id of the type's instance.  
// **Parameters**:  
// nothing
// **Throws**:  
// nothing  
// **Returns**:  
// *{string}* : The id of the type as a URI.  
// **Example usage**:  
//
//     console.log(person.id);
//      // --> "<http://viejs.org/ns/Person>"
    this.id = this.vie.namespaces.isUri(id) ? id : this.vie.namespaces.uri(id);

    /* checks whether such a type is already defined. */
    if (this.vie.types.get(this.id)) {
        throw new Error("The type " + this.id + " is already defined!");
    }    
    
// ### supertypes
// This field stores all parent types of the type's instance. This
// is set if the current type inherits from another type.   
// **Parameters**:  
// nothing  
// **Throws**:  
// nothing  
// **Returns**:  
// *{VIE.Types}* : The supertypes (parents) of the type.  
// **Example usage**:  
//
//     console.log(person.supertypes);
    this.supertypes = new this.vie.Types();

// ### subtypes
// This field stores all children types of the type's instance. This
// will be set if another type inherits from the current type.  
// **Parameters**:  
// nothing  
// **Throws**:  
// nothing  
// **Returns**:  
// *{VIE.Types}* : The subtypes (parents) of the type.  
// **Example usage**:  
//
//     console.log(person.subtypes);
    this.subtypes = new this.vie.Types();
    
// ### attributes
// This field stores all attributes of the type's instance as
// a proper ```VIE.Attributes``` class. (see also <a href="Attribute.html">VIE.Attributes</a>)  
// **Parameters**:  
// nothing  
// **Throws**:  
// nothing  
// **Returns**:  
// *{VIE.Attributes}* : The attributes of the type.  
// **Example usage**:  
//
//     console.log(person.attributes);
    this.attributes = new this.vie.Attributes(this, (attrs)? attrs : []);

// ### isof(type)
// This method checks whether the current type is a child of the given type.  
// **Parameters**:  
// *{string|VIE.Type}* **type** The type (or the id of that type) to be checked.  
// **Throws**:  
// *{Error}* If the type is not valid.   
// **Returns**:  
// *{boolean}* : ```true``` if the current type inherits from the type, ```false``` otherwise.  
// **Example usage**:  
//
//     console.log(person.isof("owl:Thing"));
//     // <-- true    
    this.isof = function (type) {
        type = this.vie.types.get(type);
        if (type) {
            return type.subsumes(this.id);
        } else {
            throw new Error("No valid type given");
        }
    };

// ### subsumes(type)
// This method checks whether the current type is a parent of the given type.  
// **Parameters**:  
// *{string|VIE.Type}* **type** The type (or the id of that type) to be checked.  
// **Throws**:  
// *{Error}* If the type is not valid.   
// **Returns**:  
// *{boolean}* : ```true``` if the current type is a parent of the type, ```false``` otherwise.  
// **Example usage**:  
//
//     var x = new vie.Type(...);
//     var y = new vie.Type(...).inherit(x);
//     y.isof(x) === x.subsumes(y);
    this.subsumes = function (type) {
        type = this.vie.types.get(type);
        if (type) {
            if (this.id === type.id) {
                return true;
            }
            var subtypes = this.subtypes.list();
            for (var c = 0; c < subtypes.length; c++) {
                var childObj = subtypes[c];
                if (childObj) {
                     if (childObj.id === type.id || childObj.subsumes(type)) {
                         return true;
                     }
                }
            }
            return false;
        } else {
            throw new Error("No valid type given");
        }
    };
    
// ### inherit(supertype)
// This method invokes inheritance throught the types. This adds the current type to the
// subtypes of the supertype and vice versa.   
// **Parameters**:  
// *{string|VIE.Type|array}* **supertype** The type to be inherited from. If this is an array
// the inherit method is called sequentially on all types.  
// **Throws**:  
// *{Error}* If the type is not valid.   
// **Returns**:  
// *{VIE.Type}* : The instance itself.  
// **Example usage**:  
//
//     var x = new vie.Type(...);
//     var y = new vie.Type(...).inherit(x);
//     y.isof(x) // <-- true
    this.inherit = function (supertype) {
        if (typeof supertype === "string") {
            this.inherit(this.vie.types.get(supertype));
        }
        else if (supertype instanceof this.vie.Type) {
            supertype.subtypes.addOrOverwrite(this);
            this.supertypes.addOrOverwrite(supertype);
            try {
                /* only for validation of attribute-inheritance!
                   if this throws an error (inheriting two attributes
                   that cannot be combined) we reverse all changes. */
                this.attributes.list();
            } catch (e) {
                supertype.subtypes.remove(this);
                this.supertypes.remove(supertype);
                throw e;
            }
        } else if (jQuery.isArray(supertype)) {
            for (var i = 0, slen = supertype.length; i < slen; i++) {
                this.inherit(supertype[i]);
            }
        } else {
            throw new Error("Wrong argument in VIE.Type.inherit()");
        }
        return this;
    };
        
// ### hierarchy()
// This method serializes the hierarchy of child types into an object.   
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing*   
// **Returns**:  
// *{object}* : The hierachy of child types as an object.  
// **Example usage**:  
//
//     var x = new vie.Type(...);
//     var y = new vie.Type(...).inherit(x);
//     x.hierarchy();
    this.hierarchy = function () {
        var obj = {id : this.id, subtypes: []};
        var list = this.subtypes.list();
        for (var c = 0, llen = list.length; c < llen; c++) {
            var childObj = this.vie.types.get(list[c]);
            obj.subtypes.push(childObj.hierarchy());
        }
        return obj;
    };
    
// ### instance()
// This method creates a ```VIE.Entity``` instance from this type.
// **Parameters**:  
// *{object}* **attrs**  see <a href="Entity.html">constructor of VIE.Entity</a>  
// *{object}* **opts**  see <a href="Entity.html">constructor of VIE.Entity</a>  
// **Throws**:  
// *{Error}* if the instance could not be built   
// **Returns**:  
// *{VIE.Entity}* : A **new** instance of a ```VIE.Entity``` with the current type.  
// **Example usage**:  
//
//     var person = new vie.Type("person");
//     var sebastian = person.instance(
//         {"@subject" : "#me", 
//          "name" : "Sebastian"});
//     console.log(sebastian.get("name")); // <-- "Sebastian"
    this.instance = function (attrs, opts) {
        attrs = (attrs)? attrs : {};
        opts = (opts)? opts : {};
        
        /* turn type/attribute checking on by default! */
        if (opts.typeChecking !== false) {
            for (var a in attrs) {
                if (a.indexOf('@') !== 0 && !this.attributes.get(a)) {
                    throw new Error("Cannot create an instance of " + this.id + " as the type does not allow an attribute '" + a + "'!");
                }
            }
        }
        
        if (attrs['@type']) {
            attrs['@type'].push(this.id);
        } else {
            attrs['@type'] = this.id;
        }
        
        return new this.vie.Entity(attrs, opts);
    };

// ### toString()
// This method returns the id of the type.   
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing*   
// **Returns**:  
// *{string}* : The id of the type.  
// **Example usage**:  
//
//     var x = new vie.Type(...);
//     x.toString() === x.id;
    this.toString = function () {
        return this.id;
    };
};

// ### VIE.Types()
// This is the constructor of a VIE.Types. This is a convenience class
// to store ```VIE.Type``` instances properly.  
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Types}* : A **new** VIE.Types object.  
// **Example usage**:  
//
//     var types = new vie.Types();
VIE.prototype.Types = function () {
        
    this._types = {};
    
// ### add(id, attrs)
// This method adds a `VIE.Type` to the types.  
// **Parameters**:  
// *{string|VIE.Type}* **id** If this is a string, the type is created and directly added.  
// *{string|object}* **attrs** Only used if ```id``` is a string.   
// **Throws**:  
// *{Error}* if a type with the given id already exists a ```VIE.Entity``` instance from this type.  
// **Returns**:  
// *{VIE.Types}* : The instance itself.  
// **Example usage**:  
//
//     var types = new vie.Types();
//     types.add("Person", ["name", "knows"]);
    this.add = function (id, attrs) {
        if (this.get(id)) {
            throw new Error("Type '" + id + "' already registered.");
        } 
        else {
            if (typeof id === "string") {
                var t = new this.vie.Type(id, attrs);
                this._types[t.id] = t;
                return t;
            } else if (id instanceof this.vie.Type) {
            	this._types[id.id] = id;
                return id;
            } else {
                throw new Error("Wrong argument to VIE.Types.add()!");
            }
        }
        return this;
    };
    
// ### addOrOverwrite(id, attrs)
// This method adds or overwrites a `VIE.Type` to the types. This is the same as 
// ``this.remove(id); this.add(id, attrs);``  
// **Parameters**:  
// *{string|VIE.Type}* **id** If this is a string, the type is created and directly added.  
// *{string|object}* **attrs** Only used if ```id``` is a string.   
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Types}* : The instance itself.  
// **Example usage**:  
//
//     var types = new vie.Types();
//     types.addOrOverwrite("Person", ["name", "knows"]);
    this.addOrOverwrite = function(id, attrs){
        if (this.get(id)) {
            this.remove(id);
        }
        return this.add(id, attrs);
    };
    
// ### get(id)
// This method retrieves a `VIE.Type` from the types by it's id.  
// **Parameters**:  
// *{string|VIE.Type}* **id** The id or the type itself.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Type}* : The instance of the type or ```undefined```.  
// **Example usage**:  
//
//     var types = new vie.Types();
//     types.addOrOverwrite("Person", ["name", "knows"]);
//     types.get("Person");
    this.get = function (id) {
        if (!id) {
            return undefined;
        }
        if (typeof id === 'string') {
            var lid = this.vie.namespaces.isUri(id) ? id : this.vie.namespaces.uri(id);
            return this._types[lid];
        } else if (id instanceof this.vie.Type) {
            return this.get(id.id);
        }
        return undefined;
    };
    
// ### remove(id)
// This method removes a type of given id from the type. This also
// removes all children if their only parent were this
// type. Furthermore, this removes the link from the
// super- and subtypes.   
// **Parameters**:  
// *{string|VIE.Type}* **id** The id or the type itself.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Type}* : The removed type.  
// **Example usage**:  
//
//     var types = new vie.Types();
//     types.addOrOverwrite("Person", ["name", "knows"]);
//     types.remove("Person");
    this.remove = function (id) {
        var t = this.get(id);
        /* test whether the type actually exists in VIE
         * and prevents removing *owl:Thing*.
         */
        if (!t) {
            return this;
        }
        if (!t || t.subsumes("owl:Thing")) {
            console.warn("You are not allowed to remove 'owl:Thing'.");
            return this;
        }
        delete this._types[t.id];
        
        var subtypes = t.subtypes.list();
        for (var c = 0; c < subtypes.length; c++) {
            var childObj = subtypes[c];
            if (childObj.supertypes.list().length === 1) {
                /* recursively remove all children 
                   that inherit only from this type */
                this.remove(childObj);
            } else {
                childObj.supertypes.remove(t.id);
            }
        }
        return t;
    };
    
// ### toArray() === list()
// This method returns an array of all types.  
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{array}* : An array of ```VIE.Type``` instances.  
// **Example usage**:  
//
//     var types = new vie.Types();
//     types.addOrOverwrite("Person", ["name", "knows"]);
//     types.list();
    this.toArray = this.list = function () {
        var ret = [];
        for (var i in this._types) {
            ret.push(this._types[i]);
        }
        return ret;
    };

// ### sort(types, desc)
// This method sorts an array of types in their order, given by the
// inheritance. This returns a copy and leaves the original array untouched.  
// **Parameters**:  
// *{array|VIE.Type}* **types** The array of ```VIE.Type``` instances or ids of types to be sorted.  
// *{boolean}* **desc** If 'desc' is given and 'true', the array will be sorted 
// in descendant order.  
// *nothing*  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{array}* : A sorted copy of the array.  
// **Example usage**:  
//
//     var types = new vie.Types();
//     types.addOrOverwrite("Person", ["name", "knows"]);
//     types.sort(types.list(), true);
    this.sort = function (types, desc) {
        var self = this;
        types = (jQuery.isArray(types))? types : [ types ];
        desc = (desc)? true : false;
        
        if (types.length === 0) return [];
        var copy = [ types[0] ];
        
        
        for (var x = 1, tlen = types.length; x < tlen; x++) {
            var insert = types[x];
            var insType = self.get(insert);
            if (insType) {
                for (var y = 0; y < copy.length; y++) {
                    if (insType.subsumes(copy[y])) {
                        copy.splice(y,0,insert);
                        break;
                    } else if (y === copy.length - 1) {
                        copy.push(insert);
                    }
                }
            }
        }
        
        if (!desc) {
            copy.reverse();
        }
        return copy;
    };
};
//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/
//

// ## VIE.Attributes
// Within VIE, we provide special capabilities of handling attributes of types of entites. This
// helps first of all to list all attributes of an entity type, but furthermore fully supports
// inheritance of attributes from the type-class to inherit from.
if (VIE.prototype.Attribute) {
	throw new Error("ERROR: VIE.Attribute is already defined. Please check your VIE installation!");
}
if (VIE.prototype.Attributes) {
	throw new Error("ERROR: VIE.Attributes is already defined. Please check your VIE installation!");
}

// ### VIE.Attribute(id, range, domain)
// This is the constructor of a VIE.Attribute.  
// **Parameters**:  
// *{string}* **id** The id of the attribute.  
// *{string|array}* **range** A string or an array of strings of the target range of 
// the attribute.  
// *{string}* **domain** The domain of the attribute.  
// *{number}* **minCount** The minimal number this attribute can occur. (needs to be >= 0)  
// *{number}* **maxCount** The maximal number this attribute can occur. (needs to be >= minCount)  
// **Throws**:  
// *{Error}* if one of the given paramenters is missing.  
// **Returns**:  
// *{VIE.Attribute}* : A **new** VIE.Attribute object.  
// **Example usage**:  
//
//     var knowsAttr = new vie.Attribute("knows", ["Person"], "Person", 0, 10);
//      // Creates an attribute to describe a *knows*-relationship
//      // between persons. Each person can only have 
VIE.prototype.Attribute = function (id, range, domain, minCount, maxCount) {
    if (id === undefined || typeof id !== 'string') {
        throw new Error("The attribute constructor needs an 'id' of type string! E.g., 'Person'");
    }
    if (range === undefined) {
        throw new Error("The attribute constructor needs 'range'.");
    }
    if (domain === undefined) {
        throw new Error("The attribute constructor needs a 'domain'.");
    }
    
    this._domain = domain;
    
// ### id
// This field stores the id of the attribute's instance.  
// **Parameters**:  
// nothing
// **Throws**:  
// nothing  
// **Returns**:  
// *{string}* : A URI, representing the id of the attribute.  
// **Example usage**:  
//
//     var knowsAttr = new vie.Attribute("knows", ["Person"], "Person");
//     console.log(knowsAttr.id);
//     // --> <http://viejs.org/ns/knows>
    this.id = this.vie.namespaces.isUri(id) ? id : this.vie.namespaces.uri(id);
    
// ### range
// This field stores the ranges of the attribute's instance.  
// **Parameters**:  
// nothing
// **Throws**:  
// nothing  
// **Returns**:  
// *{array}* : An array of strings which represent the types.  
// **Example usage**:  
//
//     var knowsAttr = new vie.Attribute("knows", ["Person"], "Person");
//     console.log(knowsAttr.range);
//      // --> ["Person"]
    this.range = (_.isArray(range))? range : [ range ];
    
// ### min
// This field stores the minimal amount this attribute can occur in the type's instance. The number
// needs to be greater or equal to zero.  
// **Parameters**:  
// nothing
// **Throws**:  
// nothing  
// **Returns**:  
// *{int}* : The minimal amount this attribute can occur.  
// **Example usage**:  
//
//     console.log(person.min);
//      // --> 0
    minCount = minCount ? minCount : 0;
    this.min = (minCount > 0) ? minCount : 0;
    
// ### max
// This field stores the maximal amount this attribute can occur in the type's instance.
// This number cannot be smaller than min  
// **Parameters**:  
// nothing
// **Throws**:  
// nothing  
// **Returns**:  
// *{int}* : The maximal amount this attribute can occur.  
// **Example usage**:  
//
//     console.log(person.max);
//      // --> 1.7976931348623157e+308
    maxCount = maxCount ? maxCount : Number.MAX_VALUE;
    this.max = (maxCount >= this.min)? maxCount : this.min;
// ### applies(range)
// This method checks, whether the current attribute applies in the given range.
// If ```range``` is a string and cannot be transformed into a ```VIE.Type```, 
// this performs only string comparison, if it is a VIE.Type 
// or an ID of a VIE.Type, then inheritance is checked as well.
// **Parameters**:  
// *{string|VIE.Type}* **range** The ```VIE.Type``` (or it's string representation) to be checked. 
// **Throws**:  
// nothing  
// **Returns**:  
// *{boolean}* : ```true``` if the given type applies to this attribute and ```false``` otherwise.  
// **Example usage**:  
//
//     var knowsAttr = new vie.Attribute("knows", ["Person"], "Person");
//     console.log(knowsAttr.applies("Person")); // --> true
//     console.log(knowsAttr.applies("Place")); // --> false
    this.applies = function (range) {
        if (this.vie.types.get(range)) {
            range = this.vie.types.get(range);
        }
        for (var r = 0, len = this.range.length; r < len; r++) {
            var x = this.vie.types.get(this.range[r]);
            if (x === undefined && typeof range === "string") {
                if (range === this.range[r]) {
                    return true;
                }
            }
            else {
                if (range.isof(this.range[r])) {
                    return true;
                }
            }
        }
        return false;
    };
            
};

// ## VIE.Attributes(domain, attrs)
// This is the constructor of a VIE.Attributes. Basically a convenience class
// that represents a list of ```VIE.Attribute```. As attributes are part of a 
// certain ```VIE.Type```, it needs to be passed for inheritance checks.  
// **Parameters**:  
// *{string}* **domain** The domain of the attributes (the type they will be part of).  
// *{string|VIE.Attribute|array}* **attrs** Either a string representation of an attribute,
// a proper instance of ```VIE.Attribute``` or an array of both.  
// *{string}* **domain** The domain of the attribute.  
// **Throws**:  
// *{Error}* if one of the given paramenters is missing.  
// **Returns**:  
// *{VIE.Attribute}* : A **new** VIE.Attribute instance.  
// **Example usage**:  
//
//     var knowsAttr = new vie.Attribute("knows", ["Person"], "Person");
//     var personAttrs = new vie.Attributes("Person", knowsAttr);
VIE.prototype.Attributes = function (domain, attrs) {
    
    this._local = {};
    this._attributes = {};
    
// ### domain
// This field stores the domain of the attributes' instance.  
// **Parameters**:  
// nothing
// **Throws**:  
// nothing  
// **Returns**:  
// *{string}* : The string representation of the domain.  
// **Example usage**:  
//
//     console.log(personAttrs.domain);
//     // --> ["Person"]
    this.domain = domain;
    
// ### add(id, range)
// This method adds a ```VIE.Attribute``` to the attributes instance.
// **Parameters**:  
// *{string|VIE.Attribute}* **id** The string representation of an attribute, or a proper
// instance of a ```VIE.Attribute```.  
// *{string|array}* **range** An array representing the target range of the attribute.  
// *{number}* **mmin** The minimal amount this attribute can appear.  
// instance of a ```VIE.Attribute```.  
// *{number}* **max** The maximal amount this attribute can appear.  
// **Throws**:  
// *{Error}* If an atribute with the given id is already registered.  
// *{Error}* If the ```id``` parameter is not a string, nor a ```VIE.Type``` instance.  
// **Returns**:  
// *{VIE.Attribute}* : The generated or passed attribute.  
// **Example usage**:  
//
//     personAttrs.add("name", "Text", 0, 1);
    this.add = function (id, range, min, max) {
        if (this.get(id)) {
            throw new Error("Attribute '" + id + "' already registered for domain " + this.domain.id + "!");
        } 
        else {
            if (typeof id === "string") {
                var a = new this.vie.Attribute(id, range, this.domain, min, max);
                this._local[a.id] = a;
                return a;
            } else if (id instanceof this.vie.Attribute) {
                id.domain = this.domain;
                id.vie = this.vie;
                this._local[id.id] = id;
                return id;
            } else {
                throw new Error("Wrong argument to VIE.Types.add()!");
            }
        }
    };
    
// ### remove(id)
// This method removes a ```VIE.Attribute``` from the attributes instance.
// **Parameters**:  
// *{string|VIE.Attribute}* **id** The string representation of an attribute, or a proper
// instance of a ```VIE.Attribute```.  
// **Throws**:  
// *{Error}* When the attribute is inherited from a parent ```VIE.Type``` and thus cannot be removed.
// **Returns**:  
// *{VIE.Attribute}* : The removed attribute.  
// **Example usage**:  
//
//     personAttrs.remove("knows");
    this.remove = function (id) {
        var a = this.get(id);
        if (a.id in this._local) {
            delete this._local[a.id];
            return a;
        }
        throw new Error("The attribute " + id + " is inherited and cannot be removed from the domain " + this.domain.id + "!");
    };
    
// ### get(id)
// This method returns a ```VIE.Attribute``` from the attributes instance by it's id.  
// **Parameters**:  
// *{string|VIE.Attribute}* **id** The string representation of an attribute, or a proper
// instance of a ```VIE.Attribute```.  
// **Throws**:  
// *{Error}* When the method is called with an unknown datatype.  
// **Returns**:  
// *{VIE.Attribute}* : The attribute.  
// **Example usage**:  
//
//     personAttrs.get("knows");
    this.get = function (id) {
        if (typeof id === 'string') {
            var lid = this.vie.namespaces.isUri(id) ? id : this.vie.namespaces.uri(id);
            return this._inherit()._attributes[lid];
        } else if (id instanceof this.vie.Attribute) {
            return this.get(id.id);
        } else {
            throw new Error("Wrong argument in VIE.Attributes.get()");
        }
    };
    
// ### _inherit()
// The private method ```_inherit``` creates a full list of all attributes. This includes
// local attributes as well as inherited attributes from the parents. The ranges of attributes
// with the same id will be merged. This method is called everytime an attribute is requested or
// the list of all attributes. Usually this method should not be invoked outside of the class.  
// **Parameters**:  
// *nothing*  
// instance of a ```VIE.Attribute```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *nothing*  
// **Example usage**:  
//
//     personAttrs._inherit();
    this._inherit = function () {
        var attributes = jQuery.extend(true, {}, this._local);
        
        var inherited = _.map(this.domain.supertypes.list(),
            function (x) {
               return x.attributes; 
            }
        );

        var add = {};
        var merge = {};
        
        for (var a = 0, ilen = inherited.length; a < ilen; a++) {
            var attrs = inherited[a].list();
            for (var x = 0, alen = attrs.length; x < alen; x++) {
                var id = attrs[x].id;
                if (!(id in attributes)) {
                    if (!(id in add) && !(id in merge)) {
                        add[id] = attrs[x];
                    }
                    else {
                        if (!merge[id]) {
                            merge[id] = {range : [], mins : [], maxs: []};
                        }
                        if (id in add) {
                            merge[id]["range"] = jQuery.merge(merge[id]["range"], add[id].range);
                            merge[id]["mins"] = jQuery.merge(merge[id]["mins"], [ add[id].min ]);
                            merge[id]["maxs"] = jQuery.merge(merge[id]["maxs"], [ add[id].max ]);
                            delete add[id];
                        }
                        merge[id]["range"] = jQuery.merge(merge[id]["range"], attrs[x].range);
                        merge[id]["mins"] = jQuery.merge(merge[id]["mins"], [ attrs[x].min ]);
                        merge[id]["maxs"] = jQuery.merge(merge[id]["maxs"], [ attrs[x].max ]);
                        merge[id]["range"] = _.uniq(merge[id]["range"]);
                        merge[id]["mins"] = _.uniq(merge[id]["mins"]);
                        merge[id]["maxs"] = _.uniq(merge[id]["maxs"]);
                    }
                }
            }
        }
        
        /* adds inherited attributes that do not need to be merged */
        jQuery.extend(attributes, add);
        
        /* merges inherited attributes */
        for (var id in merge) {
            var mranges = merge[id]["range"];
            var mins = merge[id]["mins"];
            var maxs = merge[id]["maxs"];
            var ranges = [];
            //merging ranges
            for (var r = 0, mlen = mranges.length; r < mlen; r++) {
                var p = this.vie.types.get(mranges[r]);
                var isAncestorOf = false;
                if (p) {
                    for (var x = 0; x < mlen; x++) {
                        if (x === r) {
                            continue;
                        }
                        var c = this.vie.types.get(mranges[x]);
                        if (c && c.isof(p)) {
                            isAncestorOf = true;
                            break;
                        }
                    }
                }
                if (!isAncestorOf) {
                    ranges.push(mranges[r]);
                }
            }
            
            var maxMin = _.max(mins);
            var minMax = _.min(maxs);
            
            if (maxMin <= minMax && minMax >= 0 && maxMin >= 0) {
                attributes[id] = new this.vie.Attribute(id, ranges, this, maxMin, minMax);
            } else {
                throw new Error("This inheritance is not allowed because of an invalid minCount/maxCount pair!");
            }
        }

        this._attributes = attributes;
        return this;
    };

// ### toArray() === list()
// This method return an array of ```VIE.Attribute```s from the attributes instance.  
// **Parameters**:  
// *nothing.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{array}* : An array of ```VIE.Attribute```.  
// **Example usage**:  
//
//     personAttrs.list();
    this.toArray = this.list = function (range) {
        var ret = [];
        var attributes = this._inherit()._attributes;
        for (var a in attributes) {
            if (!range || attributes[a].applies(range)) {
                ret.push(attributes[a]);
            }
        }
        return ret;
    };
        
    attrs = _.isArray(attrs) ? attrs : [ attrs ];
    
    for (var a = 0, len = attrs.length; a < len; a++) {
        this.add(attrs[a].id, attrs[a].range, attrs[a].min, attrs[a].max);
    }
};
//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/
if (VIE.prototype.Namespaces) {
    throw new Error("ERROR: VIE.Namespaces is already defined. " + 
        "Please check your VIE installation!");
}

// ## VIE Namespaces
//
// In general, a namespace is a container that provides context for the identifiers.
// Within VIE, namespaces are used to distinguish different ontolgies or vocabularies
// of identifiers, types and attributes. However, because of their verbosity, namespaces
// tend to make their usage pretty circuitous. The ``VIE.Namespaces(...)`` class provides VIE
// with methods to maintain abbreviations (akak **prefixes**) for namespaces in order to
// alleviate their usage. By default, every VIE instance is equipped with a main instance
// of the namespaces in ``myVIE.namespaces``. Furthermore, VIE uses a **base namespace**, 
// which is used if no prefix is given (has an empty prefix).
// In the upcoming sections, we will explain the
// methods to add, access and remove prefixes.



// ## VIE.Namespaces(base, namespaces)
// This is the constructor of a VIE.Namespaces. The constructor initially 
// needs a *base namespace* and can optionally be initialised with an 
// associative array of prefixes and namespaces. The base namespace is used in a way
// that every non-prefixed, non-expanded attribute or type is assumed to be of that 
// namespace. This helps, e.g., in an environment where only one namespace is given.  
// **Parameters**:  
// *{string}* **base** The base namespace.  
// *{object}* **namespaces** Initial namespaces to bootstrap the namespaces. (optional)  
// **Throws**:  
// *{Error}* if the base namespace is missing.  
// **Returns**:  
// *{VIE.Attribute}* : A **new** VIE.Attribute object.  
// **Example usage**:  
//
//     var ns = new myVIE.Namespaces("http://viejs.org/ns/", 
//           {
//            "foaf": "http://xmlns.com/foaf/0.1/"
//           });
VIE.prototype.Namespaces = function (base, namespaces) {
    
    if (!base) {
        throw new Error("Please provide a base namespace!");
    }
    this._base = base;
    
    this._namespaces = (namespaces)? namespaces : {};
    if (typeof this._namespaces !== "object" || _.isArray(this._namespaces)) {
        throw new Error("If you want to initialise VIE namespace prefixes, " + 
            "please provide a proper object!");
    }
};


// ### base(ns)
// This is a **getter** and **setter** for the base
// namespace. If called like ``base();`` it
// returns the actual base namespace as a string. If provided
// with a string, e.g., ``base("http://viejs.org/ns/");``
// it sets the current base namespace and retuns the namespace object
// for the purpose of chaining. If provided with anything except a string,
// it throws an Error.  
// **Parameters**:  
// *{string}* **ns** The namespace to be set. (optional)  
// **Throws**:  
// *{Error}* if the namespace is not of type string.  
// **Returns**:  
// *{string}* : The current base namespace.  
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     console.log(namespaces.base()); // <-- "http://base.ns/"
//     namespaces.base("http://viejs.org/ns/");
//     console.log(namespaces.base()); // <-- "http://viejs.org/ns/"
VIE.prototype.Namespaces.prototype.base = function (ns) {
    if (!ns) { 
        return this._base;
    }
    else if (typeof ns === "string") {
        /* remove another mapping */
        this.removeNamespace(ns);
        this._base = ns;
        return this._base;
    } else {
        throw new Error("Please provide a valid namespace!");
    }
};

// ### add(prefix, namespace)
// This method adds new prefix mappings to the
// current instance. If a prefix or a namespace is already
// present (in order to avoid ambiguities), an Error is thrown. 
// ``prefix`` can also be an object in which case, the method 
// is called sequentially on all elements.  
// **Parameters**:  
// *{string|object}* **prefix** The prefix to be set. If it is an object, the
// method will be applied to all key,value pairs sequentially.  
// *{string}* **namespace** The namespace to be set.  
// **Throws**:  
// *{Error}* If a prefix or a namespace is already
// present (in order to avoid ambiguities).  
// **Returns**:  
// *{VIE.Namespaces}* : The current namespaces instance.  
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.add("", "http://...");
//     // is always equal to
//     namespaces.base("http://..."); // <-- setter of base namespace
VIE.prototype.Namespaces.prototype.add = function (prefix, namespace) {
    if (typeof prefix === "object") {
        for (var k1 in prefix) {
            this.add(k1, prefix[k1]);
        }
        return this;
    }
    if (prefix === "") {
        this.base(namespace);
        return this;
    }
    /* checking if we overwrite existing mappings */
    else if (this.contains(prefix) && namespace !== this._namespaces[prefix]) {
        throw new Error("ERROR: Trying to register namespace prefix mapping (" + prefix + "," + namespace + ")!" +
              "There is already a mapping existing: '(" + prefix + "," + this.get(prefix) + ")'!");
    } else {
        jQuery.each(this._namespaces, function (k1,v1) {
            if (v1 === namespace && k1 !== prefix) {
                throw new Error("ERROR: Trying to register namespace prefix mapping (" + prefix + "," + namespace + ")!" +
                      "There is already a mapping existing: '(" + k1 + "," + namespace + ")'!");
            }
        });
    }
    /* if not, just add them */
    this._namespaces[prefix] = namespace;
    return this;
};
    
// ### addOrReplace(prefix, namespace)
// This method adds new prefix mappings to the
// current instance. This will overwrite existing mappings.  
// **Parameters**:  
// *{string|object}* **prefix** The prefix to be set. If it is an object, the
// method will be applied to all key,value pairs sequentially.  
// *{string}* **namespace** The namespace to be set.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Namespaces}* : The current namespaces instance.  
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.addOrReplace("", "http://...");
//     // is always equal to
//     namespaces.base("http://..."); // <-- setter of base namespace
VIE.prototype.Namespaces.prototype.addOrReplace = function (prefix, namespace) {
    if (typeof prefix === "object") {
        for (var k1 in prefix) {
            this.addOrReplace(k1, prefix[k1]);
        }
        return this;
    }
    this.remove(prefix);
    this.removeNamespace(namespace);
    return this.add(prefix, namespace);
};

// ### get(prefix)
// This method retrieves a namespaces, given a prefix. If the
// prefix is the empty string, the base namespace is returned.  
// **Parameters**:  
// *{string}* **prefix** The prefix to be retrieved.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{string|undefined}* : The namespace or ```undefined``` if no namespace could be found.  
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.addOrReplace("test", "http://test.ns");
//     console.log(namespaces.get("test")); // <-- "http://test.ns"
VIE.prototype.Namespaces.prototype.get = function (prefix) {
    if (prefix === "") {
        return this.base();
    }
    return this._namespaces[prefix];
};

// ### getPrefix(namespace)
// This method retrieves a prefix, given a namespace.  
// **Parameters**:  
// *{string}* **namespace** The namespace to be retrieved.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{string|undefined}* : The prefix or ```undefined``` if no prefix could be found.  
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.addOrReplace("test", "http://test.ns");
//     console.log(namespaces.getPrefix("http://test.ns")); // <-- "test"
VIE.prototype.Namespaces.prototype.getPrefix = function (namespace) {
    var prefix = undefined;
    jQuery.each(this._namespaces, function (k1,v1) {
        if (v1 === namespace) {
            prefix = k1;
        }
    });
    return prefix;
};

// ### contains(prefix)
// This method checks, whether a prefix is stored in the instance.  
// **Parameters**:  
// *{string}* **prefix** The prefix to be checked.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{boolean}* : ```true``` if the prefix could be found, ```false``` otherwise.  
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.addOrReplace("test", "http://test.ns");
//     console.log(namespaces.contains("test")); // <-- true
VIE.prototype.Namespaces.prototype.contains = function (prefix) {
    return (prefix in this._namespaces);
};

// ### containsNamespace(namespace)
// This method checks, whether a namespace is stored in the instance.  
// **Parameters**:  
// *{string}* **namespace** The namespace to be checked.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{boolean}* : ```true``` if the namespace could be found, ```false``` otherwise.  
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.addOrReplace("test", "http://test.ns");
//     console.log(namespaces.containsNamespace("http://test.ns")); // <-- true
VIE.prototype.Namespaces.prototype.containsNamespace = function (namespace) {
    return this.getPrefix(namespace) !== undefined;
};

// ### update(prefix, namespace)
// This method overwrites the namespace that is stored under the 
// prefix ``prefix`` with the new namespace ``namespace``. 
// If a namespace is already bound to another prefix, an Error is thrown.
// **Parameters**:  
// *{string}* **prefix** The prefix.  
// *{string}* **namespace** The namespace.  
// **Throws**:  
// *{Error}* If a namespace is already bound to another prefix.  
// **Returns**:  
// *{VIE.Namespaces}* : The namespace instance.  
// **Example usage**:  
//
//     ...
VIE.prototype.Namespaces.prototype.update = function (prefix, namespace) {
    this.remove(prefix);
    return this.add(prefix, namespace);
};

// ### updateNamespace(prefix, namespace)
// This method overwrites the prefix that is bound to the 
// namespace ``namespace`` with the new prefix ``prefix``. If another namespace is
// already registered with the given ``prefix``, an Error is thrown.  
// **Parameters**:  
// *{string}* **prefix** The prefix.  
// *{string}* **namespace** The namespace.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Namespaces}* : The namespace instance.  
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.add("test", "http://test.ns");
//     namespaces.updateNamespace("test2", "http://test.ns");
//     namespaces.get("test2"); // <-- "http://test.ns"
VIE.prototype.Namespaces.prototype.updateNamespace = function (prefix, namespace) {
    this.removeNamespace(prefix);
    return this.add(prefix, namespace);
};

// ### remove(prefix)
// This method removes the namespace that is stored under the prefix ``prefix``.  
// **Parameters**:  
// *{string}* **prefix** The prefix to be removed.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Namespaces}* : The namespace instance.   
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.add("test", "http://test.ns");
//     namespaces.get("test"); // <-- "http://test.ns"
//     namespaces.remove("test");
//     namespaces.get("test"); // <-- undefined
VIE.prototype.Namespaces.prototype.remove = function (prefix) {
    if (prefix) {
        delete this._namespaces[prefix];
    }
    return this;
};

// ### removeNamespace(namespace)
// This method removes removes the namespace ``namespace`` from the instance.  
// **Parameters**:  
// *{string}* **namespace** The namespace to be removed.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.Namespaces}* : The namespace instance.   
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.add("test", "http://test.ns");
//     namespaces.get("test"); // <-- "http://test.ns"
//     namespaces.removeNamespace("http://test.ns");
//     namespaces.get("test"); // <-- undefined
VIE.prototype.Namespaces.prototype.removeNamespace = function (namespace) {
    var prefix = this.getPrefix(namespace);
    if (prefix) {
        delete this._namespaces[prefix];
    }
    return this;
};

// ### toObj()
// This method serializes the namespace instance into an associative
// array representation. The base namespace is given an empty
// string as key.  
// **Parameters**:  
// *{boolean}* **omitBase** If set to ```true``` this omits the baseNamespace.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{object}* : A serialization of the namespaces as an object.  
// **Example usage**:  
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.add("test", "http://test.ns");
//     console.log(namespaces.toObj()); 
//     // <-- {""    : "http://base.ns/", 
//             "test": "http://test.ns"}
//     console.log(namespaces.toObj(true)); 
//     // <-- {"test": "http://test.ns"}
VIE.prototype.Namespaces.prototype.toObj = function (omitBase) {
    if (omitBase) {
        return jQuery.extend({}, this._namespaces);
    }
    return jQuery.extend({'' : this._base}, this._namespaces);
};

// ### curie(uri, safe)
// This method converts a given 
// URI into a CURIE (or SCURIE), based on the given ```VIE.Namespaces``` object.
// If the given uri is already a URI, it is left untouched and directly returned.
// If no prefix could be found, an ```Error``` is thrown.  
// **Parameters**:  
// *{string}* **uri** The URI to be transformed.  
// *{boolean}* **safe** A flag whether to generate CURIEs or SCURIEs.  
// **Throws**:  
// *{Error}* If no prefix could be found in the passed namespaces.  
// **Returns**:  
// *{string}* The CURIE or SCURIE.  
// **Example usage**: 
//
//     var ns = new myVIE.Namespaces(
//           "http://viejs.org/ns/", 
//           { "dbp": "http://dbpedia.org/ontology/" }
//     );
//     var uri = "<http://dbpedia.org/ontology/Person>";
//     ns.curie(uri, false); // --> dbp:Person
//     ns.curie(uri, true); // --> [dbp:Person]
VIE.prototype.Namespaces.prototype.curie = function(uri, safe){
    return VIE.Util.toCurie(uri, safe, this);
};

// ### isCurie(curie)
// This method checks, whether 
// the given string is a CURIE and returns ```true``` if so and ```false```otherwise.  
// **Parameters**:  
// *{string}* **curie** The CURIE (or SCURIE) to be checked.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{boolean}* ```true``` if the given curie is a CURIE or SCURIE and ```false``` otherwise.  
// **Example usage**: 
//
//     var ns = new myVIE.Namespaces(
//           "http://viejs.org/ns/", 
//           { "dbp": "http://dbpedia.org/ontology/" }
//     );
//     var uri = "<http://dbpedia.org/ontology/Person>";
//     var curie = "dbp:Person";
//     var scurie = "[dbp:Person]";
//     var text = "This is some text.";
//     ns.isCurie(uri);    // --> false
//     ns.isCurie(curie);  // --> true
//     ns.isCurie(scurie); // --> true
//     ns.isCurie(text);   // --> false
VIE.prototype.Namespaces.prototype.isCurie = function (something) {
    return VIE.Util.isCurie(something, this);
};
    
// ### uri(curie)
// This method converts a 
// given CURIE (or save CURIE) into a URI, based on the given ```VIE.Namespaces``` object.  
// **Parameters**:  
// *{string}* **curie** The CURIE to be transformed.  
// **Throws**:  
// *{Error}* If no URI could be assembled.  
// **Returns**:  
// *{string}* : A string, representing the URI.  
// **Example usage**: 
//
//     var ns = new myVIE.Namespaces(
//           "http://viejs.org/ns/", 
//           { "dbp": "http://dbpedia.org/ontology/" }
//     );
//     var curie = "dbp:Person";
//     var scurie = "[dbp:Person]";
//     ns.uri(curie); 
//          --> <http://dbpedia.org/ontology/Person>
//     ns.uri(scurie);
//          --> <http://dbpedia.org/ontology/Person>
VIE.prototype.Namespaces.prototype.uri = function (curie) {
    return VIE.Util.toUri(curie, this);
};

// ### isUri(something)
// This method checks, whether the given string is a URI.  
// **Parameters**:  
// *{string}* **something** : The string to be checked.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{boolean}* : ```true``` if the string is a URI, ```false``` otherwise.  
// **Example usage**: 
//
//     var namespaces = new vie.Namespaces("http://base.ns/");
//     namespaces.addOrReplace("test", "http://test.ns");
//     var uri = "<http://test.ns/Person>";
//     var curie = "test:Person";
//     namespaces.isUri(uri);   // --> true
//     namespaces.isUri(curie); // --> false
VIE.prototype.Namespaces.prototype.isUri = VIE.Util.isUri;
// Classic VIE API bindings to new VIE
VIE.prototype.ClassicRDFa = function(vie) {
    this.vie = vie;
};

VIE.prototype.ClassicRDFa.prototype = {
    readEntities: function(selector) {
        var jsonEntities = [];
        var entities = this.vie.RDFaEntities.getInstances(selector);
        _.each(entities, function(entity) {
            jsonEntities.push(entity.toJSONLD());
        });
        return jsonEntities;
    },

    findPredicateElements: function(subject, element, allowNestedPredicates) {
        return this.vie.services.rdfa._findPredicateElements(subject, element, allowNestedPredicates);
    },

    getPredicate: function(element) {
        return this.vie.services.rdfa.getElementPredicate(element);
    },

    getSubject: function(element) {
        return this.vie.services.rdfa.getElementSubject(element);
    }
};

VIE.prototype.ClassicRDFaEntities = function(vie) {
    this.vie = vie;
};

VIE.prototype.ClassicRDFaEntities.prototype = {
    getInstances: function(selector) {
        if (!this.vie.services.rdfa) {
            this.vie.use(new this.vie.RdfaService());
        }
        var foundEntities = null;
        var loaded = false;
        this.vie.load({element: selector}).from('rdfa').execute().done(function(entities) {
            foundEntities = entities;
            loaded = true;
        });

        while (!loaded) {
        }

        return foundEntities;
    },

    getInstance: function(selector) {
        var instances = this.getInstances(selector);
        if (instances && instances.length) {
            return instances.pop();
        }
        return null;
    }
};

VIE.prototype.ClassicEntityManager = function(vie) {
    this.vie = vie;
    this.entities = this.vie.entities;
};

VIE.prototype.ClassicEntityManager.prototype = {
    getBySubject: function(subject) {
        return this.vie.entities.get(subject);
    },

    getByJSONLD: function(json) {
        if (typeof json === 'string') {
            try {
                json = jQuery.parseJSON(json);
            } catch (e) {
                return null;
            }
        }
        return this.vie.entities.addOrUpdate(json);
    },

    initializeCollection: function() {
        return;
    }
};
//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/

// ## VIE - DBPedia service
// The DBPedia service allows a VIE developer to directly query
// the DBPedia database for entities and their properties. Obviously,
// the service does not allow for saving, removing or analyzing methods.
(function(){

// ## VIE.DBPediaService(options)
// This is the constructor to instantiate a new service to collect
// properties of an entity from <a href="http://dbpedia.org">DBPedia</a>.  
// **Parameters**:  
// *{object}* **options** Optional set of fields, ```namespaces```, ```rules```, or ```name```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.DBPediaService}* : A **new** VIE.DBPediaService instance.  
// **Example usage**:  
//
//     var dbpService = new vie.DBPediaService({<some-configuration>});
VIE.prototype.DBPediaService = function (options) {
    var defaults = {
        /* the default name of this service */
        name : 'dbpedia',
        /* default namespaces that are shipped with this service */
        namespaces : {
            owl    : "http://www.w3.org/2002/07/owl#",
            yago   : "http://dbpedia.org/class/yago/",
            foaf: 'http://xmlns.com/foaf/0.1/',
            georss: "http://www.georss.org/georss/",
            geo: 'http://www.w3.org/2003/01/geo/wgs84_pos#',
            rdfs: "http://www.w3.org/2000/01/rdf-schema#",
            rdf: "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            dbpedia: "http://dbpedia.org/ontology/",
            dbprop : "http://dbpedia.org/property/",
            dcelements : "http://purl.org/dc/elements/1.1/"
        },
        /* default rules that are shipped with this service */
        rules : []
    };
    /* the options are merged with the default options */
    this.options = jQuery.extend(true, defaults, options ? options : {});

    this.vie = null; /* this.vie will be set via VIE.use(); */
    /* overwrite options.name if you want to set another name */
    this.name = this.options.name;
    
    /* basic setup for the ajax connection */
    jQuery.ajaxSetup({
        converters: {"text application/rdf+json": function(s){return JSON.parse(s);}},
        timeout: 60000 /* 60 seconds timeout */
    });
};

VIE.prototype.DBPediaService.prototype = {
    
// ### init()
// This method initializes certain properties of the service and is called
// via ```VIE.use()```.  
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.DBPediaService}* : The VIE.DBPediaService instance itself.  
// **Example usage**:  
//
//     var dbpService = new vie.DBPediaService({<some-configuration>});
//     dbpService.init();
    init: function() {

        for (var key in this.options.namespaces) {
            var val = this.options.namespaces[key];
            this.vie.namespaces.add(key, val);
        }
        
        this.rules = jQuery.extend([], VIE.Util.transformationRules(this));
        this.rules = jQuery.merge(this.rules, (this.options.rules) ? this.options.rules : []);
        
        this.connector = new this.vie.DBPediaConnector(this.options);
        
        return this;
    },

// ### load(loadable)
// This method loads the entity that is stored within the loadable into VIE.
// You can also query for multiple queries by setting ```entities``` with
// an array of entities.  
// **Parameters**:  
// *{VIE.Loadable}* **lodable** The loadable.  
// **Throws**:  
// *{Error}* if an invalid VIE.Loadable is passed.  
// **Returns**:  
// *{VIE.DBPediaService}* : The VIE.DBPediaService instance itself.  
// **Example usage**:  
//
//  var dbpService = new vie.DBPediaService({<some-configuration>});
//  dbpService.load(new vie.Loadable({entity : "<http://...>"}));
//    OR
//  var dbpService = new vie.DBPediaService({<some-configuration>});
//  dbpService.load(new vie.Loadable({entities : ["<http://...>", "<http://...>"]}));
    load: function(loadable){
        var service = this;
        
        var correct = loadable instanceof this.vie.Loadable;
        if (!correct) {
            throw new Error("Invalid Loadable passed");
        }
        
        var success = function (results) {
            results = (typeof results === "string")? JSON.parse(results) : results;
            _.defer(function() {
                try {
                    var entities = VIE.Util.rdf2Entities(service, results);
                    entities = (_.isArray(entities))? entities : [ entities ];
                    for (var e = 0; e < entities.length; e++) {
                    	entities[e].set("DBPediaServiceLoad", VIE.Util.xsdDateTime(new Date()));
                    }
                    entities = (entities.length === 1)? entities[0] : entities;
                    loadable.resolve(entities);
                } catch (e) {
                    loadable.reject(e);
                }
            });
        };
        
        var error = function (e) {
            loadable.reject(e);
        };
        
        var entities = (loadable.options.entity)? loadable.options.entity : loadable.options.entities;
        
        if (!entities) {
            loadable.reject([]);
        } else {
        	entities = (_.isArray(entities))? entities : [ entities ];
        	var tmpEntities = [];
        	for (var e = 0; e < entities.length; e++) {
        		var tmpEnt = (typeof entities[e] === "string")? entities[e] : entities[e].id;
        		tmpEntities.push(tmpEnt);
        	}
                        
            this.connector.load(tmpEntities, success, error);
        }
        return this;
    }
};

// ## VIE.DBPediaConnector(options)
// The DBPediaConnector is the connection between the DBPedia service
// and the backend service.  
// **Parameters**:  
// *{object}* **options** The options.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.DBPediaConnector}* : The **new** VIE.DBPediaConnector instance.  
// **Example usage**:  
//
//     var dbpConn = new vie.DBPediaConnector({<some-configuration>});
VIE.prototype.DBPediaConnector = function (options) {
    this.options = options;
    this.baseUrl = "http://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&timeout=0";
};

VIE.prototype.DBPediaConnector.prototype = {

// ### load(uri, success, error, options)
// This method loads all properties from an entity and returns the result by the success callback.  
// **Parameters**:  
// *{string}* **uri** The URI of the entity to be loaded.  
// *{function}* **success** The success callback.  
// *{function}* **error** The error callback.  
// *{object}* **options** Options, like the ```format```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.DBPediaConnector}* : The VIE.DBPediaConnector instance itself.  
// **Example usage**:  
//
//     var dbpConn = new vie.DBPediaConnector(opts);
//     dbpConn.load("<http://dbpedia.org/resource/Barack_Obama>",
//                 function (res) { ... },
//                 function (err) { ... });
    load: function (uri, success, error, options) {
        if (!options) { options = {}; }
        
        var url = this.baseUrl + 
        "&format=" + encodeURIComponent("application/rdf+json") + 
        "&query=";
        
        if (_.isArray(uri)) {
        	var construct = "";
        	var where = "";
        	for (var u = 0; u < uri.length; u++) {
        		var subject = (/^<.+>$/.test(uri[u]))? uri[u] : '<' + uri[u] + '>';
        		if (u > 0) {
        			construct += " .";
        			where += " UNION ";
        		}
        		construct += " " + subject + " ?prop" + u + " ?val" + u;
        		where     += " { " + subject + " ?prop" + u + " ?val" + u + " }";
        	}
        	url += encodeURIComponent("CONSTRUCT {" + construct + " } WHERE {" + where + " }");
        } else {
	        uri = (/^<.+>$/.test(uri))? uri : '<' + uri + '>';
	        url += encodeURIComponent("CONSTRUCT { " + uri + " ?prop ?val } WHERE { " + uri + " ?prop ?val }");
        }
        var format = options.format || "application/rdf+json";

        if (typeof exports !== "undefined" && typeof process !== "undefined") {
            /* We're on Node.js, don't use jQuery.ajax */
            return this._loadNode(url, success, error, options, format);
        }

        jQuery.ajax({
            success: function(response){
                success(response);
            },
            error: error,
            type: "GET",
            url: url,
            accepts: {"application/rdf+json": "application/rdf+json"}
        });
        
        return this;
    },

    _loadNode: function (uri, success, error, options, format) {
        var request = require('request');
        var r = request({
            method: "GET",
            uri: uri,
            headers: {
                Accept: format
            }
        }, function(error, response, body) {
            success(JSON.parse(body));
        });
        r.end();
        
        return this;
    }
};
})();

//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/

// ## VIE - OpenCalaisService service
// The OpenCalaisService ...
(function(){

// ## VIE.OpenCalaisService(options)
// This is the constructor to instantiate a new service to collect
// properties of an entity from OpenCalais.  
// **Parameters**:  
// *{object}* **options** Optional set of fields, ```namespaces```, ```rules```, ```url```, or ```name```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.OpenCalaisService}* : A **new** VIE.OpenCalaisService instance.  
// **Example usage**:  
//
//     var service = new vie.OpenCalaisService({<some-configuration>});
VIE.prototype.OpenCalaisService = function(options) {
    var defaults = {
        /* the default name of this service */
        name : 'opencalais',
        /* you can pass an array of URLs which are then tried sequentially */
        url: ["http://api.opencalais.com/enlighten/rest/"],
        timeout : 60000, /* 60 seconds timeout */
        namespaces : {
        	opencalaisc:  "http://s.opencalais.com/1/pred/",
        	opencalaiscr: "http://s.opencalais.com/1/type/er/",
        	opencalaiscm: "http://s.opencalais.com/1/type/em/e/"
        },
        /* default rules that are shipped with this service */
        rules : []
    };
    /* the options are merged with the default options */
    this.options = jQuery.extend(true, defaults, options ? options : {});

    this.vie = null; /* will be set via VIE.use(); */
    /* overwrite options.name if you want to set another name */
    this.name = this.options.name;
    
    /* basic setup for the ajax connection */
    jQuery.ajaxSetup({
        converters: {"text application/rdf+json": function(s){return JSON.parse(s);}},
        timeout: this.options.timeout
    });
};

VIE.prototype.OpenCalaisService.prototype = {
    
// ### init()
// This method initializes certain properties of the service and is called
// via ```VIE.use()```.  
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.StanbolService}* : The VIE.StanbolService instance itself.  
// **Example usage**:  
//
//     var service = new vie.OpenCalaisService({<some-configuration>});
//     service.init();
    init: function(){

        for (var key in this.options.namespaces) {
            var val = this.options.namespaces[key];
            this.vie.namespaces.add(key, val);
        }
        
        this.rules = jQuery.extend([], VIE.Util.transformationRules(this));
       /* this.rules = jQuery.extend(this.rules, [{
        	'left' : [
        	          '?subject a opencalaiscm:Person',
                      '?subject opencalaisc:name ?name'
                ],
            	'right': function(ns) {
                    return function() {
                        return [
                            jQuery.rdf.triple(this.subject.toString(),
                                'a',
                                '<' + ns.base() + 'Person>', {
                                    namespaces: ns.toObj()
                                }),
                            jQuery.rdf.triple(this.subject.toString(),
                                '<' + ns.base() + 'name>',
                                this.label, {
                                    namespaces: ns.toObj()
                                })
                            ];
                    };
                }(this.vie.namespaces)
            }]);*/
        this.rules = jQuery.merge(this.rules, (this.options.rules) ? this.options.rules : []);
        //this.rules = [];
        this.connector = new this.vie.OpenCalaisConnector(this.options);
    },

// ### analyze(analyzable)
// This method extracts text from the jQuery element and sends it to OpenCalais for analysis.  
// **Parameters**:  
// *{VIE.Analyzable}* **analyzable** The analyzable.  
// **Throws**:  
// *{Error}* if an invalid VIE.Findable is passed.  
// **Returns**:  
// *{VIE.OpenCalaisService}* : The VIE.OpenCalaisService instance itself.  
// **Example usage**:  
//
//     var service = new vie.OpenCalaisService({<some-configuration>});
//     service.analyzable(
//         new vie.Analyzable({element : jQuery("#foo")})
//     );
    analyze: function(analyzable) {
        var service = this;

        var correct = analyzable instanceof this.vie.Analyzable;
        if (!correct) {throw "Invalid Analyzable passed";}

        var element = analyzable.options.element ? analyzable.options.element : jQuery('body');

        var text = service._extractText(element);

        if (text.length > 0) {
            /* query enhancer with extracted text */
            var success = function (results) {
                _.defer(function(){
                    var entities = VIE.Util.rdf2Entities(service, results);
                    analyzable.resolve(entities);
                });
            };
            var error = function (e) {
                analyzable.reject(e);
            };

            this.connector.analyze(text, success, error);

        } else {
            console.warn("No text found in element.");
            analyzable.resolve([]);
        }

    },

    // this private method extracts text from a jQuery element
    _extractText: function (element) {
        if (element.get(0) &&
            element.get(0).tagName &&
            (element.get(0).tagName == 'TEXTAREA' ||
            element.get(0).tagName == 'INPUT' && element.attr('type', 'text'))) {
            return element.get(0).val();
        }
        else {
            var res = element
                .text()    /* get the text of element */
                .replace(/\s+/g, ' ') /* collapse multiple whitespaces */
                .replace(/\0\b\n\r\f\t/g, ''); /* remove non-letter symbols */
            return jQuery.trim(res);
        }
    }
};

// ## VIE.OpenCalaisConnector(options)
// The OpenCalaisConnector is the connection between the VIE OpenCalais service
// and the actual ajax calls.  
// **Parameters**:  
// *{object}* **options** The options.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.OpenCalaisService}* : The **new** VIE.OpenCalaisService instance.  
// **Example usage**:  
//
//     var conn = new vie.OpenCalaisConnector({<some-configuration>});
VIE.prototype.OpenCalaisConnector = function (options) {
    this.options = options;
    this.baseUrl = (_.isArray(options.url))? options.url : [ options.url ];
    this.enhancerUrlPrefix = "/";
};

VIE.prototype.OpenCalaisConnector.prototype = {

// ### analyze(text, success, error, options)
// This method sends the given text to OpenCalais returns the result by the success callback.  
// **Parameters**:  
// *{string}* **text** The text to be analyzed.  
// *{function}* **success** The success callback.  
// *{function}* **error** The error callback.  
// *{object}* **options** Options, like the ```format```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.OpenCalaisConnector}* : The VIE.OpenCalaisConnector instance itself.  
// **Example usage**:  
//
//     var conn = new vie.OpenCalaisConnector(opts);
//     conn.analyze("This is some text.",
//                 function (res) { ... },
//                 function (err) { ... });
    analyze: function(text, success, error, options) {
        if (!options) { options = { urlIndex : 0}; }
        if (options.urlIndex >= this.baseUrl.length) {
            error("Could not connect to the given OpenCalais endpoints! Please check for their setup!");
            return;
        }
        
        var enhancerUrl = this.baseUrl[options.urlIndex].replace(/\/$/, '');
        enhancerUrl += this.enhancerUrlPrefix;
        
        var format = options.format || "application/rdf+json";
        
        var retryErrorCb = function (c, t, s, e, o) {
            /* in case a OpenCalais backend is not responding and
             * multiple URLs have been registered
             */
            return  function () {
                console.error("OpenCalais connection error", arguments);
                c.analyze(t, s, e, _.extend(o, {urlIndex : o.urlIndex+1}));
            };
        }(this, text, success, error, options);
        
        var data = this._prepareData(text);

        if (typeof exports !== "undefined" && typeof process !== "undefined") {
            /* We're on Node.js, don't use jQuery.ajax */
            return this._analyzeNode(enhancerUrl, data, success, retryErrorCb, options, format);
        }

        jQuery.ajax({
            success: function(a, b, c){
            	var responseData = c.responseText.replace(/<!--[\s\S]*?-->/g, '');
            	success(responseData);
            },
            error: retryErrorCb,
            type: "POST",
            url: enhancerUrl,
            data: data,
            accept: "text/plain"
        });
    },

    _analyzeNode: function(url, text, success, errorCB, options, format) {
        var request = require('request');
        var r = request({
            method: "POST",
            uri: url,
            body: text,
            headers: {
                Accept: format
            }
        }, function(error, response, body) {
            try {
                success({results: JSON.parse(body)});
            } catch (e) {
                errorCB(e);
            }
        });
        r.end();
    },
    
    _prepareData : function (text) {
    	return {
    		licenseID: this.options.api_key,
            calculareRelevanceScore: "true",
            enableMetadataType: "GenericRelations,SocialTags",
            contentType: "text/html",
            content: text
            // for more options check http://developer.opencalais.com/docs/suggest/
        };
    }
};
})();


(function(){
	
	VIE.prototype.RdfaRdfQueryService = function(options) {
		var defaults = {
	        name : 'rdfardfquery',
	        namespaces : {},
	        rules : []
	    };
	    /* the options are merged with the default options */
	    this.options = jQuery.extend(true, defaults, options ? options : {});

	    this.views = [],

	    this.vie = null; /* will be set via VIE.use(); */
	    /* overwrite options.name if you want to set another name */
	    this.name = this.options.name;
};

VIE.prototype.RdfaRdfQueryService.prototype = {

    init: function(){

        for (var key in this.options.namespaces) {
            var val = this.options.namespaces[key];
            this.vie.namespaces.add(key, val);
        }
        
        this.rules = jQuery.extend([], VIE.Util.transformationRules(this));
        this.rules = jQuery.merge(this.rules, (this.options.rules) ? this.options.rules : []);
    },
	    
    analyze: function(analyzable) {
        // in a certain way, analyze is the same as load
        return this.load(analyzable);
    },
        
    load : function(loadable) {
        var service = this;
        var correct = loadable instanceof this.vie.Loadable || loadable instanceof this.vie.Analyzable;
        if (!correct) {
            throw new Error("Invalid Loadable/Analyzable passed");
        }
        
        var element = loadable.options.element ? loadable.options.element : jQuery(document);
        try {
            var rdf = jQuery(element).find("[about],[typeof]").rdfa();
            
            jQuery.each(jQuery(element).xmlns(), function(prefix, ns){
                service.vie.namespaces.addOrReplace(prefix, ns.toString());
            });
            
            var entities = VIE.Util.rdf2Entities(this, rdf);
            
            loadable.resolve(entities);
        } catch (e) {
            loadable.reject(e);
        }
    },

    save : function(savable) {
        var correct = savable instanceof this.vie.Savable;
        if (!correct) {
            savable.reject("Invalid Savable passed");
        }
    
        if (!savable.options.element) {
            savable.reject("Unable to write entity to RDFa, no element given");
        }
    
        if (!savable.options.entity) {
            savable.reject("Unable to write to RDFa, no entity given");
        }
        
        if (!jQuery.rdf) {
            savable.reject("No rdfQuery found.");
        }
        var entity = savable.options.entity;
        
        var triples = [];
        var type = entity.get('@type');
        type = (jQuery.isArray(type))? type[0] : type;
        type = type.id;
        triples.push(entity.getSubject() + " a " + type);
        //TODO: add all attributes!
        jQuery(savable.options.element).rdfa(triples);
    
        savable.resolve();
    }
    
};

})();//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/

// ## VIE - RdfaService service
// The RdfaService service allows ...

(function(){

// ## VIE.RdfaService(options)
// This is the constructor to instantiate a new service.  
// **Parameters**:  
// *{object}* **options** Optional set of fields, ```namespaces```, ```rules```, ```url```, or ```name```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.RdfaService}* : A **new** VIE.RdfaService instance.  
// **Example usage**:  
//
//     var rdfaService = new vie.RdfaService({<some-configuration>});
VIE.prototype.RdfaService = function(options) {
    var defaults = {
        name : 'rdfa',
        namespaces : {},
        subjectSelector : "[about],[typeof],[src],html",
        predicateSelector : "[property],[rel]",
        /* default rules that are shipped with this service */
        rules : []
    };
    /* the options are merged with the default options */
    this.options = jQuery.extend(true, defaults, options ? options : {});

    this.views = [],

    this.vie = null; /* will be set via VIE.use(); */
    /* overwrite options.name if you want to set another name */
    this.name = this.options.name;
};

VIE.prototype.RdfaService.prototype = {
    
// ### init()
// This method initializes certain properties of the service and is called
// via ```VIE.use()```.  
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.RdfaService}* : The VIE.RdfaService instance itself.  
// **Example usage**:  
//
//     var rdfaService = new vie.RdfaService({<some-configuration>});
//     rdfaService.init();
    init: function(){

        for (var key in this.options.namespaces) {
            var val = this.options.namespaces[key];
            this.vie.namespaces.add(key, val);
        }
        
        this.rules = jQuery.merge([], VIE.Util.transformationRules(this));
        this.rules = jQuery.merge(this.rules, (this.options.rules) ? this.options.rules : []);
    },
    
    analyze: function(analyzable) {
        // in a certain way, analyze is the same as load
        return this.load(analyzable);
    },
        
    load : function(loadable) {
        var service = this;
        var correct = loadable instanceof this.vie.Loadable || loadable instanceof this.vie.Analyzable;
        if (!correct) {
            throw new Error("Invalid Loadable/Analyzable passed");
        }

        var element;
        if (!loadable.options.element) {
            if (typeof document === 'undefined') { 
                return loadable.resolve([]);
            }
            element = jQuery(document);
        } else {
            element = loadable.options.element;
        }
    
        var ns = this.xmlns(element);
        for (var prefix in ns) {
            this.vie.namespaces.addOrReplace(prefix, ns[prefix]);
        }
        var entities = [];
        var entityElements = jQuery(this.options.subjectSelector, element).add(jQuery(element).filter(this.options.subjectSelector)).each(function() {
            var entity = service._readEntity(jQuery(this));
            if (entity) {
                entities.push(entity);
            }
        });
        loadable.resolve(entities);
    },

    save : function(savable) {
        var correct = savable instanceof this.vie.Savable;
        if (!correct) {
            throw "Invalid Savable passed";
        }
    
        if (!savable.options.element) {
            // FIXME: we could find element based on subject
            throw "Unable to write entity to RDFa, no element given";
        }
    
        if (!savable.options.entity) {
            throw "Unable to write to RDFa, no entity given";
        }
    
        this._writeEntity(savable.options.entity, savable.options.element);
        savable.resolve();
    },
    
    _readEntity : function(element) {
        var subject = this.getElementSubject(element);
        var type = this._getElementType(element);
        var predicate, value, valueCollection;
        var entity = this._readEntityPredicates(subject, element, false);
        if (jQuery.isEmptyObject(entity)) {
            return null;
        }
        var vie = this.vie;
        for (predicate in entity) {
            value = entity[predicate];
            if (!_.isArray(value)) {
                continue;
            }
            valueCollection = new this.vie.Collection();
            _.each(value, function(valueItem) {
                var linkedEntity = vie.entities.addOrUpdate({'@subject': valueItem});
                valueCollection.addOrUpdate(linkedEntity);
            });
            entity[predicate] = valueCollection;
        }
        entity['@subject'] = subject;
        if (type) {
            entity['@type'] = type;
        }
        var entityInstance = new this.vie.Entity(entity);
        entityInstance = this.vie.entities.addOrUpdate(entityInstance, {
          updateOptions: {
            silent: true
          }
        });
        this._registerEntityView(entityInstance, element);
        return entityInstance;
    },
    
    _writeEntity : function(entity, element) {
        var service = this;
        this._findPredicateElements(this.getElementSubject(element), element, true).each(function() {
            var predicateElement = jQuery(this);
            var predicate = service.getElementPredicate(predicateElement);
            if (!entity.has(predicate)) {
                return true;
            }
    
            var value = entity.get(predicate);
            if (value && value.isCollection) {
                // Handled by CollectionViews separately
                return true;
            }
            if (value === service.readElementValue(predicate, predicateElement)) {
                return true;
            }
            service.writeElementValue(predicate, predicateElement, value);
        });
        return true;
    },
    
    _getViewForElement : function(element, collectionView) {
        var viewInstance;
        jQuery.each(this.views, function() {
            if (jQuery(this.el).get(0) === element.get(0)) {
                if (collectionView && !this.template) {
                    return true;
                }
                viewInstance = this;
                return false;
            }
        });
        return viewInstance;
    },
    
    _registerEntityView : function(entity, element) {
        if (!element.length) {
            return;
        }

        var service = this;
        var viewInstance = this._getViewForElement(element);
        if (viewInstance) {
            return viewInstance;
        }
    
        viewInstance = new this.vie.view.Entity({
            model: entity,
            el: element,
            tagName: element.get(0).nodeName,
            vie: this.vie,
            service: this.name
        });
        this.views.push(viewInstance);
    
        // Find collection elements and create collection views for them
        _.each(entity.attributes, function(value, predicate) {
            var attributeValue = entity.fromReference(entity.get(predicate));
            if (attributeValue.isCollection) {
                jQuery.each(service.getElementByPredicate(predicate, element), function() {
                    service._registerCollectionView(attributeValue, jQuery(this), entity);
                });
            }
        });
        return viewInstance;
    },
    
    _registerCollectionView : function(collection, element, entity) {
        var viewInstance = this._getViewForElement(element, true);
        if (viewInstance) {
            return viewInstance;
        }
    
        var entityTemplate = element.children(':first-child');
    
        viewInstance = new this.vie.view.Collection({
            owner: entity,
            collection: collection,
            model: collection.model,
            el: element,
            template: entityTemplate,
            service: this,
            tagName: element.get(0).nodeName
        });
        this.views.push(viewInstance);
        return viewInstance;
    },
    
    _getElementType : function (element) {
        var type;
        if (jQuery(element).attr('typeof') !== this.options.attributeExistenceComparator) {
            type = jQuery(element).attr('typeof');
            if (type.indexOf("://") !== -1) {
                return "<" + type + ">";
            } else {
                return type;
            }
        }
        return null;
    },
    
    getElementSubject : function(element) {
        var service = this;
        if (typeof document !== 'undefined') { 
            if (element === document) {
                return document.baseURI;
            }
        }
        var subject = undefined;
        var matched = null;
        jQuery(element).closest(this.options.subjectSelector).each(function() {
            matched = this;
            if (jQuery(this).attr('about') !== service.options.attributeExistenceComparator) {
                subject = jQuery(this).attr('about');
                return true;
            }
            if (jQuery(this).attr('src') !== service.options.attributeExistenceComparator) {
                subject = jQuery(this).attr('src');
                return true;
            }
            if (jQuery(this).attr('typeof') !== service.options.attributeExistenceComparator) {
                return true;
            }
            // We also handle baseURL outside browser context by manually
            // looking for the `<base>` element inside HTML head.
            if (jQuery(this).get(0).nodeName === 'HTML') {
                jQuery('base', this).each(function() {
                    subject = jQuery(this).attr('href');
                });
            }
        });

        if (!subject) {
            if (matched === element) {
                // Workaround for https://github.com/assaf/zombie/issues/235
                return service.getElementSubject(jQuery(element).parent());
            }
            return undefined;
        }
                
        if (typeof subject === 'object') {
            return subject;
        }
        if (subject.indexOf('_:') === 0) {
            return subject;
        }
        if (subject.indexOf('<') === 0) {
            return subject;
        }
        return "<" + subject + ">";
    },
    
    setElementSubject : function(subject, element) {
        if (jQuery(element).attr('src')) {
            return jQuery(element).attr('src', subject);
        }
        return jQuery(element).attr('about', subject);
    },
    
    getElementPredicate : function(element) {
        var predicate;
        element = jQuery(element);
        predicate = element.attr('property');
        if (!predicate) {
            predicate = element.attr('rel');
        }
        return predicate;
    },
    
    getElementBySubject : function(subject, element) {
        var service = this;
        return jQuery(element).find(this.options.subjectSelector).add(jQuery(element).filter(this.options.subjectSelector)).filter(function() {
            if (service.getElementSubject(jQuery(this)) !== subject) {
                return false;
            }
     
            return true;
        });
    },
    
    getElementByPredicate : function(predicate, element) {
        var service = this;
        var subject = this.getElementSubject(element);
        return jQuery(element).find(this.options.predicateSelector).add(jQuery(element).filter(this.options.predicateSelector)).filter(function() {
            var foundPredicate = service.getElementPredicate(jQuery(this));
            if (service.vie.namespaces.curie(foundPredicate) !== service.vie.namespaces.curie(predicate)) {
                return false;
            }
    
            if (service.getElementSubject(this) !== subject) {
                return false;
            }
     
            return true;
        });
    },
    
    _readEntityPredicates : function(subject, element, emptyValues) {
        var service = this;
        var entityPredicates = {};
    
        this._findPredicateElements(subject, element, true).each(function() {
            var predicateElement = jQuery(this);
            var predicate = service.getElementPredicate(predicateElement);
            if (predicate === '') {
                return;
            }
            var value = service.readElementValue(predicate, predicateElement);
            if (value === null && !emptyValues) {
                return;
            }
   
            entityPredicates[predicate] = value;
        });
    
        if (jQuery(element).get(0).tagName !== 'HTML') {
            jQuery(element).parent('[rev]').each(function() {
                var relation = jQuery(this).attr('rev');
                if (!relation) {
                    return;
                }
                entityPredicates[jQuery(this).attr('rev')] = service.getElementSubject(this); 
            });
        }
        return entityPredicates;
    },
    
    _findPredicateElements : function(subject, element, allowNestedPredicates) {
        var service = this;
        return jQuery(element).find(this.options.predicateSelector).add(jQuery(element).filter(this.options.predicateSelector)).filter(function() {
            if (service.getElementSubject(this) !== subject) {
                return false;
            }
            if (!allowNestedPredicates) {
                if (!jQuery(this).parents('[property]').length) {
                    return true;
                }
                return false;
            }
    
            return true;
        });
    },
    
    readElementValue : function(predicate, element) {
        // The `content` attribute can be used for providing machine-readable
        // values for elements where the HTML presentation differs from the
        // actual value.
        var content = element.attr('content');
        if (content) {
            return content;
        }
                
        // The `resource` attribute can be used to link a predicate to another
        // RDF resource.
        var resource = element.attr('resource');
        if (resource) {
            return ["<" + resource + ">"];
        }
                
        // `href` attribute also links to another RDF resource.
        var href = element.attr('href');
        if (href && element.attr('rel') === predicate) {
            return ["<" + href + ">"];
        }
    
        // If the predicate is a relation, we look for identified child objects
        // and provide their identifiers as the values. To protect from scope
        // creep, we only support direct descentants of the element where the
        // `rel` attribute was set.
        if (element.attr('rel')) {
            var value = [];
            var service = this;
            jQuery(element).children(this.options.subjectSelector).each(function() {
                value.push(service.getElementSubject(this));
            });
            return value;
        }
    
        // If none of the checks above matched we return the HTML contents of
        // the element as the literal value.
        return element.html();
    },
    
    writeElementValue : function(predicate, element, value) {
        //TODO: this is a hack, please fix!
        if (value instanceof Array && value.length > 0) {
            value = value[0];
        }
        
        // The `content` attribute can be used for providing machine-readable
        // values for elements where the HTML presentation differs from the
        // actual value.
        var content = element.attr('content');
        if (content) {
            element.attr('content', value);
            return;
        }
                
        // The `resource` attribute can be used to link a predicate to another
        // RDF resource.
        var resource = element.attr('resource');
        if (resource) {
            element.attr('resource', value);
        }
    
        // Property has inline value. Change the HTML contents of the property
        // element to match the new value.
        element.html(value);
    },
    
    // mostyl copied from http://code.google.com/p/rdfquery/source/browse/trunk/jquery.xmlns.js
    xmlns : function (elem) {
        var $elem;
        if (!elem) {
            if (typeof document === 'undefined') { 
                return {};
            }
            $elem = jQuery(document);
        } else {
            $elem = jQuery(elem);
        }
        // Collect namespace definitions from the element and its parents
        $elem = $elem.add($elem.parents());
        var obj = {};

        $elem.each(function (i, e) {
            if (e.attributes) {
                for (i = 0; i < e.attributes.length; i += 1) {
                    var attr = e.attributes[i];
                    if (/^xmlns(:(.+))?$/.test(attr.nodeName)) {
                        var prefix = /^xmlns(:(.+))?$/.exec(attr.nodeName)[2] || '';
                        var value = attr.nodeValue;
                        if (prefix === '' || value !== '') {
                            obj[prefix] = attr.nodeValue;
                        }
                    }
                }
            }
        });
        
        return obj;
    }

};

})();
//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/

// ## VIE - StanbolService service
// The StanbolService service allows a VIE developer to directly query
// the <a href="http://incubator.apache.org/stanbol/">Apache Stanbol</a> entityhub for entities and their properties. 
// Furthermore, it gives access to the enhance facilities of
// Stanbol to analyze content and semantically enrich it.
(function(){

// ## VIE.StanbolService(options)
// This is the constructor to instantiate a new service to collect
// properties of an entity from <a href="http://incubator.apache.org/stanbol/">Apache Stanbol</a>.  
// **Parameters**:  
// *{object}* **options** Optional set of fields, ```namespaces```, ```rules```, ```url```, or ```name```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.StanbolService}* : A **new** VIE.StanbolService instance.  
// **Example usage**:  
//
//     var stnblService = new vie.StanbolService({<some-configuration>});
VIE.prototype.StanbolService = function(options) {
    var defaults = {
        /* the default name of this service */
        name : 'stanbol',
        /* you can pass an array of URLs which are then tried sequentially */
        url: ["http://dev.iks-project.eu/stanbolfull"],
        timeout : 60000, /* 60 seconds timeout */
        namespaces : {
            semdeski : "http://www.semanticdesktop.org/ontologies/2007/01/19/nie#",
            semdeskf : "http://www.semanticdesktop.org/ontologies/2007/03/22/nfo#",
            skos: "http://www.w3.org/2004/02/skos/core#",
            foaf: "http://xmlns.com/foaf/0.1/",
            opengis: "http://www.opengis.net/gml/",
            dbpedia: "http://dbpedia.org/ontology/",
            dbprop: "http://dbpedia.org/property/",
            owl : "http://www.w3.org/2002/07/owl#",
            geonames : "http://www.geonames.org/ontology#",
            enhancer : "http://fise.iks-project.eu/ontology/",
            entityhub: "http://www.iks-project.eu/ontology/rick/model/",
            entityhub2: "http://www.iks-project.eu/ontology/rick/query/",
            rdf: "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            rdfs: "http://www.w3.org/2000/01/rdf-schema#",
            dcterms  : 'http://purl.org/dc/terms/',
            schema: 'http://schema.org/',
            geo: 'http://www.w3.org/2003/01/geo/wgs84_pos#'
        },
        /* default rules that are shipped with this service */
        rules : [
            /* rule to add backwards-relations to the triples
             * this makes querying for entities a lot easier!
             */
            {
                'left' : [
                    '?subject a <http://fise.iks-project.eu/ontology/EntityAnnotation>',
                    '?subject enhancer:entity-type ?type',
                    '?subject enhancer:confidence ?confidence',
                    '?subject enhancer:entity-reference ?entity',
                    '?subject dcterms:relation ?relation',
                    '?relation a <http://fise.iks-project.eu/ontology/TextAnnotation>',
                    '?relation enhancer:selected-text ?selected-text',
                    '?relation enhancer:selection-context ?selection-context',
                    '?relation enhancer:start ?start',
                    '?relation enhancer:end ?end'
                ],
                'right' : [
                    '?entity a ?type',
                    '?entity enhancer:hasTextAnnotation ?relation',
                    '?entity enhancer:hasEntityAnnotation ?subject'
                ]
            }
        ]
    };
    /* the options are merged with the default options */
    this.options = jQuery.extend(true, defaults, options ? options : {});

    this.vie = null; /* will be set via VIE.use(); */
    /* overwrite options.name if you want to set another name */
    this.name = this.options.name;
    
    /* basic setup for the ajax connection */
    jQuery.ajaxSetup({
        converters: {"text application/rdf+json": function(s){return JSON.parse(s);}},
        timeout: this.options.timeout
    });
};

VIE.prototype.StanbolService.prototype = {
    
// ### init()
// This method initializes certain properties of the service and is called
// via ```VIE.use()```.  
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.StanbolService}* : The VIE.StanbolService instance itself.  
// **Example usage**:  
//
//     var stnblService = new vie.StanbolService({<some-configuration>});
//     stnblService.init();
    init: function(){

        for (var key in this.options.namespaces) {
            var val = this.options.namespaces[key];
            this.vie.namespaces.add(key, val);
        }
        
        this.rules = jQuery.extend([], VIE.Util.transformationRules(this));
        this.rules = jQuery.merge(this.rules, (this.options.rules) ? this.options.rules : []);
        
        this.connector = new this.vie.StanbolConnector(this.options);

        /* adding these entity types to VIE helps later the querying */
        this.vie.types.addOrOverwrite('enhancer:EntityAnnotation', [
            /*TODO: add attributes */
        ]).inherit("owl:Thing");
        this.vie.types.addOrOverwrite('enhancer:TextAnnotation', [
            /*TODO: add attributes */
        ]).inherit("owl:Thing");
        this.vie.types.addOrOverwrite('enhancer:Enhancement', [
            /*TODO: add attributes */
        ]).inherit("owl:Thing");
    },

// ### analyze(analyzable)
// This method extracts text from the jQuery element and sends it to Apache Stanbol for analysis.  
// **Parameters**:  
// *{VIE.Analyzable}* **analyzable** The analyzable.  
// **Throws**:  
// *{Error}* if an invalid VIE.Findable is passed.  
// **Returns**:  
// *{VIE.StanbolService}* : The VIE.StanbolService instance itself.  
// **Example usage**:  
//
//     var stnblService = new vie.StanbolService({<some-configuration>});
//     stnblService.analyzable(
//         new vie.Analyzable({element : jQuery("#foo")})
//     );
    analyze: function(analyzable) {
        var service = this;

        var correct = analyzable instanceof this.vie.Analyzable;
        if (!correct) {throw "Invalid Analyzable passed";}

        var element = analyzable.options.element ? analyzable.options.element : jQuery('body');

        var text = service._extractText(element);

        if (text.length > 0) {
            /* query enhancer with extracted text */
            var success = function (results) {
                _.defer(function(){
                    var entities = VIE.Util.rdf2Entities(service, results);
                    analyzable.resolve(entities);
                });
            };
            var error = function (e) {
                analyzable.reject(e);
            };

            this.connector.analyze(text, success, error);

        } else {
            console.warn("No text found in element.");
            analyzable.resolve([]);
        }

    },

// ### find(findable)
// This method finds entities given the term from the entity hub.  
// **Parameters**:  
// *{VIE.Findable}* **findable** The findable.  
// **Throws**:  
// *{Error}* if an invalid VIE.Findable is passed.  
// **Returns**:  
// *{VIE.StanbolService}* : The VIE.StanbolService instance itself.  
// **Example usage**:  
//
//     var stnblService = new vie.StanbolService({<some-configuration>});
//     stnblService.load(new vie.Findable({
//         term : "Bischofsh", 
//         limit : 10, 
//         offset: 0
//     }));
    find: function (findable) {
        var correct = findable instanceof this.vie.Findable;
        if (!correct) {throw "Invalid Findable passed";}
        var service = this;
        /* The term to find, * as wildcard allowed */
        var term = escape(findable.options.term);
        if(!term){
            console.warn("StanbolConnector: No term to look for!");
            findable.resolve([]);
        };
        var limit = (typeof findable.options.limit === "undefined") ? 20 : findable.options.limit;
        var offset = (typeof findable.options.offset === "undefined") ? 0 : findable.options.offset;
        var success = function (results) {
            _.defer(function(){
                var entities = VIE.Util.rdf2Entities(service, results);
                findable.resolve(entities);
            });
        };
        var error = function (e) {
            findable.reject(e);
        };
        this.connector.find(term, limit, offset, success, error);
    },

// ### load(loadable)
// This method loads the entity that is stored within the loadable into VIE.  
// **Parameters**:  
// *{VIE.Loadable}* **lodable** The loadable.  
// **Throws**:  
// *{Error}* if an invalid VIE.Loadable is passed.  
// **Returns**:  
// *{VIE.StanbolService}* : The VIE.StanbolService instance itself.  
// **Example usage**:  
//
//     var stnblService = new vie.StanbolService({<some-configuration>});
//     stnblService.load(new vie.Loadable({
//         entity : "<http://...>"
//     }));
    load: function(loadable){
        var correct = loadable instanceof this.vie.Loadable;
        if (!correct) {throw "Invalid Loadable passed";}
        var service = this;

        var entity = loadable.options.entity;
        if(!entity){
            console.warn("StanbolConnector: No entity to look for!");
            loadable.resolve([]);
        };
        var success = function (results) {
            _.defer(function(){
                var entities = VIE.Util.rdf2Entities(service, results);
                loadable.resolve(entities);
            });
        };
        var error = function (e) {
            loadable.reject(e);
        };
        this.connector.load(entity, success, error);
    },

    // this private method extracts text from a jQuery element
    _extractText: function (element) {
        if (element.get(0) &&
            element.get(0).tagName &&
            (element.get(0).tagName == 'TEXTAREA' ||
            element.get(0).tagName == 'INPUT' && element.attr('type', 'text'))) {
            return element.get(0).val();
        }
        else {
            var res = element
                .text()    /* get the text of element */
                .replace(/\s+/g, ' ') /* collapse multiple whitespaces */
                .replace(/\0\b\n\r\f\t/g, ''); /* remove non-letter symbols */
            return jQuery.trim(res);
        }
    }
};

// ## VIE.StanbolConnector(options)
// The StanbolConnector is the connection between the VIE Stanbol service
// and the actual ajax calls.  
// **Parameters**:  
// *{object}* **options** The options.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.StanbolConnector}* : The **new** VIE.StanbolConnector instance.  
// **Example usage**:  
//
//     var stnblConn = new vie.StanbolConnector({<some-configuration>});
VIE.prototype.StanbolConnector = function (options) {
    options = (options)? options : {};
    this.options = options;
    this.baseUrl = (_.isArray(options.url))? options.url : [ options.url ];
    this.enhancerUrlPostfix = (options.enhancerUrlPostfix)? options.enhancerUrlPostfix : "/enhancer";
    this.entityhubUrlPostfix = (options.entityhubUrlPostfix)? options.entityhubUrlPostfix : "/entityhub";
    /*TODO: this.ontonetUrlPostfix = "/ontonet"; */
    /*TODO: this.rulesUrlPostfix = "/rules"; */
    /*TODO: this.factstoreUrlPostfix = "/factstore"; */
};

VIE.prototype.StanbolConnector.prototype = {

// ### analyze(text, success, error, options)
// This method sends the given text to Apache Stanbol returns the result by the success callback.  
// **Parameters**:  
// *{string}* **text** The text to be analyzed.  
// *{function}* **success** The success callback.  
// *{function}* **error** The error callback.  
// *{object}* **options** Options, like the ```format```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.StanbolConnector}* : The VIE.StanbolConnector instance itself.  
// **Example usage**:  
//
//     var stnblConn = new vie.StanbolConnector(opts);
//     stnblConn.analyze("This is some text.",
//                 function (res) { ... },
//                 function (err) { ... });
    analyze: function(text, success, error, options) {
        if (!options) { options = { urlIndex : 0}; }
        if (options.urlIndex >= this.baseUrl.length) {
            error("Could not connect to the given Stanbol endpoints! Please check for their setup!");
            return;
        }
        
        var enhancerUrl = this.baseUrl[options.urlIndex].replace(/\/$/, '');
        enhancerUrl += this.enhancerUrlPostfix;
        
        var format = options.format || "application/rdf+json";
        
        var retryErrorCb = function (c, t, s, e, o) {
            /* in case a Stanbol backend is not responding and
             * multiple URLs have been registered
             */
            return  function () {
                console.error("Stanbol connection error", arguments);
                c.analyze(t, s, e, _.extend(o, {urlIndex : o.urlIndex+1}));
            };
        }(this, text, success, error, options);

        if (typeof exports !== "undefined" && typeof process !== "undefined") {
            /* We're on Node.js, don't use jQuery.ajax */
            return this._analyzeNode(enhancerUrl, text, success, retryErrorCb, options, format);
        }

        jQuery.ajax({
            success: function(response){
                success(response);
            },
            error: retryErrorCb,
            type: "POST",
            url: enhancerUrl,
            data: text,
            dataType: format,
            contentType: "text/plain",
            accepts: {"application/rdf+json": "application/rdf+json"}

        });
    },

    _analyzeNode: function(url, text, success, errorCB, options, format) {
        var request = require('request');
        var r = request({
            method: "POST",
            uri: url,
            body: text,
            headers: {
                Accept: format
            }
        }, function(error, response, body) {
            try {
                success({results: JSON.parse(body)});
            } catch (e) {
                errorCB(e);
            }
        });
        r.end();
    },

// ### load(uri, success, error, options)
// This method loads all properties from an entity and returns the result by the success callback.  
// **Parameters**:  
// *{string}* **uri** The URI of the entity to be loaded.  
// *{function}* **success** The success callback.  
// *{function}* **error** The error callback.  
// *{object}* **options** Options, like the ```format```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.StanbolConnector}* : The VIE.StanbolConnector instance itself.  
// **Example usage**:  
//
//     var stnblConn = new vie.StanbolConnector(opts);
//     stnblConn.load("<http://dbpedia.org/resource/Barack_Obama>",
//                 function (res) { ... },
//                 function (err) { ... });
    load: function (uri, success, error, options) {
        if (!options) { options = { urlIndex : 0}; }
        if (options.urlIndex >= this.baseUrl.length) {
            error("Could not connect to the given Stanbol endpoints! Please check for their setup!");
            return;
        }
        
        uri = uri.replace(/^</, '').replace(/>$/, '');
        var url = this.baseUrl[options.urlIndex].replace(/\/$/, '');
        url += this.entityhubUrlPostfix + "/sites/entity?id=" + escape(uri);
        
        var format = options.format || "application/rdf+json";
        
        var retryErrorCb = function (c, u, s, e, o) {
            /* in case an backend of Stanbol is not responding and
             * multiple URLs have been registered
             */
            return  function () {
                c.load(u, s, e, _.extend(o, {urlIndex : o.urlIndex+1}));
            };
        }(this, uri, success, error, options);
        
        if (typeof exports !== "undefined" && typeof process !== "undefined") {
            /* We're on Node.js, don't use jQuery.ajax */
            return this._loadNode(url, success, retryErrorCb, options, format);
        }
        
        jQuery.ajax({
            success: function(response){
                success(response);
            },
            error: retryErrorCb,
            type: "GET",
            url: url,
            data: null,
            dataType: format,
            contentType: "text/plain",
            accepts: {"application/rdf+json": "application/rdf+json"}
        });
    },

    _loadNode: function (uri, success, errorCB, options, format) {
        var request = require('request');
        var r = request({
            method: "GET",
            uri: uri,
            headers: {
                Accept: format
            }
        }, function(error, response, body) {
            try {
                success(JSON.parse(body));
            } catch (e) {
                errorCB(e);
            }
        });
        r.end();
        
        return this;
    },

// ### find(term, limit, offset, success, error, options)
// This method finds entities given the term from the entity hub and returns the result by the success callback.  
// **Parameters**:  
// *{string}* **term** The term to be searched for. 
// *{int}* **limit** The limit of results to be returned. 
// *{int}* **offset** The offset to be search for.  
// *{function}* **success** The success callback.  
// *{function}* **error** The error callback.  
// *{object}* **options** Options, like the ```format```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.StanbolConnector}* : The VIE.StanbolConnector instance itself.  
// **Example usage**:  
//
//     var stnblConn = new vie.StanbolConnector(opts);
//     stnblConn.find("Bishofsh", 10, 0,
//                 function (res) { ... },
//                 function (err) { ... });
    find: function (term, limit, offset, success, error, options) {
        /* curl -X POST -d "name=Bishofsh&limit=10&offset=0" http://localhost:8080/entityhub/sites/find */
        if (!options) { options = { urlIndex : 0}; }
        
        if (options.urlIndex >= this.baseUrl.length) {
            error("Could not connect to the given Stanbol endpoints! Please check for their setup!");
            return;
        }
        
        var url = this.baseUrl[options.urlIndex].replace(/\/$/, '');
        url += this.entityhubUrlPostfix + "/sites/find";
        
        var format = options.format || "application/rdf+json";
        
        if (offset == null) {
            offset = 0;
        }
        if (limit == null) {
            limit = 10;
        }
        
        var retryErrorCb = function (c, t, l, of, s, e, o) {
            /* in case an backend of Stanbol is not responding and
             * multiple URLs have been registered
             */
            return  function () {
                c.find(t, l, of, s, e, _.extend(o, {urlIndex : o.urlIndex+1}));
            };
        }(this, term, limit, offset, success, error, options);
        
        if (typeof exports !== "undefined" && typeof process !== "undefined") {
            /* We're on Node.js, don't use jQuery.ajax */
            return this._findNode(url, term, limit, offset, success, retryErrorCb, options, format);
        }
        
        jQuery.ajax({
            success: function(response){
                success(response);
            },
            error: retryErrorCb,
            type: "POST",
            url: url,
            data: "name=" + term + "&limit=" + limit + "&offset=" + offset,
            dataType: format,
            accepts: {"application/rdf+json": "application/rdf+json"}
        });
    },

    _findNode: function (uri, term, limit, offset, success, errorCB, options, format) {
        var request = require('request');
        var r = request({
            method: "POST",
            uri: uri,
            headers: {
                Accept: format
            },
            body : "name=" + term + "&limit=" + limit + "&offset=" + offset
        }, function(error, response, body) {
            try {
                success(JSON.parse(body));
            } catch (e) {
                errorCB(e);
            }
        });
        r.end();
        
        return this;
    }
};
})();

//     VIE - Vienna IKS Editables
//     (c) 2011 Henri Bergius, IKS Consortium
//     (c) 2011 Sebastian Germesin, IKS Consortium
//     (c) 2011 Szaby Grünwald, IKS Consortium
//     VIE may be freely distributed under the MIT license.
//     For all details and documentation:
//     http://viejs.org/

// ## VIE - ZemantaService service
// The ZemantaService ...
(function(){

// ## VIE.ZemantaService(options)
// This is the constructor to instantiate a new service to collect
// properties of an entity from Zemanta.  
// **Parameters**:  
// *{object}* **options** Optional set of fields, ```namespaces```, ```rules```, ```url```, or ```name```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.ZemantaService}* : A **new** VIE.ZemantaService instance.  
// **Example usage**:  
//
//     var service = new vie.ZemantaService({<some-configuration>});
VIE.prototype.ZemantaService = function(options) {
    var defaults = {
        /* the default name of this service */
        name : 'zemanta',
        /* you can pass an array of URLs which are then tried sequentially */
        url: ["http://api.zemanta.com/services/rest/0.0/"],
        timeout : 60000, /* 60 seconds timeout */
        namespaces : {
        	zemanta: "http://s.zemanta.com/ns#"
        },
        /* default rules that are shipped with this service */
        rules : [
                 {
                'left' : [
                    '?subject a zemanta:Recognition',
                    '?subject zemanta:object ?object',
                    '?object owl:sameAs ?entity'
                ],
                'right' : [
                    '?entity zemanta:hasEntityAnnotation ?subject'
                ]
            }
         ]
    };
    /* the options are merged with the default options */
    this.options = jQuery.extend(true, defaults, options ? options : {});

    this.vie = null; /* will be set via VIE.use(); */
    /* overwrite options.name if you want to set another name */
    this.name = this.options.name;
    
    /* basic setup for the ajax connection */
    jQuery.ajaxSetup({
        converters: {"text application/rdf+json": function(s){return JSON.parse(s);}},
        timeout: this.options.timeout
    });
};

VIE.prototype.ZemantaService.prototype = {
    
// ### init()
// This method initializes certain properties of the service and is called
// via ```VIE.use()```.  
// **Parameters**:  
// *nothing*  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.StanbolService}* : The VIE.StanbolService instance itself.  
// **Example usage**:  
//
//     var service = new vie.ZemantaService({<some-configuration>});
//     service.init();
    init: function(){

        for (var key in this.options.namespaces) {
            var val = this.options.namespaces[key];
            this.vie.namespaces.add(key, val);
        }
        
        this.rules = jQuery.extend([], VIE.Util.transformationRules(this));
        this.rules = jQuery.merge(this.rules, (this.options.rules) ? this.options.rules : []);
        
        this.connector = new this.vie.ZemantaConnector(this.options);
        
        /* adding these entity types to VIE helps later the querying */
        this.vie.types.addOrOverwrite('zemanta:EntityAnnotation', [
            /*TODO: add attributes */
        ]).inherit("owl:Thing");
    },

// ### analyze(analyzable)
// This method extracts text from the jQuery element and sends it to Zemanta for analysis.  
// **Parameters**:  
// *{VIE.Analyzable}* **analyzable** The analyzable.  
// **Throws**:  
// *{Error}* if an invalid VIE.Findable is passed.  
// **Returns**:  
// *{VIE.ZemantaService}* : The VIE.ZemantaService instance itself.  
// **Example usage**:  
//
//     var service = new vie.ZemantaService({<some-configuration>});
//     service.analyzable(
//         new vie.Analyzable({element : jQuery("#foo")})
//     );
    analyze: function(analyzable) {
        var service = this;

        var correct = analyzable instanceof this.vie.Analyzable;
        if (!correct) {throw "Invalid Analyzable passed";}

        var element = analyzable.options.element ? analyzable.options.element : jQuery('body');

        var text = service._extractText(element);

        if (text.length > 0) {
            /* query enhancer with extracted text */
            var success = function (results) {
                _.defer(function(){
                    var entities = VIE.Util.rdf2Entities(service, results);
                    analyzable.resolve(entities);
                });
            };
            var error = function (e) {
                analyzable.reject(e);
            };

            this.connector.analyze(text, success, error);

        } else {
            console.warn("No text found in element.");
            analyzable.resolve([]);
        }

    },

    // this private method extracts text from a jQuery element
    _extractText: function (element) {
        if (element.get(0) &&
            element.get(0).tagName &&
            (element.get(0).tagName == 'TEXTAREA' ||
            element.get(0).tagName == 'INPUT' && element.attr('type', 'text'))) {
            return element.get(0).val();
        }
        else {
            var res = element
                .text()    /* get the text of element */
                .replace(/\s+/g, ' ') /* collapse multiple whitespaces */
                .replace(/\0\b\n\r\f\t/g, ''); /* remove non-letter symbols */
            return jQuery.trim(res);
        }
    }
};

// ## VIE.ZemantaConnector(options)
// The ZemantaConnector is the connection between the VIE Zemanta service
// and the actual ajax calls.  
// **Parameters**:  
// *{object}* **options** The options.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.ZemantaService}* : The **new** VIE.ZemantaService instance.  
// **Example usage**:  
//
//     var conn = new vie.ZemantaConnector({<some-configuration>});
VIE.prototype.ZemantaConnector = function (options) {
    this.options = options;
    this.baseUrl = (_.isArray(options.url))? options.url : [ options.url ];
    this.enhancerUrlPrefix = "/";
};

VIE.prototype.ZemantaConnector.prototype = {

// ### analyze(text, success, error, options)
// This method sends the given text to Zemanta returns the result by the success callback.  
// **Parameters**:  
// *{string}* **text** The text to be analyzed.  
// *{function}* **success** The success callback.  
// *{function}* **error** The error callback.  
// *{object}* **options** Options, like the ```format```.  
// **Throws**:  
// *nothing*  
// **Returns**:  
// *{VIE.ZemantaConnector}* : The VIE.ZemantaConnector instance itself.  
// **Example usage**:  
//
//     var conn = new vie.ZemantaConnector(opts);
//     conn.analyze("This is some text.",
//                 function (res) { ... },
//                 function (err) { ... });
    analyze: function(text, success, error, options) {
        if (!options) { options = { urlIndex : 0}; }
        if (options.urlIndex >= this.baseUrl.length) {
            error("Could not connect to the given Zemanta endpoints! Please check for their setup!");
            return;
        }
        
        var enhancerUrl = this.baseUrl[options.urlIndex].replace(/\/$/, '');
        enhancerUrl += this.enhancerUrlPrefix;
        
        var format = options.format || "application/rdf+json";
        
        var retryErrorCb = function (c, t, s, e, o) {
            /* in case a Zemanta backend is not responding and
             * multiple URLs have been registered
             */
            return  function () {
                console.error("Zemanta connection error", arguments);
                c.analyze(t, s, e, _.extend(o, {urlIndex : o.urlIndex+1}));
            };
        }(this, text, success, error, options);
        
        var data = this._prepareData(text);

        if (typeof exports !== "undefined" && typeof process !== "undefined") {
            /* We're on Node.js, don't use jQuery.ajax */
            return this._analyzeNode(enhancerUrl, data, success, retryErrorCb, options, format);
        }

        jQuery.ajax({
            success: function(a, b, c){
	        	var responseData = c.responseText.replace(/<z:signature>.*?<\/z:signature>/, '');
	        	success(responseData);
            },
            error: retryErrorCb,
            type: "POST",
            url: enhancerUrl,
            data: data
        });
    },

    _analyzeNode: function(url, text, success, errorCB, options, format) {
        var request = require('request');
        var r = request({
            method: "POST",
            uri: url,
            body: text,
            headers: {
                Accept: format
            }
        }, function(error, response, body) {
            try {
                success({results: JSON.parse(body)});
            } catch (e) {
                errorCB(e);
            }
        });
        r.end();
    },
    
    _prepareData : function (text) {
        return {
            method: 'zemanta.suggest_markup',
            format: 'rdfxml',
            api_key: this.options.api_key,
            text: text,
            return_rdf_links: 1
            // for more options check http://developer.zemanta.com/docs/suggest/
        };
    }
};
})();

if (!VIE.prototype.view) {
    VIE.prototype.view = {};
}

VIE.prototype.view.Collection = Backbone.View.extend({
    // Ensure the collection view gets updated when items get added or removed
    initialize: function() {
        this.template = this.options.template;
        this.service = this.options.service;
        if (!this.service) {
            throw "No RDFa service provided to the Collection View";
        }
        this.owner = this.options.owner;
        this.entityViews = {};
        _.bindAll(this, 'addItem', 'removeItem', 'refreshItems');
        this.collection.bind('add', this.addItem);
        this.collection.bind('remove', this.removeItem);

        // Make the view aware of existing entities in collection
        var view = this;
        this.collection.forEach(function(entity) {
            view.registerItem(entity, view.collection);
        });
    },

    addItem: function(entity, collection) {
        if (collection !== this.collection) {
            return;
        }

        if (!this.template || this.template.length === 0) {
            return;
        }

        var entityView = this.service._registerEntityView(entity, this.cloneElement(this.template, entity));
        var entityElement = jQuery(entityView.render().el);
        if (entity.id) {
            this.service.setElementSubject(entity.getSubjectUri(), entityElement);
        }

        // TODO: Ordering
        jQuery(this.el).append(entityElement);

        // Ensure we catch all inferred predicates. We add these via JSONLD
        // so the references get properly Collectionized.
        var service = this.service;
        entityElement.parent('[rev]').each(function() {
            var predicate = jQuery(this).attr('rev');
            var relations = {};
            relations[predicate] = new service.vie.Collection();
            relations[predicate].addOrUpdate(service.vie.entities.get(service.getElementSubject(this)));
            entity.set(relations);
        });
        
        this.trigger('add', entityView);
        this.entityViews[entity.cid] = entityView;
        entityElement.show();
    },

    registerItem: function(entity, collection) {
        var element = this.service.getElementBySubject(entity.id, this.el);
        if (!element) {
            return;
        }
        var entityView = this.service._registerEntityView(entity, element);
        this.entityViews[entity.cid] = entityView;
    },

    removeItem: function(entity) {
        if (!this.entityViews[entity.cid]) {
            return;
        }

        this.trigger('remove', this.entityViews[entity.cid]);
        jQuery(this.entityViews[entity.cid].el).remove();
        delete(this.entityViews[entity.cid]);
    },

    refreshItems: function(collection) {
        var view = this;
        jQuery(this.el).empty();
        collection.forEach(function(entity) {
            view.addItem(entity, collection);
        });
    },

    cloneElement: function(element, entity) {
        var newElement = jQuery(element).clone(false);
        var service = this.service;
        if (newElement.attr('about') !== undefined) {
            // Direct match with container
            newElement.attr('about', '');
        }
        newElement.find('[about]').attr('about', '');
        var subject = this.service.getElementSubject(newElement);
        service._findPredicateElements(subject, newElement, false).each(function() {
            var predicate = service.getElementPredicate(jQuery(this));
            if (entity.get(predicate) && entity.get(predicate).isCollection) {
              return true;
            }
            service.writeElementValue(null, jQuery(this), '');
        });
        return newElement;
    }
});
if (!VIE.prototype.view) {
    VIE.prototype.view = {};
}

VIE.prototype.view.Entity = Backbone.View.extend({
    initialize: function(options) {
        this.service = options.service ? options.service : 'rdfa';
        this.vie = options.vie;

        // Ensure view gets updated when properties of the Entity change.
        _.bindAll(this, 'render');
        this.model.bind('change', this.render);
    },

    // Rendering a view means writing the properties of the Entity back to
    // the element containing our RDFa annotations.
    render: function() {
        this.vie.save({
                element: this.el, 
                entity: this.model
            }).
            to(this.service).
            execute();
        return this;
    }
}); 
// Based on [Julian Aubourg's xdr.js](https://github.com/jaubourg/ajaxHooks/blob/master/src/ajax/xdr.js)  
// Internet Explorer 8 & 9 don't support the cross-domain request protocol known as CORS. 
// Their solution we use is called XDomainRequest. This module is a wrapper for 
// XDR using jQuery ajaxTransport, jQuery's way to support such cases.
// Author: Szaby Grünwald @ Salzburg Research, 2011
var root = this;
(function( jQuery ) {

if ( root.XDomainRequest ) {
	jQuery.ajaxTransport(function( s ) {
		if ( s.crossDomain && s.async ) {
			if ( s.timeout ) {
				s.xdrTimeout = s.timeout;
				delete s.timeout;
			}
			var xdr;
			return {
				send: function( _, complete ) {
					function callback( status, statusText, responses, responseHeaders ) {
						xdr.onload = xdr.onerror = xdr.ontimeout = jQuery.noop;
						xdr = undefined;
						complete( status, statusText, responses, responseHeaders );
					}
					xdr = new XDomainRequest();
					// For backends supporting header_* in the URI instead of real header parameters,
					// use the dataType for setting the Accept request header. e.g. Stanbol supports this.
					if(s.dataType){
					    var headerThroughUriParameters = "header_Accept=" + encodeURIComponent(s.dataType);
					    s.url = s.url + (s.url.indexOf("?") === -1 ? "?" : "&" ) + headerThroughUriParameters;
					}
					xdr.open( s.type, s.url );
					xdr.onload = function(e1, e2) {
						callback( 200, "OK", { text: xdr.responseText }, "Content-Type: " + xdr.contentType );
					};
					// XDR cannot differentiate between errors, 
					// we call every error 404. Could be changed to another one.
					xdr.onerror = function(e) {
					    console.error(JSON.stringify(e));
						callback( 404, "Not Found" );
					};
					if ( s.xdrTimeout ) {
						xdr.ontimeout = function() {
							callback( 0, "timeout" );
						};
						xdr.timeout = s.xdrTimeout;
					}
					xdr.send( ( s.hasContent && s.data ) || null );
				},
				abort: function() {
					if ( xdr ) {
						xdr.onerror = jQuery.noop();
						xdr.abort();
					}
				}
			};
		}
	});
}
})( jQuery );})();
