/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsPreviewUtil.java,v $
 * Date   : $Date: 2010/06/02 14:46:36 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.galleries.client.preview;

/**
 * Utility class for resource preview.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public final class CmsPreviewUtil {

    /** The fck editor key for js. */
    static final String KEY_FCKEDITOR = "fckeditor";

    /** The fck editor key for js. */
    static final String KEY_FCKEDITOR_EDITOR = "editor";

    /** The fck editor key for js. */
    static final String KEY_FCKEDITOR_FCK = "fck";

    /** The fck editor key for image gallery enhanced options. */
    static final String KEY_IS_ENHANCED_OPTS = "isEnhanced";

    /** The fck editor key for image gallery enhanced options. */
    static final String KEY_USE_TB_LINK_ORG = "useTbForLinkOriginal";

    /**
     * Constructor.<p>
     */
    private CmsPreviewUtil() {

        // hiding the constructor
    }

    /**
     * Exports the functions of {@link org.opencms.ade.galleries.client.preview.I_CmsResourcePreview}
     * to the window object for use via JSNI.<p> 
     * 
     * @param previewName the name of the preview
     * @param preview the preview
     */
    public static native void exportFunctions(String previewName, I_CmsResourcePreview preview) /*-{
        var listKey=@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::KEY_PREVIEW_PROVIDER_LIST;
        if (!$wnd[listKey]){
        $wnd[listKey]={};
        }
        $wnd[listKey][previewName]={};
        $wnd[listKey][previewName][@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::KEY_OPEN_PREVIEW_FUNCTION]=function(mode, path, parentElementId){
        preview.@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::openPreview(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(mode, path, parentElementId);
        };
        $wnd[listKey][previewName][@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::KEY_SELECT_RESOURCE_FUNCTION]=function(mode, path){
        preview.@org.opencms.ade.galleries.client.preview.I_CmsResourcePreview::selectResource(Ljava/lang/String;Ljava/lang/String;)(mode, path);
        };
    }-*/;

    /**
     * Sets the path of the selected resource in the input field of the xmlcontent.<p>
     * 
     * Widget mode: Use this function inside the xmlcontent. 
     * 
     * @param fieldId the field id to identify the input field
     * @param path the path to the selected resource
     */
    public static native void setResourcePath(String fieldId, String path) /*-{
        //the id of the input field in the xml content
        var fieldId = fieldId;

        if (fieldId != null && fieldId != "") {
        var imgField = $wnd.parent.document.getElementById(fieldId);
        imgField.value = itemId;
        try {
        // toggle preview icon if possible
        $wnd.parent.checkPreview(fieldId);
        } catch (e) {}
        }
        $wnd.parent.closeGallery($wnd.parent, fieldId);
    }-*/;

    // TODO: from hert remove!!!!!!    

    /**
     * Initialize the FCKeditor variables from editor and sets them to the window object.<p>
     * 
     * All gallery modes. This function should be called in order to use the fck editor reference from native js. 
     */
    public static native void readFck() /*-{
        var fckedior = @org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR; 
        var oEditor = $wnd.parent.InnerDialogLoaded();

        $wnd[fckedior][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_EDITOR] = oEditor;
        $wnd[fckedior][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_FCK] = oEditor.FCK;
    }-*/;

    /**
     * Reads the enhanced options and set them to the window object.<p>
     */
    public static native void readEnhancedMode() /*-{
        var fckeditor = @org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR; 
        var oEditor = $wnd[fckeditor][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_EDITOR];

        // Enables or disables the enhanced image dialog options.
        $wnd[fckeditor][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_IS_ENHANCED_OPTS] = oEditor.FCKConfig.ShowEnhancedOptions;
        $wnd[fckeditor][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_USE_TB_LINK_ORG] = oEditor.FCKConfig.UseTbForLinkOriginal;
    }-*/;

    /**
     * Enables and shows the ok-button of the fck editor.<p>
     * 
     * Editor, download gallery, from integrator.js
     * Old name: activeItemAdditionalActions
     */
    public static native void showOkButton() /*-{
        $wnd.parent.SetOkButton(true);
    }-*/;

    /**
     *  Triggers the ok- button of the fck editor.<p> 
     *  
     *  Editor mode, download gallery, image gallery.
     */
    public static native void triggerOkEvent() /*-{
        $wnd.parent.Ok();
    }-*/;

    /**
     * Additional editor actions on image load.<p>
     * 
     * Editor mode, imagegallery.
     * FCK API!
     * 
     * @param isInitial true, if the gallery is just loaded
     */
    public static native void activeImageAdditionalActions(boolean isInitial) /*-{
        if (isInitial) {
        loadSelection();
        } else {
        if (isEnhanced()) {
        resetCopyrightText();
        }        
        var imgTitle = cms.galleries.activeItem.title;
        if (cms.galleries.activeItem.description != "") {
        imgTitle = cms.galleries.activeItem.description;
        }
        GetE("txtAlt").value = imgTitle;
        //set default values for the image opened from the list
        $('#' + cms.imagepreviewhandler.keys['editorFormatTabId'])
        .find('#' + cms.imagepreviewhandler.editorKeys['imgAlign'])
        .find('.cms-selectbox').selectBox('setValue','left');        
        GetE("txtHSpace").value = "5";
        GetE("txtVSpace").value = "5";
        cms.galleries.checkGalleryCheckbox($('#' + cms.imagepreviewhandler.editorKeys['imgSpacing']),true);        
        }
        // activate the "OK" button of the dialog
        $wnd.parent.SetOkButton(true);
    }-*/;

    // TODO: where are these function used? remove
    /**
     * Opens the file browser popup for the link dialog.<p>
     * 
     * Editor mode, imagegallery.
     * FCK API!
     * 
     */
    public static native void LnkBrowseServer() /*-{
        //OpenFileBrowser(FCKConfig.LinkBrowserURL, FCKConfig.LinkBrowserWindowWidth, FCKConfig.LinkBrowserWindowHeight);
    }-*/;

    // TODO: where are these function used? remove
    /**
     * Triggered by the file browser popup to set the selected URL in the input field.
     * 
     * Editor mode, imagegallery.
     * FCK API!
     */
    public static native void SetUrl(String url, String width, String height, String alt) /*-{
        GetE("txtLnkUrl").value = url;
    }-*/;

    /**
     * Triggered by the file browser popup to set the selected URL in the input field.
     * 
     * Editor mode, imagegallery.
     * FCK API!
     */
    // TODO: jquery dependences
    public static native void prepareEditorForImage() /*-{
        var dialog = $wnd.parent;        

        // get the selected image
        oImage = dialog.Selection.GetSelectedElement();
        if (oImage && oImage.tagName != "IMG" && oImage.tagName != "SPAN" && !(oImage.tagName == "INPUT" && oImage.type == "image")) {
        oImage = null;
        }
        // get the active link
        oLink = dialog.Selection.GetSelection().MoveToAncestorNode("A");

        if (!oImage) {       
        return;
        }

        var sUrl = oImage.getAttribute("_fcksavedurl");
        if (sUrl == null) {
        sUrl = GetAttribute(oImage, "src", "");
        }
        var paramIndex = sUrl.indexOf("?__scale");
        if (paramIndex != -1) {
        cms.galleries.initValues.scale = sUrl.substring(paramIndex + 9);
        sUrl = sUrl.substring(0, paramIndex);
        }

        cms.galleries.initValues.linkpath = sUrl;

        var iWidth, iHeight;

        var regexSize = /^\s*(\d+)px\s*$/i ;

        if (oImage.style.width) {
        var aMatch = oImage.style.width.match(regexSize);
        if (aMatch) {
        iWidth = aMatch[1];
        oImage.style.width = "";
        }
        }

        if (oImage.style.height) {
        var aMatch = oImage.style.height.match(regexSize);
        if (aMatch) {
        iHeight = aMatch[1];
        oImage.style.height = "";
        }
        }

        iWidth = iWidth ? iWidth : GetAttribute(oImage, "width", "");
        iHeight = iHeight ? iHeight : GetAttribute(oImage, "height", "");

        cms.galleries.initValues.imgwidth = "" + iWidth;
        cms.galleries.initValues.imgheight = "" + iHeight;
    }-*/;

    /**
     * Loads the selected image from the editor, if available.
     * 
     * Editor mode, imagegallery.
     * FCK API!
     */
    // TODO: replace old varibles with $wnd[fckeditor]
    // TODO: remove jquery layout actions
    public static native void loadSelection() /*-{
        var dialog = $wnd.parent;
        var fckeditor = @org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR;        
        var oEditor = $wnd[fckeditor][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_EDITOR];
        var FCK = $wnd[fckeditor][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_FCK];

        var altText = "";
        var copyText = "";
        var imgBorder = false;
        var imgHSp = "";
        var imgVSp = "";
        var imgAlign = GetAttribute(oImage, "align", "");
        if (dialog.Selection.GetSelection().HasAncestorNode("SPAN") || dialog.Selection.GetSelection().HasAncestorNode("TABLE")) {
        if (FCK.Selection.HasAncestorNode("SPAN")) {
        oSpan = dialog.Selection.GetSelection().MoveToAncestorNode("SPAN");
        } else {
        oSpan = dialog.Selection.GetSelection().MoveToAncestorNode("TABLE");
        }
        try {
        var idPart = oSpan.getAttribute("id").substring(1);
        if (idPart == oImage.getAttribute("id").substring(1)) {

        var altElem = oEditor.FCK.EditorDocument.getElementById("s" + idPart);
        if (altElem) {
        altText = altElem.firstChild.data;
        cms.galleries.checkGalleryCheckbox($('#' + cms.imagepreviewhandler.editorKeys['fckInsertAlt']), true);                   
        }

        var cpElem = oEditor.FCK.EditorDocument.getElementById("c" + idPart);
        if (cpElem) {
        copyText = cpElem.firstChild.data;
        cms.galleries.checkGalleryCheckbox($('#' + cms.imagepreviewhandler.editorKeys['fckInsertCr']), true);                   
        }
        var divElem = oEditor.FCK.EditorDocument.getElementById("a" + idPart);
        imgHSp = divElem.style.marginLeft;
        if (imgAlign == "left") {
        imgHSp = divElem.style.marginRight;
        } else if (imgAlign == "right") {
        imgHSp = divElem.style.marginLeft;
        }
        imgVSp = divElem.style.marginBottom;
        }
        } catch (e) {}
        } else {
        if (imgAlign == "left") {
        imgHSp = oImage.style.marginRight;
        imgVSp = oImage.style.marginBottom;
        if (imgHSp == "") {
        imgHSp = GetAttribute(oImage, "hspace", "");
        }
        if (imgVSp == "") {
        imgVSp = GetAttribute(oImage, "vspace", "");
        }
        } else if (imgAlign == "right") {
        imgHSp = oImage.style.marginLeft;
        imgVSp = oImage.style.marginBottom;
        if (imgHSp == "") {
        imgHSp = GetAttribute(oImage, "hspace", "");
        }
        if (imgVSp == "") {
        imgVSp = GetAttribute(oImage, "vspace", "");
        }
        } else {
        imgHSp = GetAttribute(oImage, "hspace", "");
        imgVSp = GetAttribute(oImage, "vspace", "");
        }
        }
        var cssTxt = oImage.style.cssText;
        if (showEnhancedOptions) {
        if (imgAlign == "left") {
        cssTxt = cssTxt.replace(/margin-right:\s*\d+px;/, "");
        cssTxt = cssTxt.replace(/margin-bottom:\s*\d+px;/, "");
        } else if (imgAlign == "right") {
        cssTxt = cssTxt.replace(/margin-left:\s*\d+px;/, "");
        cssTxt = cssTxt.replace(/margin-bottom:\s*\d+px;/, "");
        }
        }

        if (altText == "") {
        altText = GetAttribute(oImage,  "alt", "");
        }

        // at this point the url is already given
        var sUrl = cms.galleries.initValues.linkpath;



        GetE("txtAlt").value = altText;
        if (copyText != "") {
        GetE("txtCopyright").value = copyText;
        }

        if (isNaN(imgHSp) && imgHSp.indexOf("px") != -1)    {   
        imgHSp = imgHSp.substring(0, imgHSp.length - 2);
        }
        if (isNaN(imgVSp) && imgVSp.indexOf("px") != -1)    {   
        imgVSp = imgVSp.substring(0, imgVSp.length - 2);
        }

        if (imgHSp != "" || imgVSp != "") {
        imgBorder = true;
        }


        if (imgBorder) {
        GetE("txtVSpace").value = imgVSp;
        GetE("txtHSpace").value = imgHSp;
        cms.galleries.checkGalleryCheckbox($('#' + cms.imagepreviewhandler.editorKeys['imgSpacing']), true);        
        }

        $('#' + cms.imagepreviewhandler.keys['editorFormatTabId'])
        .find('#' + cms.imagepreviewhandler.editorKeys['imgAlign'])
        .find('.cms-selectbox').selectBox('setValue',imgAlign);

        // get Advanced Attributes
        GetE("txtAttId").value = oImage.id;
        var langDir = oImage.dir;
        if (langDir == "") {
        langDir = 'none';
        }   
        $('#' + cms.imagepreviewhandler.keys['editorAdvancedTabId'])
        .find('#' + cms.imagepreviewhandler.editorKeys['advLangDir'])
        .find('.cms-selectbox').selectBox('setValue', langDir);

        GetE("txtAttLangCode").value = oImage.lang;
        GetE("txtAttTitle").value = oImage.title;
        GetE("txtAttClasses").value = oImage.getAttribute("class", 2) || "";
        GetE("txtLongDesc").value = oImage.longDesc;
        GetE("txtAttStyle").value = cssTxt;

        if (oLink) {
        var lnkUrl = oLink.getAttribute("_fcksavedurl");
        if (lnkUrl == null) {
        lnkUrl = oLink.getAttribute("href", 2);
        }
        if (lnkUrl != sUrl) {
        GetE("txtLnkUrl").value = lnkUrl;            
        $('#' + cms.imagepreviewhandler.keys['editorAdvancedTabId']).find('#cmbLnkTarget').find('.cms-selectbox').selectBox('setValue',oLink.target);           
        }
        var idAttr = oLink.id;
        if (idAttr != null && idAttr.indexOf("limg_") == 0) {
        cms.galleries.checkGalleryCheckbox($('#' + cms.imagepreviewhandler.editorKeys['linkOriginal']), true);          
        }
        }
    }-*/;

    /**
     * Resets the image alternative text to the original value.
     * 
     * FCK API!
     */
    // TODO: jquery global image infos
    public static native void resetAltText() /*-{
        var imgTitle = cms.galleries.activeItem.title;
        if (cms.galleries.activeItem.description != "") {
        imgTitle = cms.galleries.activeItem.description;
        }
        GetE("txtAlt").value = imgTitle;
    }-*/;

    /**
     * Resets the image copyright text to the original value.
     * 
     * Editor mode, imagegallery.
     * FCK API!
     */
    // TODO: jquery global image infos
    public static native void resetCopyrightText() /*-{
        var copyText = cms.galleries.activeItem.copyright;
        if ((copyText == null) || (copyText == "")) {
        copyText = "";
        } else {
        copyText = "&copy; " + copyText;
        }
        GetE("txtCopyright").value = copyText;
    }-*/;

    /**
     * Toggles the image spacing values.
     *  
     *  Editor mode, imagegallery.
     *  FCK API!
     */
    public static native void setImageBorder() /*-{
        if (insertImageBorder()) {
        var hSp = GetE("txtHSpace").value;
        if (hSp == "") {
        GetE("txtHSpace").value = "5";
        }
        var vSp = GetE("txtVSpace").value;
        if (vSp == "") {
        GetE("txtVSpace").value = "5";
        }
        } else {
        GetE("txtHSpace").value = "";
        GetE("txtVSpace").value = "";
        }
    }-*/;

    // TODO: this functions are calles from native functions!!! e.g setImageBorder
    //    /* Returns if the image border checkbox is checked or not. */
    //    function insertImageBorder() {
    //        var checked = $('#' + cms.imagepreviewhandler.editorKeys['imgSpacing']).hasClass('cms-checkbox-checked');    
    //        return checked; 
    //    }
    //
    //    /* Returns if the link to original image checkbox is checked or not. */
    //    function insertLinkToOriginal() {
    //        var checked = $('#' + cms.imagepreviewhandler.editorKeys['linkOriginal']).hasClass('cms-checkbox-checked');    
    //        return checked; 
    //    }
    //
    //    /* Returns if the sub title checkbox is checked or not. */
    //    function insertSubTitle() {
    //        var checked = $('#' + cms.imagepreviewhandler.editorKeys['fckInsertAlt']).hasClass('cms-checkbox-checked');    
    //        return checked;    
    //    }
    //
    //    /* Returns if the copyright checkbox is checked or not. */
    //    function insertCopyright() {
    //        var checked = $('#' + cms.imagepreviewhandler.editorKeys['fckInsertCr']).hasClass('cms-checkbox-checked');    
    //        return checked;    
    //    }

    /**
     * Returns if enhanced options are used and sub title or copyright should be inserted.<p>
     * 
     * Editor mode, imagegallery.
     * USE FCK editor object! 
     */
    // TODO: use the set values from gallery
    public static native boolean isEnhancedPreview() /*-{
        //return showEnhancedOptions && (insertSubTitle() || insertCopyright());
        return $wnd[@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_IS_ENHANCED_OPTS] && (insertSubTitle() || insertCopyright());
    }-*/;

    /**
     * Returns if enhanced options should be shown.<p> 
     */
    public static native boolean isEnhanced() /*-{
        return $wnd[@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_IS_ENHANCED_OPTS];
    }-*/;

    /**
     * Saves all image specific changes. <p>
     * 
     * Editor mode, imagegallery.
     * FCK API!
     * Global variable oImage is used!!
     */
    // TODO: remove jquery: select box settings
    public static native void closeEditor() /*-{
        var fckeditor = @org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR;        
        var oEditor = $wnd[fckeditor][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_EDITOR];
        var FCK = $wnd[fckeditor][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_FCK];

        var bHasImage = oImage != null;
        var imgCreated = false;

        if (!bHasImage) {
        oImage = FCK.InsertElement("img");
        // set flag that image is newly created
        imgCreated = true;
        }  else {
        oEditor.FCKUndo.SaveUndoStep();
        }

        updateImage(oImage);

        // now its getting difficult, be careful when modifying anything below this comment...

        if (isEnhancedPreview() && oLink) {
        // original link has to be removed if a span is created in enhanced options
        FCK.Selection.SelectNode(oLink);
        FCK.ExecuteNamedCommand("Unlink");
        }

        // now we set the image object either to the image or in case of enhanced options to a span element
        oImage = createEnhancedImage();

        if (showEnhancedOptions && (oSpan != null) && ((oSpan.id.substring(0, 5) == "aimg_") || (oSpan.id.substring(0, 5) == "timg_"))) {
        // span is already present, select it
        FCK.Selection.SelectNode(oSpan);
        // remove child elements of span
        while (oSpan.firstChild != null) {
        oSpan.removeChild(oSpan.firstChild);
        }
        }

        if (!imgCreated) {
        // delete the selection (either the image or the complete span) if the image was not freshly created
        FCK.Selection.Delete();
        // now insert the new element
        oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
        } else {
        // this handles the initial creation of an image, might be buggy...
        if (!oEditor.FCKBrowserInfo.IsIE) {
        // we have to differ here, otherwise the stupid IE creates the image twice!
        oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
        } else if (isEnhancedPreview()) {
        // in IE... insert the new element to make sure the span is inserted
        oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
        }
        }

        if (oImage.tagName != "SPAN") {
        // the object to insert is a simple image, check the link to set
        FCK.Selection.SelectNode(oImage);

        oLink = FCK.Selection.MoveToAncestorNode("A");

        var sLnkUrl = GetE("txtLnkUrl").value.Trim();
        var linkOri = "";

        if (insertLinkToOriginal()) {
        sLnkUrl = "#";
        linkOri = getLinkToOriginal();
        } else if (sLnkUrl == "#") {
        sLnkUrl = "";
        }

        if (sLnkUrl.length == 0) {
        if (oLink) {
        oLink.removeAttribute("class");
        FCK.ExecuteNamedCommand("Unlink");
        }
        } else {
        if (oLink) {  
        // remove an existing link and create it newly, because otherwise the "onclick" attribute does not vanish in Mozilla
        oLink.removeAttribute("class");
        FCK.ExecuteNamedCommand("Unlink");
        oLink = oEditor.FCK.CreateLink(sLnkUrl)[0];
        } else {
        // creating a new link
        if (!bHasImage) {
        oEditor.FCKSelection.SelectNode(oImage);
        }

        oLink = oEditor.FCK.CreateLink(sLnkUrl)[0];

        if (!bHasImage) {
        oEditor.FCKSelection.SelectNode(oLink);
        oEditor.FCKSelection.Collapse(false);
        }
        }

        if (linkOri != "") {
        // set the necessary attributes for the link to original image
        try {
        if (useTbForLinkOriginal == true) {
        oLink.setAttribute("href", linkOri);
        oLink.setAttribute("title", GetE("txtAlt").value);
        oLink.setAttribute("class", "thickbox");
        sLnkUrl = linkOri;
        } else {
        oLink.setAttribute("onclick", linkOri);
        }
        oLink.setAttribute("id", "limg_" + cms.galleries.activeItem.hash);
        oImage.setAttribute("border", "0");
        } catch (e) {}
        }
        try {
        SetAttribute(oLink, "_fcksavedurl", sLnkUrl);
        var target = $('#' + cms.imagepreviewhandler.keys['editorAdvancedTabId'])
        .find('#cmbLnkTarget').find('.cms-selectbox').selectBox('getValue');
        SetAttribute(oLink, "target", target);
        } catch (e) {}
        }
        } // end simple image tag
    }-*/;

    /* Creates the enhanced image HTML if configured. */
    /**
     * Creates the enhanced image HTML if configured.<p>
     * 
     * Editor mode, imagegallery.
     * FCK API!
     * Global variable oImage is used!!
     */
    // TODO: remove jquery: select box settings
    public static native void createEnhancedImage() /*-{
        var oEditor = $wnd[@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_EDITOR];        

        if (isEnhancedPreview()) {
        // sub title and/or copyright information has to be inserted 
        var oNewElement = oEditor.FCK.EditorDocument.createElement("SPAN");
        // now set the span attributes              
        var txtWidth = $('#' + cms.imagepreviewhandler.keys['formatTabId']).find('.cms-format-line[alt="width"]').find('input').val();
        var st = "width: " + txtWidth + "px;";      
        var al = $('#' + cms.imagepreviewhandler.keys['editorFormatTabId'])
        .find('#cmbAlign').find('.cms-selectbox').selectBox('getValue');
        if (al == "left" || al == "right") {
        st += " float: " + al + ";";
        }
        var imgVSp = GetE('txtVSpace').value;
        var imgHSp = GetE('txtHSpace').value;
        if (imgVSp != "" || imgHSp != "") {
        if (imgVSp == "") {
        imgVSp = "0";
        }
        if (imgHSp == "") {
        imgHSp = "0";
        }
        if (showEnhancedOptions && al != "") {
        var marginH = "right";
        if (al == "right") {
        marginH = "left";
        }
        st += "margin-bottom: " + imgVSp + "px; margin-" + marginH + ": " + imgHSp + "px;";
        } else {
        st += "margin: " + imgVSp + "px " + imgHSp + "px " + imgVSp + "px " + imgHSp + "px";
        }
        }
        oNewElement.style.cssText = st;
        SetAttribute(oNewElement, "id", "aimg_" + cms.galleries.activeItem.hash);

        // insert the image
        if (insertLinkToOriginal()) {
        var oLinkOrig = oEditor.FCK.EditorDocument.createElement("A");
        if (useTbForLinkOriginal == true) {
        oLinkOrig.href = getLinkToOriginal();
        oLinkOrig.setAttribute("title", cms.galleries.activeItem.title);
        oLinkOrig.setAttribute("class", "thickbox");
        } else {
        oLinkOrig.href = "#";
        oLinkOrig.setAttribute("onclick", getLinkToOriginal());
        }
        oLinkOrig.setAttribute("id", "limg_" + cms.galleries.activeItem.hash);
        oImage.setAttribute("border", "0");
        oLinkOrig.appendChild(oImage);
        oNewElement.appendChild(oLinkOrig);
        } else {
        // simply add image
        oNewElement.appendChild(oImage);
        }

        if (insertCopyright()) {
        // insert the 2nd span with the copyright information
        var copyText = GetE("txtCopyright").value;
        if (copyText == "") {
        copyText = "&copy; " + cms.galleries.activeItem.copyright;
        }
        var oSpan2 = oEditor.FCK.EditorDocument.createElement("SPAN");
        oSpan2.style.cssText = "display: block; clear: both;";
        oSpan2.className = "imgCopyright";
        oSpan2.id = "cimg_" + cms.galleries.activeItem.hash;
        oSpan2.innerHTML = copyText;
        oNewElement.appendChild(oSpan2);
        }

        if (insertSubTitle()) {
        // insert the 3rd span with the subtitle
        var altText = GetE("txtAlt").value;
        if (altText == "") {
        altText = cms.galleries.activeItem.title;
        }
        var oSpan3 = oEditor.FCK.EditorDocument.createElement("SPAN");
        oSpan3.style.cssText = "display: block; clear: both;";
        oSpan3.className = "imgSubtitle";
        oSpan3.id = "simg_" + cms.galleries.activeItem.hash;
        oSpan3.innerHTML = altText;
        oNewElement.appendChild(oSpan3);
        }

        // return the new object
        return oNewElement;
        } else {
        // return the original object
        return oImage;
        }
    }-*/;

    /**
     *  Creates the link to the original image.<p>
     * 
     * Editor mode, imagegallery.
     */
    // TODO: move to gwt??
    // TODO: global link to original image is used
    public static native String getLinkToOriginal() /*-{
        var linkUri = "";
        var useTbForLinkOriginal = $wnd[@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_USE_TB_LINK_ORG];
        if (useTbForLinkOriginal == true) {
        linkUri += cms.galleries.activeItem.linkpath;
        } else {
        linkUri += "javascript:window.open('";
        linkUri += vfsPopupUri;
        linkUri += "?uri=";
        linkUri += cms.galleries.activeItem.linkpath;
        linkUri += "', 'original', 'width=";
        linkUri += cms.galleries.activeItem.width;
        linkUri += ",height=";
        linkUri += cms.galleries.activeItem.height;
        linkUri += ",location=no,menubar=no,status=no,toolbar=no');";
        }
        return linkUri;
    }-*/;

    /**
     * Updates the image element with the values of the input fields.
     * 
     * Editor mode, imagegallery.
     * FCK API!
     * JQuery!!
     * Image functions
     */
    public static native void updateImage(String e) /*-{
        var oEditor = $wnd[@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_EDITOR];

        var txtUrl = cms.galleries.activeItem.linkpath;
        var newWidth = cms.galleries.activeItem.width;
        var newHeight = cms.galleries.activeItem.height;
        if (cms.galleries.initValues.scale == null || cms.galleries.initValues.scale == "") {
        cms.galleries.initValues.scale = "c:transparent,t:4,r=0,q=70";
        } else {
        if (cms.galleries.initValues.scale.lastIndexOf(",") == cms.galleries.initValues.scale.length - 1) {
        cms.galleries.initValues.scale =cms.galleries.initValues.scale.substring(0, cms.galleries.initValues.scale.length - 1);
        }
        }

        if (cms.galleries.activeItem.isCropped) {
        var newScale = "";
        if (cms.galleries.initValues.scale != null && cms.galleries.initValues.scale != "") {
        newScale += ",";
        }
        newScale += "cx:" + cms.galleries.activeItem.cropx;
        newScale += ",cy:" + cms.galleries.activeItem.cropy;
        newScale += ",cw:" + cms.galleries.activeItem.cropw;
        newScale += ",ch:" + cms.galleries.activeItem.croph;
        cms.galleries.initValues.scale += newScale;
        } else if (cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['getScaleValue'](cms.galleries.initValues.scale, "cx") != "") {
        cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "cx");
        cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "cy");
        cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "cw");
        cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "ch");
        }

        cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "w");
        cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "h");
        var newScale = "";
        var sizeChanged = false;
        if (cms.galleries.initValues.scale != null && cms.galleries.initValues.scale != "") {
        newScale += ",";
        }
        if (cms.galleries.activeItem.newwidth > 0 && cms.galleries.activeItem.width != cms.galleries.activeItem.newwidth) {
        sizeChanged = true;
        newScale += "w:" + cms.galleries.activeItem.newwidth;
        newWidth = cms.galleries.activeItem.newwidth;
        }
        if (cms.galleries.activeItem.newheight > 0 && cms.galleries.activeItem.height != cms.galleries.activeItem.newheight ) {
        if (sizeChanged == true) {
        newScale += ",";
        }
        sizeChanged = true;
        newScale += "h:" + cms.galleries.activeItem.newheight;
        newHeight = cms.galleries.activeItem.newheight;
        }
        cms.galleries.initValues.scale += newScale;
        if (cms.galleries.activeItem.isCropped || sizeChanged) {
        txtUrl += "?__scale=" + cms.galleries.initValues.scale;
        }

        e.src = txtUrl;
        SetAttribute(e, "_fcksavedurl", txtUrl);
        SetAttribute(e, "alt"   , GetE("txtAlt").value);
        SetAttribute(e, "width" , newWidth);
        SetAttribute(e, "height", newHeight);
        SetAttribute(e, "border", "");

        var align = $('#' + cms.imagepreviewhandler.keys['editorFormatTabId'])
        .find('#' + cms.imagepreviewhandler.editorKeys['imgAlign']).find('.cms-selectbox').selectBox('getValue');   
        SetAttribute(e, "align" , align);

        var styleAttr = "";
        SetAttribute(e, "vspace", "");
        SetAttribute(e, "hspace", "");
        if (!isEnhancedPreview()) {         
        var imgAlign = align;
        var vSp = GetE("txtVSpace").value;
        var hSp = GetE("txtHSpace").value;
        if (vSp == "") {
        vSp = "0";
        }
        if (hSp == "") {
        hSp = "0";
        }
        if (showEnhancedOptions && imgAlign == "left") {
        styleAttr = "margin-bottom: " + vSp + "px; margin-right: " + hSp + "px;";
        } else if (showEnhancedOptions && imgAlign == "right") {
        styleAttr = "margin-bottom: " + vSp + "px; margin-left: " + hSp + "px;";
        } else {
        SetAttribute(e, "vspace", GetE("txtVSpace").value);
        SetAttribute(e, "hspace", GetE("txtHSpace").value);
        }
        if (insertLinkToOriginal()) {
        SetAttribute(e, "border", "0");
        }
        }

        // advanced attributes

        var idVal = GetE("txtAttId").value;
        if (idVal == "" || idVal.substring(0, 5) == "iimg_") {
        idVal = "iimg_" + cms.galleries.activeItem.hash;
        }
        SetAttribute(e, "id", idVal);

        var langDir = $('#' + cms.imagepreviewhandler.keys['editorAdvancedTabId'])
        .find('#' + cms.imagepreviewhandler.editorKeys['advLangDir']).find('.cms-selectbox').selectBox('getValue');
        SetAttribute(e, "dir", langDir);
        SetAttribute(e, "lang", GetE("txtAttLangCode").value);
        SetAttribute(e, "title", GetE("txtAttTitle").value);
        SetAttribute(e, "class", GetE("txtAttClasses").value);
        SetAttribute(e, "longDesc", GetE("txtLongDesc").value);

        styleAttr += GetE("txtAttStyle").value;
        if (oEditor.FCKBrowserInfo.IsIE) {
        e.style.cssText = styleAttr;
        } else {
        SetAttribute(e, "style", styleAttr);
        }
    }-*/;
}