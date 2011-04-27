/*
 * File   : $Source: /alkacon/cvs/opencms/modules/org.opencms.workplace/resources/system/workplace/resources/components/widgets/dialog-helper.js,v $
 * Date   : $Date: 2011/04/27 19:03:06 $
 * Version: $Revision: 1.5 $
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
 * Depends on jquery and jquery-ui. Make sure to include both into the page.
 */

function _getScaleParam(sitePath){
	if (sitePath.indexOf('?')!=-1){
		var param = sitePath.substr(sitePath.indexOf('?'));
		var i=param.indexOf("__scale=");
		if (i!=-1){
			return (param.indexOf('&')!=-1) ? param.substring(i+8, param.indexOf('&')) : param.substr(i+8);
		} 
	}
	return null;
}

function _getScaleValue(scale, valueName) {
	if (scale == null) {
		return null;
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
	return null;
}

/**
 * Closes the dialog with the given id.<p>
 * 
 * @param fieldId the field id
 * 
 * @return void
 */
function cmsCloseDialog(fieldId){
	
	$(document.getElementById("cms_dialog_"+fieldId)).dialog('destroy').remove();
}

/**
 * Opens a modal iFrame dialog with the given parameters.<p>
 * Call #cmsCloseDialog(fieldId) to close the dialog again.<p>
 * 
 * @param title the dialog title
 * @param dialogUrl the iFrame URL
 * @param fieldId the field id
 * @param height the dialog height
 * @param width the dialog width
 * 
 * @return void 
 */
function cmsOpenDialog(title, dialogUrl, fieldId, height, width){
	var _dialogElementId="cms_dialog_"+fieldId;
	if (!(document.getElementById(_dialogElementId))){
		var _iframe = $('<iframe/>', {
            'src': dialogUrl,
            'name': 'cms_iframe_' + fieldId,
            'css': {
               'width': '100%',
               'height': '100%',
               'border': 'none',
               'overflow': 'hidden'
            },
            'frameborder': '0',
            'framespacing': '0'
         });
         var _iframeBox = $('<div/>').appendTo(document.body);
         _iframeBox.css({
            'width': '100%',
            'height': height+'px',
            'background-color': 'transparent',
            'padding': '0px',
            'overflow': 'visible'
         });
         
         // new code
         _iframeBox.attr('id', _dialogElementId);
         _iframeBox.dialog({
            dialogClass: 'galleryDialog hideCaption',
            modal: true,
            zIndex: 99999,
            close: function() {
               _iframeBox.dialog('destroy').remove();
               
            },
            open: function() {
               _iframeBox.append(_iframe);
               _iframeBox.closest('.galleryDialog').css('overflow', 'visible');
            },
            resizable: false,
            autoOpen: true,
            width: width + 4 + ($.browser.msie ? 6 : 0),
            // height: height+ 27 + ($.browser.msie ? 11 : 0)
            height: height -2
         });
	}
}

/**
 * Opens a modal image preview.<p>
 * 
 * @param title the dialog title
 * @param context the context path
 * @param sitePath the site path of the resource to preview
 * 
 * @return void
 */
function cmsOpenImagePreview(title, context, fieldId){
	var sitePath=document.getElementById(fieldId).getAttribute('value');
	if (sitePath && $.trim(sitePath).charAt(0)=='/'){
		sitePath=$.trim(sitePath);
		var _dialogWidth=null;
		var _boxWidth=null;
		var _resizable=false;
		if ($.browser.msie){
			// for IE dialog width 'auto' will not work, so try to read scaling parameter to detect image width
			var _scale=_getScaleParam(sitePath);
			if (_scale){
				_dialogWidth=_getScaleValue(_scale, 'w');
			}
			if (_dialogWidth==null){
				// no scale parameters found, use default width
				_dialogWidth=650;
				// enable resize on dialog
				_resizable=true;
			}else{
				_dialogWidth+=6;
			}
			
			_boxWidth=_dialogWidth-6;
		}else{
			_dialogWidth='auto';
			_boxWidth=_dialogWidth;
		}
		var _previewImage=$('<img />', {
			'src': context + sitePath,
			'title': sitePath,
			'alt': sitePath
		});
		var _imageBox=$('<div />', {'css':{
		    'width': _boxWidth,
	            'background-color': 'white',
	            'padding': '6px',
	            'text-align': 'center',
	            '-moz-border-radius': '8px',
	            '-webkit-border-radius': '8px',
	            'border-radius': '8px',
	            'border-radius': '8px'
	        }
		}).append(_previewImage).appendTo(document.body);
		_imageBox.dialog({
            /** title: title, */
	    dialogClass: 'galleryDialog hideCaption',
            modal: true,
            zIndex: 99999,
            open: function(){
            	if ($.browser.msie){
            		_imageBox.css('width', _boxWidth);
            		_imageBox.parent().css('padding','2px');
            	}
            	_imageBox.closest('.galleryDialog').css('overflow', 'visible');
            	_imageBox.closest('.galleryDialog').css('margin-top', '20px');
            },
            close: function() {
				_imageBox.dialog('destroy').remove();
            },
            resizable: _resizable,
            resize: function(event, ui){
            	_imageBox.css('width', _imageBox.parent().width() );
            },
            autoOpen: true,
            width: _dialogWidth,
            maxWidth: 900,
            maxHeight: 700
         });
	 
	     _previewImage.load(function() {
	         _imageBox.dialog( "option", "position", 'center' );
	     });
	}
}



/**
 * Opens a modal preview dialog.<p>
 * 
 * @param title the dialog title
 * @param context the context path
 * @param sitePath the site path of the resource to preview
 * 
 * @return void
 */
function cmsOpenPreview(title, context,  fieldId){
	var sitePath=document.getElementById(fieldId).getAttribute('value');
	if (sitePath && sitePath.trim().charAt(0)=='/'){
		cmsOpenDialog(title, context+sitePath.trim(), 'preview', 650, 750);
	}
}