<%@ page import="org.opencms.jsp.*" %><%
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
%><%= cms.getContent("/system/workplace/resources/editors/fckeditor/editor/dialog/common/fck_dialog_common.js") %>
/**
 * The JavaScript functions of this file serve as an interface between the API of the FCKEditor and the gallery dialog.<p>
 *
 * Following function needs to be provided by the gallery dialog:<p>
 * 
 * boolean setDataInEditor()<p>
 * 
 * This should check if further user input is required and other wise set the selected resource via the provided functions <code>setLink</code> and <code>setImage</code>.<p>
 * Returning <code>true</code> when all data has been set and the dialog should be closed.<p>
 * 
 */

/** The fck editor frame. */
var parentDialog=window.parent;

// remove loading overlay and get editor reference
/** The fck editor instance. */
var fckEditor=parentDialog.InnerDialogLoaded();

/** The fck editor configuration. */
var fckConfig= fckEditor.FCKConfig;

/* Absolute path to the JSP that displays the image in original size. */
var imagePopupUri = "<%= cms.link("/system/workplace/editors/fckeditor/plugins/ocmsimage/popup.html") %>";

// some string constants

/** Link id prefix. */
var LINK_PREFIX='limg_';
/** Copyright span id prefix. */
var COPY_PREFIX='cimg_';
/** Subtitle span prefix. */
var SUB_PREFIX='simg_'
/** Enhancement span id prefix. */
var ENHANCE_PREFIX='aimg_';


/**
 * Function will be triggered by the editor dialog OK button. 
 * Will check if current gallery state allows closing and sets the selected resource parameters.<p>
 * 
 * @return <code>true</code> to close the dialog
 */
var Ok =function(){
    return setDataInEditor();
}

/**
 * Closes the dialog without setting any data.<p>
 */
function closeDialog(){
    enableDialogOk(true);
    // overriding ok function to avoid setting data
    Ok =function(){
        return true;
    }
    dialogOk();
}

/**
 * Triggers the OK action.<p>
 * 
 * @return void
 */
function dialogOk(){
    parentDialog.Ok();
}

/**
 * Enables the dialog OK button.<p>
 * 
 * @param enabled <code>boolean</code> <code>true</code> to enable the button
 * 
 * @return void
 */
function enableDialogOk(enabled){
    parentDialog.SetOkButton(enabled);
}

/**
 * Returns all available information of the selected image tag, or null, if no image is selected.<p>
 * 
 * @return a map with the following keys: 
 *          alt, clazz, height, hspace, linkPath, linkTarget, longDesc, style, title, vspace, width
 * 
 *          all keys represent a tag attribute by the same name, only linkPath and linkTarget contain
 *          information on an surrounding link tag
 */
function getImageInfo(){
    var image=_getSelectedImage()
    if (image==null){
        return {emptySelection: "emptySelection"};
    }
    var result={};
    _collectAttributes(image, result);
    if (fckEditor.FCK.Selection.HasAncestorNode("A")) {
        var imageLink=parentDialog.Selection.GetSelection().MoveToAncestorNode("A");
        if (imageLink != null && imageLink.id.substring(0, 5) == LINK_PREFIX){
            result['hash']=imageLink.id.substring(5);
            result['target']=GetAttribute(imageLink,'target',null);
            var href=GetAttribute(imageLink,'href',null);
            if (href && href!="" && href!="#" && GetAttribute(image, 'src', null).indexOf(href)==-1){
                result['linkPath']=href;
            } else{
                result['insertLinkOrig']="true";
            }
        }
        
        fckEditor.FCK.Selection.SelectNode(image);
    }
    
    // image tag already present
    if (fckEditor.FCK.Selection.HasAncestorNode("SPAN")) {
        imageWrap=parentDialog.Selection.GetSelection().MoveToAncestorNode("SPAN");
        _collectAttributes(imageWrap, result);
        if (imageWrap && imageWrap.id.substring(0, 5) == ENHANCE_PREFIX){
            result['hash']=imageWrap.id.substring(5);
            var child=imageWrap.firstChild;
            while (child){
                if (child.tagName=="SPAN"){
                    if (child.id.substring(0, 5) == COPY_PREFIX){
                        result['copyright']=_getInnerText(child);
                        result['insertCopyright']="true";
                    }
                    if (child.id.substring(0, 5) == SUB_PREFIX){
                        var title=_getInnerText(child);
                        result['title']=title;
                        result['alt']=title;
                        result['insertSubtitle']="true";
                    }
                }
                child=child.nextSibling;
            }
        }
        fckEditor.FCK.Selection.SelectNode(image);
    }
    return result;
}

/**
 * Collects the attributes from the given DOM element and adds them to the given attribute map.<p>
 */
function _collectAttributes(element, attributes){
    var attributeNames = ['alt', 'align', 'clazz', 'dir', 'height', 'hspace', 'id', 'lang', 'longDesc', 'style', 'title', 'vspace', 'width'];
    var value;
    var null_marker = "cms_null_aslkj239fhoih9";
    for (var i=0; i<attributeNames.length; i++){
       var attrName = attributeNames[i];
        if (attributeNames[i]!='clazz'){
            value=GetAttribute(element, attrName, null_marker);
        }else{
            value=GetAttribute(element, 'class', null_marker);
        }
        if (value!=null_marker){
            if (attributeNames[i]=='style'){
                value= value.replace(/margin-right:\s*\d+px;/, "");
                value= value.replace(/margin-bottom:\s*\d+px;/, "");
                value= value.replace(/margin-left:\s*\d+px;/, "");
                value= value.replace(/width:\s*\d+px;/, "");
            }
            attributes[attributeNames[i]]=value;
        }
    }
    var align=element.style.cssFloat;
    if (align==null || align==""){
        // IE only
        align=element.style.styleFloat;
    }
    if (align=="left" || align=="right"){
        attributes.align=align;
    }
    var vspace=element.style.marginBottom;
    if (vspace!=null && vspace!=""){
        var vspaceValue=parseInt(vspace);
        if (!isNaN(vspaceValue)){
            attributes.vspace= ""+vspaceValue;
        }
    }
    var hspace=element.style.marginLeft;
    var hspaceValue="test";
    if (hspace!=null || hspace!=""){
    hspaceValue=parseInt(hspace);
    }
    if (isNaN(hspaceValue)){
    
        hspace= element.style.marginRight;
        if (hspace!=null || hspace!=""){
            hspaceValue=parseInt(hspace);
        }
    }
    
    
    if (!isNaN(hspaceValue)){
        attributes.hspace= ""+hspaceValue;
    }
}


/**
 * Returns the availability of enhanced image options.<p>
 * 
 * @return <code>true</code> if enhanced image options are available
 */
function hasEnhancedImageOptions(){
    return fckConfig.ShowEnhancedOptions;
}

/**
 * Returns if lightbox should be used with enhanced image options.<p>
 * 
 * @return <code>true</code> if lightbox is available for enhanced image options
 */
function hasLightboxOption(){
    return hasEnhancedImageOptions() && fckConfig.UseTbForLinkOriginal;
}

/**
 * Returns the target attribute of the currently selected link. May be null or empty.<p>
 * 
 * @return the target attribute of the currently selected link
 */
function getLinkTarget(){
    var target="";
    if (_hasSelectedText()){
        var a = fckEditor.FCK.Selection.MoveToAncestorNode('A') ;
        if (a) {
            // link present
            fckEditor.FCK.Selection.SelectNode(a);
            // target attribute
            target = a.getAttribute("target");
        }   
    }
    return target;
}

/**
 * Inserts or updates a selected image, setting the given path and tag attributes.<p>
 * 
 * @param path <code>String</code> the image path (including optional cropping parameters)
 * @param attributes <code>JSONObject</code> a map of attribute to set on the image tag
 * 
 * @return void
 */
function setImage(path, attributes){
    
    var image=_getSelectedImage();
    if (image){
        // remove previous image and wrapping links or tags first
        var previousSelection=image;
        // looking for wrapping anchor
        if (fckEditor.FCK.Selection.HasAncestorNode("A")) {
            var imageLink=parentDialog.Selection.GetSelection().MoveToAncestorNode("A");
            if (imageLink == null || imageLink.id.substring(0, 5) != LINK_PREFIX){
                imageLink=null;
            }else{
                imageLink.removeAttribute("class");
                fckEditor.FCK.ExecuteNamedCommand("Unlink");
            }
        }
        
        // image tag already present
        if (fckEditor.FCK.Selection.HasAncestorNode("SPAN")) {
            var imageWrap=parentDialog.Selection.GetSelection().MoveToAncestorNode("SPAN");
            if (imageWrap == null || imageWrap.id.substring(0, 5) != ENHANCE_PREFIX){
                imageWrap=null;
            }else{
                previousSelection=imageWrap;
            }
            
            
        }
        fckEditor.FCK.Selection.SelectNode(previousSelection);
        fckEditor.FCK.Selection.Delete();
    }
    // create a new image element
    image = fckEditor.FCK.EditorDocument.createElement("IMG");
    image.src = path;
    SetAttribute(image, "_fcksavedurl", path);
    
    // the element going to be inserted at the selection
    var insertElement=image;
    
    // the attributes map contains only string values, so check for "true" or "false"
    if (hasEnhancedImageOptions() && (attributes.insertCopyright=="true" || attributes.insertSubtitle=="true")){
            // sub title and/or copyright information has to be inserted 
            insertElement = fckEditor.FCK.EditorDocument.createElement("SPAN");
            // now set the span attributes      
            SetAttribute(insertElement, "id", ENHANCE_PREFIX + attributes.hash);
            
            // insert the image
            if (attributes.insertLinkOrig=="true") {
                var oLinkOrig = fckEditor.FCK.EditorDocument.createElement("A");
                _setLinkOriginalProperties(oLinkOrig, path, attributes)
                image.setAttribute("border", "0");
                oLinkOrig.appendChild(image);
                insertElement.appendChild(oLinkOrig);
            } else {
                // simply add image
                insertElement.appendChild(image);
            }
    
            if (attributes.insertCopyright=="true") {
                // insert the 2nd span with the copyright information
                var copyText = attributes.copyright;
                var oSpan2 = fckEditor.FCK.EditorDocument.createElement("SPAN");
                oSpan2.style.cssText = "display: block; clear: both; width: " + attributes.width + "px;";
                oSpan2.className = "imgCopyright";
                oSpan2.id = COPY_PREFIX + attributes.hash;
                _setInnerText(oSpan2, copyText);
                insertElement.appendChild(oSpan2);
            }
    
            if (attributes.insertSubtitle=="true") {
                // insert the 3rd span with the subtitle
                var altText = attributes.alt;
                if (altText == "") {
                    altText = attributes.title;
                }
                var oSpan3 = fckEditor.FCK.EditorDocument.createElement("SPAN");
                oSpan3.style.cssText = "display: block; clear: both; width: " + attributes.width + "px;";
                oSpan3.className = "imgSubtitle";
                _setInnerText(oSpan3, altText);
                oSpan3.id = SUB_PREFIX + attributes.hash;
                insertElement.appendChild(oSpan3);
            }
            // insert into DOM
            fckEditor.FCK.InsertElement(insertElement);
    }else{
        // insert into DOM
        fckEditor.FCK.InsertElement(insertElement);
        
        fckEditor.FCK.Selection.SelectNode(image);
        
        // wrapping anchor element
        var imageLink=null;
        if (attributes.insertLinkOrig=="true"){
            imageLink=fckEditor.FCK.CreateLink("#")[0];
            _setLinkOriginalProperties(imageLink, path, attributes)
        }else if (attributes.linkPath!=null && attributes.linkPath.length>0){
            imageLink=fckEditor.FCK.CreateLink(attributes.linkPath)[0];
            SetAttribute(imageLink, "_fcksavedurl", attributes.linkPath);
            SetAttribute(imageLink, "target", attributes.linkTarget);
            imageLink.setAttribute("id", LINK_PREFIX + attributes.hash);
        }
    }
    _setAlignmentStyle(insertElement, attributes);
    _removeEnhancementAttributes(attributes);
    //iterating given attributes and setting them on the image tag
    if (attributes){
        for (var key in attributes){
            if (attributes[key]!=null){
                if (key=='clazz'){
                    SetAttribute(image, 'class', attributes[key]);
                }else{
                    SetAttribute(image, key, attributes[key]);
                }
            }
        }
    }
}

/**
 * Removes enhanced image attributes from the given attribute map.<p>
 * 
 * @param attributes the attribute map
 */
function _removeEnhancementAttributes(attributes){
    attributes.copyright=null;
    attributes.hash=null;
    attributes.insertCopyright=null;
    attributes.insertLinkOrig=null;
    attributes.insertSpacing=null;
    attributes.insertSubtitle=null;
    attributes.linkPath=null;
    attributes.linkTarget=null;
    attributes.vspace=null;
    attributes.hspace=null;
    attributes.style=null;
    attributes.width=null;
    attributes.height=null;
    attributes.align=null;
}

/**
 * Returns the alignment styles.<p>
 * 
 * @param attributes the attribute map
 * 
 * @return the alignment styles
 */
function _setAlignmentStyle(insertElement, attributes){
    if (attributes.style!=null){
        insertElement.style.cssText=attributes.style;
    }
    insertElement.style.width=attributes.width+"px";
    var al = attributes.align;
    if (al == "left" || al == "right") {
        insertElement.style.cssFloat=al;
        insertElement.style.styleFloat=al;
    }
    var imgVSp = attributes.vspace;
    var imgHSp = attributes.hspace;
    if ((imgVSp && imgVSp != "") || (imgHSp && imgHSp != "")) {
        if (!imgVSp || imgVSp == "") {
            imgVSp = "0";
        }
        if (!imgHSp || imgHSp == "") {
            imgHSp = "0";
        }
        if (al || al != "") {
            var marginH = "Right";
            if (al == "right") {
                marginH = "Left";
            }
            insertElement.style.marginBottom= imgVSp + "px"
            insertElement.style["margin" + marginH]=imgHSp + "px";
        } else {
            insertElement.style.margin=imgVSp + "px " + imgHSp + "px " + imgVSp + "px " + imgHSp + "px";
        }
    }
}

/**
 * Inserts or updates an image link, setting the given image path and tag attributes as well as the link path and target attribute.<p>
 * 
 * @param path <code>String</code> the image path (including optional cropping parameters)
 * @param attributes <code>JSONObject</code> a map of attribute to set on the image tag 
 * @param linkPath <code>String</code> the path to link to
 * @param target <code>String</code> the target attribute, optional
 * 
 * @return void
 */
function setImageLink(path, attributes, linkPath, target){

}

/**
 * Sets a link to the given path.<p>
 * 
 * @param path <code>String</code> the path to link to
 * @param title <code>String</code> the title, will be used as link content if no text has been selected within the editor
 * @param target <code>String</code> the target attribute, optional
 * 
 * @return void
 */
function setLink(path, title, target){
    if (_hasSelectedText()){
        var a = fckEditor.FCK.Selection.MoveToAncestorNode('A') ;
        if (a) {
            // link present, manipulate it
            fckEditor.FCK.Selection.SelectNode(a);
            a = fckEditor.FCK.CreateLink(path)[0];
            
        } else {
            // new link, create it
            a = fckEditor.FCK.CreateLink(path)[0];        
        }
        
        // set or remove target attribute
        if (target!=null && target!="") {
            a.target = target;
        } else {
            a.removeAttribute("target");
        }
        a.title = title;
    }else{
        var result = "<a href=\"" + path + "\" title=\"" + title;
        result += (target!=null && target!="") ? ("\" target=\"" + target) : "";
        result += "\">" + title + "<\/a>";
        fckEditor.FCK.InsertHtml(result);
    }
}

/**
 * Returns if a text part has been selected by the user.<p>
 * 
 * @return <code>true</code> if text is selected
 */
function _hasSelectedText() {
    var sel = parentDialog.Selection.GetSelection();
    var text = "";
    if (fckEditor.FCKSelection.GetSelection().createRange){
        text = fckEditor.FCKSelection.GetSelection().createRange().text;
    } else {
        text = fckEditor.FCKSelection.GetSelection();
    }
    
    if ((sel.GetType() == 'Text' || sel.GetType() == 'Control') && text != '') {
        return true;
    }
    return false; 
}
/**
 * Returns the selected image element or null if none is selected.<p>
 * 
 * @returns the image element
 */
function _getSelectedImage(){
    var path=null;
    var selected=parentDialog.Selection.GetSelectedElement();
    if (selected && selected.tagName == "IMG"){
        // try to read selected url
        path = selected.getAttribute("_fcksavedurl");
        if (path == null) {
            path = GetAttribute(selected, "src", "");
        }
    }
    return (path && path.length>0) ? selected : null;
}

/**
 * Sets the 'link to original' properties to the link-element.<p>
 * 
 * @param linkElement the link-element
 * @param path the image path
 * @param attributes additional link attributes
 */
function _setLinkOriginalProperties(linkElement, path, attributes){
    if (path.indexOf("?")!=-1){
        path=path.substring(0, path.indexOf("?"));
    }
    if (hasLightboxOption()) {
        linkElement.href = path; //TODO: create lightbox link
        linkElement.setAttribute("title", attributes.title);
        linkElement.setAttribute("class", "thickbox");
    } else {
        linkElement.href = "#";
        var linkUri = "javascript:window.open('";
        linkUri += vfsPopupUri;
        linkUri += "?uri=";
        linkUri += path;
        linkUri += "', 'original', 'width=";
        linkUri += attributes.width;
        linkUri += ",height=";
        linkUri += attributes.height;
        linkUri += ",location=no,menubar=no,status=no,toolbar=no');";
        linkElement.setAttribute("onclick", linkUri); //TODO: create popup link
    }
    linkElement.setAttribute("id", LINK_PREFIX + attributes.hash);
}

/**
 * Returns the inner text of the given element.<p>
 * 
 * This function is necessary as firefox uses the textContent property instead of innerText as in most other browsers.<p>
 * 
 * @param element the element
 * 
 * @returns the inner text
 */
function _getInnerText(element){
    if (document.body.innerText){
        return element.innerText;
    }else{
        return element.textContent;
    }
}

/**
 * Stes the inner text of the given element.<p>
 * 
 * This function is necessary as firefox uses the textContent property instead of innerText as in most other browsers.<p>
 * 
 * @param element the element
 * @param value the inner text value to set
 */
function _setInnerText(element, value){
    if (document.body.innerText){
        element.innerText = value;
    }else{
        element.textContent = value;
    }
}

//TODO: specify and implement extended image functions