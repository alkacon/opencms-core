<%@ page import="org.opencms.jsp.*" %><%
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
%>

function SetAttribute( element, attName, attValue )
{
   if ( attValue == null || attValue.length == 0 )
      element.removeAttribute( attName, 0 ) ;         // 0 : Case Insensitive
   else
      element.setAttribute( attName, attValue, 0 ) ;  // 0 : Case Insensitive
}

function GetAttribute( element, attName, valueIfNull )
{
   var oAtt = element.attributes[attName] ;

   if ( oAtt == null || !oAtt.specified )
      return valueIfNull ? valueIfNull : '' ;

   var oValue = element.getAttribute( attName, 2 ) ;

   if ( oValue == null )
      oValue = oAtt.nodeValue ;

   return ( oValue == null ? valueIfNull : oValue ) ;
}

<%= cms.getContent("/system/workplace/resources/editors/tinymce/jscripts/tiny_mce/tiny_mce_popup.js") %>
/**
 * The JavaScript functions of this file serve as an interface between the API of the TinyMCE and the gallery dialog.<p>
 *
 * Following function needs to be provided by the gallery dialog:<p>
 * 
 * boolean setDataInEditor()<p>
 * 
 * This should check if further user input is required and other wise set the selected resource via the provided functions <code>setLink</code> and <code>setImage</code>.<p>
 * Returning <code>true</code> when all data has been set and the dialog should be closed.<p>
 */
/* absolute path to the JSP that displays the image in original size */
var vfsPopupUri = "<%= cms.link("/system/workplace/editors/fckeditor/plugins/ocmsimage/popup.html") %>";
var showSelect = "true";

/** The editor frame. */
var parentDialog=window.parent;

// remove loading overlay and get editor reference
/** The editor instance. */
var editor=parentDialog.tinymce.activeEditor;
var tinymce = parentDialog.tinymce;

/** The fck editor configuration. */
var editorConfig= {};

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
   tinyMCEPopup.close();
}

/**
 * Triggers the OK action.<p>
 * 
 * @return void
 */
function dialogOk(){
//    parentDialog.Ok();
}

function enableDialogOk(enabled){
}

function _selectionHasAncestorNode(nodeName) {
   return _selectionMoveToAncestorNode(nodeName) != null; 
}

function _selectionMoveToAncestorNode(nodeName) {

   var node = editor.selection.getNode();
   return editor.dom.getParent(node, nodeName);
}

function _selectionSelectNode(node) {
   editor.selection.select(node);
}

function _editorExecuteUnlink() {
   editor.execCommand("unlink", false, null);
}


function _selectionDelete() {
    var selectionNode = editor.selection.getNode();
    var parent = selectionNode.parentNode; 
    if (parent) {
        parent.removeChild(selectionNode);
    }
}

function _editorDocumentCreateElement(nodeName) {
   return parentDialog.document.createElement(nodeName);
}

function _selectSubNode(nodeName, node) {
   if (node.tagName == 'IMG') {
      return node;
   }
   subnodes = editor.dom.select('img', node);
   if (subnodes.length == 0) {
      return null;
   }
   return subnodes[0];
}

function _editorInsertElement(element) {
   // hack to get at the DOM node of the inserted element: add a CSS class, then 
   // use it later to find the node again 
   var cls = editor.dom.uniqueId("cmsInserted");
   editor.dom.addClass(element, cls);
   var html = editor.dom.getOuterHTML(element);
   editor.selection.setContent(html);
   var node = editor.dom.select('.'+cls, editor.selection.getNode())[0];
   editor.dom.removeClass(node, cls);
   return node; 
}

function _editorCreateLink(target) {
   var linkAttrs = {href: target};
   var specialClass = "cmsInsertedLink" + Math.floor(Math.random()*10000);
   linkAttrs["class"] = specialClass;
   editor.execCommand("mceInsertLink", false, linkAttrs);
   var result = editor.dom.select("."+specialClass)[0];
   editor.dom.removeClass(result, specialClass);
   return result; 
}

function _editorInsertHtml(html) {
   editor.execCommand("mceInsertContent", false, html);
}

/**
 * Returns if a text part has been selected by the user.<p>
 * 
 * @return <code>true</code> if text is selected
 */
function _hasSelectedText() {
   return !editor.selection.isCollapsed() && editor.selection.getContent() != '';
}

/**
 * Returns the selected image element or null if none is selected.<p>
 * 
 * @returns the image element
 */
function _getSelectedImage(){
   var selected = editor.selection.getNode();
   var path = null;
   if (selected && selected.tagName == "IMG"){
      // try to read selected url
        path = selected.getAttribute("data-mce-src");
        if (path == null) {
          path = selected.getAttribute(selected, "src", "");
      }
  }
  return (path && path.length>0) ? selected : null;
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
    if (_selectionHasAncestorNode("A")) {
        var imageLink=_selectionMoveToAncestorNode("A");
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
        
        _selectionSelectNode(image);
    }
    
    // image tag already present
    if (_selectionHasAncestorNode("SPAN")) {
        imageWrap=_selectionMoveToAncestorNode("SPAN");
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
        _selectionSelectNode(image);
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
   return !!(editor.settings.cmsGalleryEnhancedOptions);
}

/**
 * Returns if lightbox should be used with enhanced image options.<p>
 * 
 * @return <code>true</code> if lightbox is available for enhanced image options
 */
function hasLightboxOption(){
   return !!(editor.settings.cmsGalleryUseThickbox); 
}

/**
 * Returns the target attribute of the currently selected link. May be null or empty.<p>
 * 
 * @return the target attribute of the currently selected link
 */
function getLinkTarget(){
    var target="";
    if (_hasSelectedText()){
        var a = _selectionMoveToAncestorNode('A') ;
        if (a) {
            // link present
            _selectionSelectNode(a);
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
    // restore the selection; needed because IE forgets the selection otherwise 
    tinyMCEPopup.restoreSelection();
    var image=_getSelectedImage();
    
    if (image){
        // remove previous image and wrapping links or tags first
        var previousSelection=image;
        // looking for wrapping anchor
        if (_selectionHasAncestorNode("A")) {
            var imageLink=_selectionMoveToAncestorNode("A");
            if (imageLink == null || imageLink.id.substring(0, 5) != LINK_PREFIX){
                imageLink=null;
            }else{
                imageLink.removeAttribute("class");
                _editorExecuteUnlink();
            }
        }
        
        // image tag already present
        if (_selectionHasAncestorNode("SPAN")) {
            var imageWrap=_selectionMoveToAncestorNode("SPAN");
            if (imageWrap == null || imageWrap.id.substring(0, 5) != ENHANCE_PREFIX){
                imageWrap=null;
            }else{
                previousSelection=imageWrap;
            }
            
            
        }
        _selectionSelectNode(previousSelection);
        _selectionDelete();
    }
    // create a new image element
    image = _editorDocumentCreateElement("IMG");
    image.src = path;
    SetAttribute(image, "data-mce-src", path);
    
    // the element going to be inserted at the selection
    var insertElement=image;
    
    // the attributes map contains only string values, so check for "true" or "false"
    if (hasEnhancedImageOptions() && (attributes.insertCopyright=="true" || attributes.insertSubtitle=="true")){
            // sub title and/or copyright information has to be inserted 
            insertElement = _editorDocumentCreateElement("SPAN");
            // now set the span attributes      
            SetAttribute(insertElement, "id", ENHANCE_PREFIX + attributes.hash);
            
            // insert the image
            if (attributes.insertLinkOrig=="true") {
                var oLinkOrig = _editorDocumentCreateElement("A");
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
                var oSpan2 = _editorDocumentCreateElement("SPAN");
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
                var oSpan3 = _editorDocumentCreateElement("SPAN");
                oSpan3.style.cssText = "display: block; clear: both; width: " + attributes.width + "px;";
                oSpan3.className = "imgSubtitle";
                _setInnerText(oSpan3, altText);
                oSpan3.id = SUB_PREFIX + attributes.hash;
                insertElement.appendChild(oSpan3);
            }
            // insert into DOM
            insertElement = _editorInsertElement(insertElement);
    }else{
        // insert into DOM
        insertElement = _editorInsertElement(insertElement);
        image = _selectSubNode("img", insertElement);
        _selectionSelectNode(image);
        
        // wrapping anchor element
        var imageLink=null;
        if (attributes.insertLinkOrig=="true"){
            imageLink=_editorCreateLink("#");
            _setLinkOriginalProperties(imageLink, path, attributes)
        }else if (attributes.linkPath!=null && attributes.linkPath.length>0){
            imageLink=_editorCreateLink(attributes.linkPath);
            SetAttribute(imageLink, "data-mce-src", attributes.linkPath);
            SetAttribute(imageLink, "target", attributes.linkTarget);
            imageLink.setAttribute("id", LINK_PREFIX + attributes.hash);
        }
    }
    image = _selectSubNode("img", insertElement);
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
    editor.selection.collapse(false);
    editor.execCommand("mceRepaint");
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
   tinyMCEPopup.restoreSelection();
    if (_hasSelectedText()){
        var a = _selectionMoveToAncestorNode('A') ;
        if (a) {
            // link present, manipulate it
            _selectionSelectNode(a);
            a = _editorCreateLink(path);
            
        } else {
            // new link, create it
            a = _editorCreateLink(path);        
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
        _editorInsertHtml(result);
    }
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
        linkElement.href = path; 
        linkElement.setAttribute("data-mce-href", path);
        linkElement.setAttribute("title", attributes.title);
        linkElement.setAttribute("class", "thickbox");
    } else {
        linkElement.href = "#";
        linkElement.setAttribute("data-mce-href", "#");
        var linkUri = "javascript:window.open('";
        linkUri += vfsPopupUri;
        linkUri += "?uri=";
        linkUri += path;
        linkUri += "', 'original', 'width=";
        linkUri += attributes.width;
        linkUri += ",height=";
        linkUri += attributes.height;
        linkUri += ",location=no,menubar=no,status=no,toolbar=no');";
        linkElement.setAttribute("onclick", linkUri);
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

