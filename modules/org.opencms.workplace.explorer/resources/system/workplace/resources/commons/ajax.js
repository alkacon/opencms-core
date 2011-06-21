/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * This method executes an AJAX request to the given url.<p>
 *
 * Normally you have to call this method using following syntax:<br>
 * <code>makeRequest('url', 'param1=value1&...', 'method')</code><p>
 *
 * Parameters are passed as content using a post request.<p>
 *
 * The given <code>method</code> is used to communicate the result of the request.<p>
 *
 * The method parameter needs to have following signature:<br>
 * <code>method(result, state);</code><p>
 *
 * Where the <code>state</code> can have following values:<br>
 * <code>'ok'</code>   : Everything went fine and the result of the request is given in the <code>result</code> parameter.<br>
 * <code>'fatal'</code>: The AJAX XmlHttpRequest object could not be created, the <code>result</code> parameter is empty.<br>
 * <code>'wait'</code> : The AJAX request was successfully send, so please wait, the <code>result</code> parameter is empty.<br>
 * <code>'error'</code>: The AJAX request was successfully send, but the response status was not ok (different to 200),
 *                       The <code>result</code> parameter is the received status code.<br>
 */
function makeRequest(url, params, method) {

   var http_request = false;
   if (window.XMLHttpRequest) { // Mozilla, Safari, ...
      http_request = new XMLHttpRequest();
      if (http_request.overrideMimeType) {
         http_request.overrideMimeType('text/xml');
      }
   } else if (window.ActiveXObject) { // IE
      try {
         http_request = new ActiveXObject("Msxml2.XMLHTTP");
      } catch (e) {
         try {
            http_request = new ActiveXObject("Microsoft.XMLHTTP");
         } catch (e) {}
      }
   }
   if (!http_request && navigator.userAgent.toLowerCase().indexOf("msie") != -1) {
   	  // for IE browsers without ActiveX enabled, use the <iframe> workaround
      try {
         http_request = new XMLHttpRequestIE();
      } catch (e) {}
   }
   if (!http_request) {
      updateContents('fatal', eval(method));
      return false;
   }
   updateContents('wait', eval(method));
   http_request.onreadystatechange = function() { updateContents(http_request, eval(method)); };
   http_request.open('POST', url, true);
   try {
   	  http_request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
   	  http_request.setRequestHeader("Content-length", params.length);
   } catch (e) {}
   http_request.send(params);
}

/**
 * Help method for AJAX request.<p>
 *
 * see #makeRequest(url, params, method)
 */
function updateContents(http_request, method) {

   try {
      if (http_request == 'fatal' || http_request == 'wait') {
         method('', http_request);
         return;
      }
   } catch (e) {
      // ignore type validation error
   }
   if (http_request.readyState != 4) {
      // ignore if request still not ready
   } else if (http_request.status != 200) {
   	  method(http_request.status, 'error');
   } else {
      method(http_request.responseText, 'ok');
   }
}

/*
The following code is a modified version of a request without using ActiveX objects with IE
Changes to the original code are:
- document => XMLHttpRequestIE_getDoc() (for OpenCms frameset, uses "foot" frame)
- added request parameters to iframe src (document.getElementById('kXHR_iframe_'+this.i).src=this.url +params;)
- changed responseText using innerText to innerHTML, because we are always submitting html code
- renamed functions, added "IE" suffix to keep original code working in Mozilla and other browsers

coded by Kae - http://verens.com/
use this code as you wish, but retain this notice
*/

var kXHR_instances=0;
var kXHR_objs=[];

XMLHttpRequestIE=function(){
	var i=0;
	var url='';
	var responseText='';
	var iframe='';
	this.onreadystatechange=function(){
		return false;
	}
	this.open=function(method,url){
		//TODO: POST methods
		this.i=++kXHR_instances; // id number of this request
		this.url=url;
		XMLHttpRequestIE_getDoc().body.appendChild(XMLHttpRequestIE_getDoc().createElement('<iframe id="kXHR_iframe_'+this.i+'" style="display:none" src="/"></iframe>'));
	}
	this.send=function(postdata){
		//TODO: use the postdata, request parameters are working...
		var params = "";
		if (postdata.length > 0) {
			params = "?" + postdata;
		}
		XMLHttpRequestIE_getDoc().getElementById('kXHR_iframe_'+this.i).src=this.url + params;
		kXHR_objs[this.i]=this;
		setTimeout('XMLHttpRequestIE_checkState('+this.i+',2)',2);
	}
	return true;
}

XMLHttpRequestIE_checkState=function(inst,delay){
	var el=XMLHttpRequestIE_getDoc().getElementById('kXHR_iframe_'+inst);
	if(el.readyState=='complete'){
		var responseText=XMLHttpRequestIE_getDoc().frames['kXHR_iframe_'+inst].document.body.innerHTML;
		kXHR_objs[inst].responseText=responseText;
		kXHR_objs[inst].readyState=4;
		kXHR_objs[inst].status=200;
		kXHR_objs[inst].onreadystatechange();
		el.parentNode.removeChild(el);
	}else{
		delay*=1.5;
		setTimeout('XMLHttpRequestIE_checkState('+inst+','+delay+')',delay);
	}
}

function XMLHttpRequestIE_getDoc() {
	if (top.frames['foot'] != null) {
		return top.frames['foot'].document;
	}
	return document;
}