/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.workplace/resources/system/workplace/resources/components/widgets/defaultadvancedgallery.js,v $
 * Date   : $Date: 2010/01/21 08:14:23 $
 * Version: $Revision: 1.13 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2008 Alkacon Software GmbH (http://www.alkacon.com)
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
/*
 * When using this script to open the default advanced gallery dialog, be sure to
 * initialize the context path (e.g. "/opencms/opencms") and gallery path in the opener properly.
 *
 */
var defaultAdvancedGalleryPath;
var requestData;
var ade = {};
(function(ade) {
   // closes the gallery window 
   ade.closeGallery=function(win, fieldId) {
      win.$(win.document.getElementById(fieldId + '-gallery')).dialog('destroy').remove();
   }
   
   // opens the default advanced gallery popup window
   ade.openDefaultAdvancedGallery = function(dialogMode, fieldId, idHash) {
      requestData = {};
      // standard parameter from the xml configuration
      // startup param as string
      var startupFolder = eval('startupFolder' + idHash);
      // startup param as array    
      var startupFolders = eval('startupFolders' + idHash);
      
      // start type from the configuration could be 'gallery' or 'category'
      var startupType = eval('startupType' + idHash);
      var resourceTypes = eval('resourceTypes' + idHash);
      
      // value of the selected resource in the input field including scale parameter
      var scalePath = null;
      if (fieldId != null && fieldId != "" && fieldId != 'null') {
         var itemField = window.document.getElementById(fieldId);
         if (itemField.value != null && itemField.value != '') {
            scalePath = itemField.value;
         }
      }
      
      //alert(JSON.stringify([startupFolder, startupFolders, startupType, resourceTypes]));
      var searchKeys = {
         'category': 'categories',
         'gallery': 'galleries'
      };
      
      // Json object as request parameter for standard gallery
      // if input field is not empty
      if (scalePath) {
         requestData['resourcepath'] = removeParamFromPath(scalePath);
         requestData['types'] = resourceTypes;
         // id input field is empty
      } else {
         requestData = {
            'querydata': {},
            'types': resourceTypes
         };
         requestData['querydata']['types'] = resourceTypes;
         requestData['querydata']['galleries'] = [];
         requestData['querydata']['categories'] = [];
         requestData['querydata']['matchesperpage'] = 8;
         requestData['querydata']['query'] = '';
         requestData['querydata']['tabid'] = 'tabs-result';
         requestData['querydata']['page'] = 1;
         // check the 
         if (startupFolder != null) {
            requestData['querydata'][searchKeys[startupType]] = [startupFolder];
         } else if (startupFolders != null) {
            requestData['querydata'][searchKeys[startupType]] = startupFolders;
         }
      }
      
      var paramString = "dialogmode=" + dialogMode;
      paramString += "&fieldid=" + fieldId;
      paramString += "&path=" + removeParamFromPath(scalePath);
      paramString += "&data=" + JSON.stringify(requestData);
      
      // additional parameter for the image resource type
      var initialImageInfos = '';
      if ($.inArray(3, resourceTypes) != -1) {
         var scaleParam = extractScaleParam(scalePath);
         var imgWidth = "";
         var imgHeight = "";
         if (scaleParam != null) {
            imgWidth = getScaleValue(scaleParam, "w");
            imgHeight = getScaleValue(scaleParam, "h");
         }
         // additional infos for image gallery
         initialImageInfos = {
            "widgetmode": "simple",
            "useformats": false,
            "showformats": true,
            "scale": scaleParam,
            "imgwidth": imgWidth,
            "imgheight": imgHeight
         };
         paramString += "&imagedata=" + JSON.stringify(initialImageInfos);
      }
      var galleryWidgetUri = contextPath + defaultAdvancedGalleryPath + paramString;
      var galleryId = fieldId + '-gallery';
      var galleryElem = document.getElementById(galleryId);
      if (!galleryElem) { // make sure the gallery isn't opened twice
         var $iframe = $('<iframe/>', {
             'src': galleryWidgetUri,
             'name': 'gallery-iframe',
             'css': {
                 'width': '100%',
                 'height': '100%',
                 'border': 'none',
                 'overflow': 'hidden'
             },
             'frameborder': '0',
             'framespacing': '0'
         });
         var $iframeBox = $('<div/>').appendTo(document.body);
         $iframeBox.css({
            'width': '100%',
            'height': '490px',
            'background-color': 'white',
            'padding': '0px'
         });
         
         // new code
         $iframeBox.attr('id', galleryId)
         $iframeBox.dialog({
            title: 'Gallery',
            modal: true,
            zIndex: 99999,
            close: function() {
               $iframeBox.dialog('destroy').remove();
               
            },
            open: function() {
               $iframeBox.append($iframe);
            },
            resizable: false,
            autoOpen: true,
            width: 654 + ($.browser.msie ? 6 : 0),
            height: 517 + ($.browser.msie ? 11 : 0)
         
         });
      }
   }
   
   // opens a preview popup window to display the currently selected download
   ade.previewDefault=function (fieldId) {
      var downUri = document.getElementById(fieldId).value;
      downUri = downUri.replace(/ /, "");
      if ((downUri != "") && (downUri.charAt(0) == "/")) {
         treewin = window.open(contextPath + downUri, "opencms", 'toolbar=no,location=no,directories=no,status=yes,menubar=0,scrollbars=yes,resizable=yes,top=20,left=150,width=653,height=605');
      }
   }
   
   /* extracts the scale parameter from the imagepath if available */
   function extractScaleParam(pathWithParam) {
      var path = "";
      if (pathWithParam != null && pathWithParam != '') {
         var index = pathWithParam.indexOf("?__scale=");
         if (index == -1) {
            path = path;
         } else {
            path = pathWithParam.substring(index + 9);
         }
      }
      return path;
   }
   
   /* removes scale parameter from the image path if available. */
   function removeParamFromPath(pathWithParam) {
      var path = "";
      if (pathWithParam != null && pathWithParam != '') {
         var index = pathWithParam.indexOf("?__scale=");
         if (index == -1) {
            path = pathWithParam;
         } else {
            path = pathWithParam.substring(0, eval(index));
         }
      }
      return path;
   }
   
   /* Returns the value of the specified scale parameter. */
   /* Copy from vfsimage.js*/
   function getScaleValue(scale, valueName) {
      if (scale == null) {
         return "";
      }
      var pos = scale.indexOf(valueName + ":");
      if (pos != -1) {
         // found value, return it
         if (pos > 0 && (valueName == "h" || valueName == "w")) {
            // special handling for "w" and "h", could also match "cw" and "ch"
            if (scale.charAt(pos - 1) == "c") {
               scale = scale.substring(pos + 1);
            }
         }
         var searchVal = new RegExp(valueName + ":\\d+,*", "");
         var result = scale.match(searchVal);
         if (result != null && result != "") {
            result = result.toString().substring(valueName.length + 1);
            if (result.indexOf(",") != -1) {
               result = result.substring(0, result.indexOf(","));
            }
            return result;
         }
      }
      return "";
   }
})(ade);
var openDefaultAdvancedGallery = ade.openDefaultAdvancedGallery;
var previewDefault = ade.previewDefault;
var closeGallery = ade.closeGallery;
