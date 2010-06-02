/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsEditorPreviewUtil.java,v $
 * Date   : $Date: 2010/06/02 14:46:36 $
 * Version: $Revision: 1.1 $
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
 * Utility class for resource preview in editor.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public final class CmsEditorPreviewUtil {

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
    private CmsEditorPreviewUtil() {

        // hiding the constructor
    }

    /**
     * Checks if a text part has been selected by the user.<p>
     * 
     * Editor mode, download gallery.
     * FCK API uses! Called by Ok()
     * OldName: hasSelectedText
     *
     * @return true if text is selected in the editor, false otherwise
     */
    public static native boolean isTextSelected() /*-{
        var dialog = $wnd.parent;
        var fckeditor = @org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR;        
        var oEditor = $wnd[fckeditor][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_EDITOR];

        var sel = dialog.Selection.GetSelection();
        var text = "";
        if (oEditor.FCKSelection.GetSelection().createRange){
        text = oEditor.FCKSelection.GetSelection().createRange().text;
        } else {
        text = oEditor.FCKSelection.GetSelection();
        }

        if ((sel.GetType() == 'Text' || sel.GetType() == 'Control') && text != '') {
        return true;
        }
        return false;
    }-*/;

    /**
     * Creates a new html link tag with given infos  and pastes a new link to the current position of the fck editor.<p>
     *  
     * Editor mode, download gallery.
     * Native function: insertHtml, escapeBrackets
     * Called by Ok()!
     *   
     * @param linkPath the new linkPath
     * @param title the title, if set
     * @param desc the description, if set to be used as title attribute
     * @param target the window target of the link
     */
    public static native void pasteLink(String linkPath, String title, String desc, String target) /*-{
        // Fck object
        var FCK = $wnd[@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_FCK];

        // local function to escape the brackets
        function escapeBrackets(s) {
        var searchResultStart = s.search(/\[.+/);
        var searchResultEnd = s.search(/.+\]/);
        var cut = (searchResultStart == 0 && searchResultEnd != -1 && s.charAt(s.length - 1) == ']');
        if (cut) {
        // cut off the first '['
        s = s.substring(1,s.length);
        // cut off the last ']'
        s = s.substring(0,s.length-1);
        }

        return s;
        }

        var result = "<a href=\"";
        result += linkPath;
        result += "\" title=\"";
        result += escapeBrackets(desc);
        result += "\" target=\"";
        result += target;
        result += "\">";
        result += escapeBrackets(title);
        result += "<\/a>";

        FCK.InsertHtml(result);
    }-*/;

    /**
     * Updates the link to the selected resource.<p>
     * 
     * Editor mode, download gallery.
     * Creates a named anchor or a link from the OpenCms link dialog.
     * Called by Ok()!
     * Old name: setLink()
     * 
     * @param linkPath the link path to the new resource 
     * @param title the title, if set
     * @param target the window target of the link
     */
    public static native void updateLink(String linkPath, String title, String target) /*-{
        var FCK = $wnd[@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR][@org.opencms.ade.galleries.client.preview.CmsPreviewUtil::KEY_FCKEDITOR_FCK];

        var linkInformation = new Object();
        linkInformation["type"] = "link";
        linkInformation["href"] = linkPath;
        linkInformation["target"] = target;       
        linkInformation["style"] = "";
        linkInformation["class"] = "";
        linkInformation["title"] = title;

        var a = FCK.Selection.MoveToAncestorNode('A') ;
        if (a) {
        // link present, manipulate it
        FCK.Selection.SelectNode(a);
        //a.href= linkInformation["href"];
        a = FCK.CreateLink(linkInformation["href"])[0];

        } else {
        // new link, create it
        a = FCK.CreateLink(linkInformation["href"])[0];

        }

        if (linkInformation["target"] != "") {
        a.target = linkInformation["target"];
        } else {
        a.removeAttribute("target");
        }

        if (linkInformation["title"] != null && linkInformation["title"] != "") {
        a.title = linkInformation["title"];
        } else {
        a.removeAttribute("title");
        }
    }-*/;
}
