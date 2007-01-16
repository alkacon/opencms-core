/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.workplace.explorer/resources/system/workplace/resources/commons/ajax.js,v $
 * Date   : $Date: 2007/01/16 09:19:47 $
 * Version: $Revision: 1.1.2.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * Params are passed as content using a post request.<p>
 *
 * The given method is used to communicate the result of the request.<p>
 *
 * The method needs to have following signature:<br>
 * <code>method(result, params, state);</code><p>
 *
 * Normally you have to call this method using following syntax:<br>
 * <code>makeRequest('url', 'param1=value1&...','method')</code><p>
 *
 * Where the <code>state</code> can have following values:<br>
 * <code>'ok'</code>   : Everything went fine and the result of the request is given in the <code>result</code> parameter.<br>
 * <code>'fatal'</code>: The AJAX XmlHttpRequest object could not be created, the <code>result</code> parameter is empty.<br>
 * <code>'wait'</code> : The AJAX request was successfully send, so please wait, the <code>result</code> parameter is empty.<br>
 * <code>'error'</code>: The AJAX request was successfully send, but the response status was not ok (code 200), 
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
   if (!http_request) {
      updateContents('fatal', eval(method));
      return false;
   }
   updateContents('wait', eval(method));
   http_request.onreadystatechange = function() { updateContents(http_request, eval(method)); };
   http_request.open('POST', url, true);
   http_request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
   http_request.setRequestHeader("Content-length", params.length);
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
